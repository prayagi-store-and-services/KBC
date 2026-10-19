package com.example.sound

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Text-to-Speech Voice Host for TarkShastra.
 * Authoritative Narration Completion Semantics:
 * 1. Question reading TTS completion callback triggers ONLY when the TTS engine genuinely emits onDone(),
 *    or explicitly halts with onError()/TTS_FAILED, or after a safety watchdog cancels/stops a stalled engine.
 * 2. It NEVER emits premature completion while the utterance is still actively speaking.
 * 3. Option narration runs within the active main running timer without pause/extension.
 * 4. Hint / Tark Guru audio reading is strictly forbidden (visual only).
 * 5. Wrong Answer Solution Narration runs at a calm, natural learning pace.
 */
class SpeechNarrator(context: Context) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Ecosystem rule (19 Oct 2026): while the app speaks it holds transient audio focus, so
    // other media pauses (not mutes) and resumes afterwards. A watchdog releases the focus
    // even when no utterance callback ever arrives.
    private val audioManager: AudioManager? = try {
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    } catch (_: Exception) { null }
    private val focusLock = Any()
    private var heldFocus: Any? = null
    private var focusWatchdog: Job? = null

    private fun acquireSpeechFocus() {
        synchronized(focusLock) {
            if (heldFocus != null) return
            val am = audioManager ?: return
            val handle: Any? = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).build()
                    if (am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) req else null
                } else {
                    @Suppress("DEPRECATION")
                    if (am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) Any() else null
                }
            } catch (_: Exception) { null }
            if (handle != null) {
                heldFocus = handle
                focusWatchdog?.cancel()
                focusWatchdog = scope.launch {
                    delay(60_000L)
                    releaseSpeechFocus()
                }
            }
        }
    }

    private fun releaseSpeechFocus() {
        val handle: Any?
        synchronized(focusLock) {
            handle = heldFocus
            heldFocus = null
            focusWatchdog?.cancel()
            focusWatchdog = null
        }
        if (handle == null) return
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && handle is AudioFocusRequest) {
                am.abandonAudioFocusRequest(handle)
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (_: Exception) { }
    }

    /** Releases focus after a fire-and-forget line's estimated speaking time. */
    private fun releaseFocusAfterEstimate(text: String) {
        val words = text.split("\s+".toRegex()).size
        val est = (words * 400L + 2500L).coerceIn(4000L, 25000L)
        scope.launch {
            delay(est)
            releaseSpeechFocus()
        }
    }
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    @Volatile
    private var isReady = false
    private val initDeferred = CompletableDeferred<Boolean>()

    // Current active question utterance tracking
    @Volatile
    private var activeUtteranceId: String? = null
    private var activeWatchdogJob: Job? = null
    private var activeCompletionTriggered: AtomicBoolean? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            try {
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)
            } catch (_: Exception) {}
            initDeferred.complete(true)
            Log.d("SpeechNarrator", "TTS_READY: Engine successfully initialized")
        } else {
            isReady = false
            initDeferred.complete(false)
            Log.e("SpeechNarrator", "TTS_FAILED: Initialization returned status $status")
        }
    }

    private fun getLocaleForMode(mode: String): Locale {
        val m = mode.uppercase()
        return if (m == "ENGLISH" || m == "EN") Locale.US else Locale.forLanguageTag("hi-IN")
    }

    private fun setHostPersonality(gender: String) {
        if (gender.equals("MALE", ignoreCase = true)) {
            tts?.setPitch(0.7f)
        } else {
            tts?.setPitch(1.2f)
        }
    }

    private fun isEnglishMode(mode: String): Boolean {
        val m = mode.uppercase()
        return m == "ENGLISH" || m == "EN"
    }

    /**
     * Reads the question with authoritative completion semantics.
     * The callback is invoked ONLY when:
     * - onDone() fires for this exact utterance
     * - onError() fires for this utterance (controlled fallback)
     * - TTS is genuinely unavailable (fails init or speak returns ERROR)
     * - Stall safety watchdog triggers: it FORCIBLY STOPS the utterance before notifying completion.
     *
     * Exactly one completion invocation is guaranteed via AtomicBoolean token protection.
     */
    fun speakQuestionBounded(
        text: String,
        languageMode: String = "ENGLISH",
        hostGender: String = "FEMALE",
        onComplete: (() -> Unit)? = null
    ) {
        stop()

        scope.launch {
            // If not initialized yet, give a brief window to complete async initialization
            if (!isReady) {
                val ready = withTimeoutOrNull(800L) {
                    initDeferred.await()
                } ?: false
                if (!ready || tts == null) {
                    Log.w("SpeechNarrator", "TTS_FAILED: Engine unavailable at question start, fallback to non-narrated completion")
                    onComplete?.invoke()
                    return@launch
                }
            }

            try {
                val locale = getLocaleForMode(languageMode)
                tts?.language = locale
                tts?.setSpeechRate(1.0f) // Normal comfortable speech rate
                tts?.setPitch(if (hostGender.uppercase() == "MALE") 0.7f else 1.1f)

                val utteranceId = "Tark_Q_${System.currentTimeMillis()}"
                activeUtteranceId = utteranceId
                val isCompleted = AtomicBoolean(false)
                activeCompletionTriggered = isCompleted

                val notifyComplete = { source: String ->
                    if (isCompleted.compareAndSet(false, true)) {
                        activeWatchdogJob?.cancel()
                        activeWatchdogJob = null
                        if (activeUtteranceId == utteranceId) {
                            activeUtteranceId = null
                        }
                        releaseSpeechFocus()
                        Log.d("SpeechNarrator", "TTS_COMPLETED: Question utterance finished via $source (utteranceId=$utteranceId)")
                        scope.launch { onComplete?.invoke() }
                    }
                }

                // Authoritative Safety/Stall Detector:
                // If the underlying Android TTS engine locks up or never emits onDone,
                // this watchdog forcibly stops TTS to cancel the audio BEFORE reporting completion.
                // It NEVER allows audio to keep playing while answering starts.
                val words = text.split("\\s+".toRegex()).size
                val stallTimeoutMillis = (words * 400L + 6000L).coerceIn(8000L, 25000L)
                activeWatchdogJob = scope.launch {
                    delay(stallTimeoutMillis)
                    if (!isCompleted.get()) {
                        Log.w("SpeechNarrator", "TTS_STALL_RECOVERED: Watchdog forcibly stopping stalled TTS audio after ${stallTimeoutMillis}ms")
                        try {
                            tts?.stop()
                        } catch (_: Exception) {}
                        notifyComplete("WATCHDOG_STALL_RECOVERY")
                    }
                }

                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(id: String?) {
                        if (id == utteranceId) {
                            Log.d("SpeechNarrator", "TTS_STARTED: Utterance $utteranceId started speaking")
                        }
                    }

                    override fun onDone(id: String?) {
                        if (id == utteranceId) {
                            notifyComplete("ON_DONE")
                        }
                    }

                    override fun onError(id: String?) {
                        if (id == utteranceId) {
                            Log.e("SpeechNarrator", "TTS_FAILED: onError received for $utteranceId")
                            notifyComplete("ON_ERROR")
                        }
                    }
                })

                acquireSpeechFocus()
                val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                if (result == TextToSpeech.ERROR) {
                    Log.e("SpeechNarrator", "TTS_FAILED: speak() returned ERROR for $utteranceId")
                    notifyComplete("SPEAK_CALL_ERROR")
                }
            } catch (e: Exception) {
                Log.e("SpeechNarrator", "TTS_FAILED: Exception during speakQuestionBounded", e)
                releaseSpeechFocus()
                onComplete?.invoke()
            }
        }
    }

    fun speakSequential(hindiText: String, englishText: String, onComplete: (() -> Unit)? = null) {
        val utteranceHi = "Tark_Hi_${System.currentTimeMillis()}"
        val utteranceEn = "Tark_En_${System.currentTimeMillis()}"
        val isCompleted = AtomicBoolean(false)
        val notifyComplete = {
            if (isCompleted.compareAndSet(false, true)) {
                releaseSpeechFocus()
                scope.launch { onComplete?.invoke() }
            }
        }

        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == utteranceHi) {
                    try {
                        tts?.language = Locale.US
                        tts?.speak(englishText, TextToSpeech.QUEUE_FLUSH, null, utteranceEn)
                    } catch (_: Exception) {
                        notifyComplete()
                    }
                } else if (id == utteranceEn) {
                    notifyComplete()
                }
            }
            override fun onError(id: String?) {
                if (id == utteranceHi) {
                    try {
                        tts?.language = Locale.US
                        tts?.speak(englishText, TextToSpeech.QUEUE_FLUSH, null, utteranceEn)
                    } catch (_: Exception) {
                        notifyComplete()
                    }
                } else if (id == utteranceEn) {
                    notifyComplete()
                }
            }
        })

        try {
            tts?.language = Locale.forLanguageTag("hi-IN")
            tts?.setSpeechRate(1.0f)
            acquireSpeechFocus()
            val res = tts?.speak(hindiText, TextToSpeech.QUEUE_FLUSH, null, utteranceHi)
            if (res == TextToSpeech.ERROR) {
                notifyComplete()
            }
        } catch (_: Exception) {
            notifyComplete()
        }
    }

    /**
     * Reads option within active game timer. Does not stop or pause game timer.
     */
    fun speakOptionInGameTimer(text: String, languageMode: String = "ENGLISH") {
        if (!isReady || tts == null) return
        try {
            val locale = getLocaleForMode(languageMode)
            tts?.language = locale
            tts?.setSpeechRate(1.25f)
            val utteranceId = "Tark_Opt_${System.currentTimeMillis()}"
            acquireSpeechFocus()
            releaseFocusAfterEstimate(text)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (_: Exception) {}
    }

    /**
     * Announces result with user's authoritative profile name.
     * e.g. "Deepak, आपका जवाब सही है।" or "Deepak, your answer is correct."
     */
    fun speakResultAnnouncement(isCorrect: Boolean, languageMode: String = "ENGLISH", hostGender: String = "FEMALE", onComplete: (() -> Unit)? = null) {
        if (!isReady || tts == null) {
            onComplete?.invoke()
            return
        }
        stop()
        try {
            val locale = getLocaleForMode(languageMode)
            tts?.language = locale
            tts?.setSpeechRate(1.08f)
            tts?.setPitch(if (hostGender.uppercase() == "MALE") 0.7f else 1.1f)
            val isEn = isEnglishMode(languageMode)
            val announcement = if (isCorrect) {
                if (isEn) "Correct." else "सही।"
            } else {
                if (isEn) "Incorrect." else "गलत।"
            }
            val utteranceId = "Tark_Result_${System.currentTimeMillis()}"
            val isCompleted = AtomicBoolean(false)
            val notifyComplete = {
                if (isCompleted.compareAndSet(false, true)) {
                    releaseSpeechFocus()
                    scope.launch { onComplete?.invoke() }
                }
            }
            tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId) notifyComplete()
                }
                override fun onError(id: String?) {
                    if (id == utteranceId) notifyComplete()
                }
            })
            acquireSpeechFocus()
            val result = tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (result == TextToSpeech.ERROR) {
                notifyComplete()
            }
        } catch (_: Exception) {
            releaseSpeechFocus()
            onComplete?.invoke()
        }
    }

    /**
     * Wrong Answer Educational Solution Narration.
     * Operates at a natural, teacher-guided pace without speed limits or pressure.
     */
    fun speakSolutionNatural(text: String, languageMode: String = "ENGLISH") {
        if (!isReady || tts == null) return
        stop()
        try {
            val locale = getLocaleForMode(languageMode)
            tts?.language = locale
            tts?.setSpeechRate(0.96f)
            tts?.setPitch(1.02f)
            val utteranceId = "Tark_Solution_${System.currentTimeMillis()}"
            acquireSpeechFocus()
            releaseFocusAfterEstimate(text)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (_: Exception) {}
    }

    fun speakFinalResult(
        correct: Int,
        incorrect: Int,
        pointsWon: Long,
        language: String,
        gender: String
    ) {
        if (!isReady || tts == null) return
        stop()
        try {
            val locale = getLocaleForMode(language)
            tts?.language = locale
            setHostPersonality(gender)
            tts?.setSpeechRate(1.0f)
            val isHindi = language.equals("HINDI", ignoreCase = true)
            val text = if (isHindi) {
                "आपने $correct सवालों के सही जवाब दिए, और $incorrect के गलत। आपके कुल पॉइंट्स हैं $pointsWon।"
            } else {
                "You answered $correct questions correctly, and $incorrect incorrectly. Your total points are $pointsWon."
            }
            val utteranceId = "Final_Result_${System.currentTimeMillis()}"
            acquireSpeechFocus()
            releaseFocusAfterEstimate(text)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (_: Exception) {}
    }

    /**
     * Clearly labelled test line through the real narration path (standing rule, 19 Oct 2026):
     * the user can check the voice without starting a quiz. It is only a test, never a game event.
     */
    fun speakTest(languageMode: String = "ENGLISH", onComplete: (() -> Unit)? = null) {
        if (!isReady || tts == null) {
            onComplete?.invoke()
            return
        }
        stop()
        try {
            val locale = getLocaleForMode(languageMode)
            tts?.language = locale
            tts?.setSpeechRate(1.0f)
            val isEn = isEnglishMode(languageMode)
            val text = if (isEn) {
                "Test announcement. The TarkShastra voice is working."
            } else {
                "परीक्षण घोषणा। तर्कशास्त्र की आवाज़ ठीक से काम कर रही है।"
            }
            val utteranceId = "Tark_Test_${System.currentTimeMillis()}"
            val isCompleted = AtomicBoolean(false)
            val notifyComplete = {
                if (isCompleted.compareAndSet(false, true)) {
                    releaseSpeechFocus()
                    scope.launch { onComplete?.invoke() }
                }
            }
            tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId) notifyComplete()
                }
                override fun onError(id: String?) {
                    if (id == utteranceId) notifyComplete()
                }
            })
            acquireSpeechFocus()
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (result == TextToSpeech.ERROR) {
                notifyComplete()
            }
        } catch (_: Exception) {
            releaseSpeechFocus()
            onComplete?.invoke()
        }
    }

    fun stop() {
        releaseSpeechFocus()
        try {
            activeWatchdogJob?.cancel()
            activeWatchdogJob = null
            activeUtteranceId = null
            activeCompletionTriggered = null
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        releaseSpeechFocus()
        try {
            activeWatchdogJob?.cancel()
            activeWatchdogJob = null
            activeUtteranceId = null
            activeCompletionTriggered = null
            tts?.stop()
            tts?.shutdown()
            tts = null
            isReady = false
        } catch (_: Exception) {}
    }
}
