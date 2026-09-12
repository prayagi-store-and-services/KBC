package com.example.ui.viewmodel
import android.util.Log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TarkDatabase
import com.example.data.db.GameSessionEventEntity
import com.example.data.model.GameSessionResult
import com.example.data.model.LifelineState
import com.example.data.model.QuestionItem
import com.example.data.model.UserProfile
import com.example.data.repository.CurrentAffairsReasoningGenerator
import com.example.data.repository.PreparationProgress
import com.example.data.repository.PreparationStage
import com.example.data.repository.TarkRepository
import com.example.sound.SpeechNarrator
import com.example.sound.SoundEffectsPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Question Phase State Machine enforcing anti-cheat and strict timing contracts.
 * Transition Path:
 * READING_WINDOW (5s bounded study & TTS) -> ACTIVE_CHOICE (Options + Running Timer)
 * -> LOCKED (IRREVERSIBLE state, no edits/changes) -> VALIDATING -> REVEALED (Proof)
 * -> TRANSITIONING (<= 0.5s swap)
 */
enum class QuestionPhase {
    QUESTION_PREPARING,
    QUESTION_READING,
    ANSWER_ACTIVE,
    ANSWER_LOCKED,
    ANSWER_RESULT,
    TRANSITIONING
}

enum class TimerMode {
    TIMED,             // Main authoritative countdown (Q1-Q10) with urgency alerts
    UNLIMITED_ELAPSED, // Post-Second Padaav (Q11-Q17): count-up thinking time
    RESULT             // Post-lock / evaluation state
}

sealed interface QuizUiState {
    data object HomeScreen : QuizUiState
    data object ProfileScreen : QuizUiState
    data object HistoryScreen : QuizUiState
    data object ItProfessionalHubScreen : QuizUiState
    data object QuestionLoading : QuizUiState
    data class QuestionBankPreparing(
        val progress: PreparationProgress
    ) : QuizUiState
    data class ProfileInstalling(
        val progress: Float,
        val message: String
    ) : QuizUiState
    data class PermissionRequired(
        val missingPermissions: List<String>,
        val message: String
    ) : QuizUiState
    data class InGame(
        val question: QuestionItem,
        val currentQNumber: Int,
        val phase: QuestionPhase = QuestionPhase.QUESTION_READING,
        val timerMode: TimerMode = TimerMode.TIMED,
        val isPaused: Boolean = false,
        val pauseSecondsRemaining: Int = 10,
        val elapsedThinkingSeconds: Int = 0,
        val isHintLifelineActive: Boolean = false,
        val activeHintContent: String? = null,
        val isOptionsVisible: Boolean = false,
        val selectedOptionIndex: Int? = null,
        val lockedOptionIndex: Int? = null,
        val isLockedIn: Boolean = false,
        val isAnswerRevealed: Boolean = false,
        val isCorrect: Boolean = false,
        val timeRemainingSeconds: Int? = null,
        val totalTimeAllocated: Int? = null,
        val baseTimeSeconds: Int? = null,
        val hasBonusTime: Boolean = false,
        val accumulatedBonusSeconds: Int = 0,
        val lifelineUsedInCurrentQuestion: Boolean = false,
        val isTimerRunning: Boolean = false,
        val discardedOptionIndices: Set<Int> = emptySet(),
        val currentPointsWon: Long = 0L,
        val guaranteedSecuredPoints: Long = 0L,
        val lifelineState: LifelineState = LifelineState(),
        val isLadderDrawerOpen: Boolean = false,
        val expertDialogContent: String? = null,
        val isExpertLoading: Boolean = false,
        val fiftyFiftyProofDialog: String? = null,
        val showCheckpointFanfare: String? = null,
        val bonusLostNotice: Boolean = false,
        val identityWarningCount: Int = 0,
        val disqualificationNotice: String? = null,
        val isMonitoringActive: Boolean = false,
        val audioWaveform: List<Float> = listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f),
        val audioState: String = "NORMAL"
    ) : QuizUiState
    data class GameSummary(
        val result: GameSessionResult,
        val lastQuestion: QuestionItem?
    ) : QuizUiState
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TarkDatabase.getDatabase(application)
    private val repository = TarkRepository(
        context = application,
        questionDao = db.questionDao(),
        userProfileDao = db.userProfileDao(),
        gameHistoryDao = db.gameHistoryDao(),
        currentAffairsDao = db.currentAffairsDao(),
        sessionBankCacheDao = db.sessionBankCacheDao()
    )
    val soundPlayer = SoundEffectsPlayer(application)
    val speechNarrator = SpeechNarrator(application)
    val monitoringManager = com.example.monitoring.MonitoringManager(application, viewModelScope)

    private val _isVoiceNarrationEnabled = MutableStateFlow(true)
    val isVoiceNarrationEnabled: StateFlow<Boolean> = _isVoiceNarrationEnabled.asStateFlow()

    private val _isSoundMuted = MutableStateFlow(false)
    val isSoundMuted: StateFlow<Boolean> = _isSoundMuted.asStateFlow()

    fun toggleSound() {
        val nextMuted = !_isSoundMuted.value
        _isSoundMuted.value = nextMuted
        soundPlayer.setMuted(nextMuted)
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    val gameHistory = repository.gameHistoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.HomeScreen)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    val languageMode: StateFlow<String> = userProfile.map { it.languageMode.uppercase() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ENGLISH"
    )

    private var timerJob: Job? = null
    private var readOnlyJob: Job? = null
    private var pauseJob: Job? = null
    private var savedTimeRemainingBeforePause: Int? = null
    private var flippedQuestionIds = mutableSetOf<String>()
    private var currentSessionLifelinesUsed = 0
    private var currentSessionStartTime = 0L
    private var currentSessionCorrectCount = 0
    private var currentConsecutiveBonusSeconds = 0
    private var currentSessionId = generateSessionId()
    private var currentSessionHintsUsed = 0
    private var currentSessionWrongCount = 0
    private var currentSessionTotalResponseSeconds = 0f
    private var currentQuestionPresentationTimestamp = 0L
    private var currentNarrationToken = 0L
    private val isFinalized = java.util.concurrent.atomic.AtomicBoolean(false)
    private val isLocking = java.util.concurrent.atomic.AtomicBoolean(false)

    private fun cleanupSessionResources() {
        stopTimer()
        speechNarrator.stop()
        pauseJob?.cancel()
        pauseJob = null
        readOnlyJob?.cancel()
        readOnlyJob = null
        currentNarrationToken = System.currentTimeMillis()
        sessionLadder.clear()
        stopIdentityMonitoring()
        if (currentSessionId.isNotBlank()) {
            val oldSid = currentSessionId
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.invalidateSessionBank(oldSid)
                    repository.invalidateAllSessionBanks()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun generateSessionId(): String {
        val sdf = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US)
        val now = java.util.Date()
        return String.format(java.util.Locale.US, "TS-%s-%03d", sdf.format(now), now.time % 1000)
    }

    // Preloaded Session Pipeline to guarantee <= 0.5s transition time
    private val sessionLadder = mutableMapOf<Int, QuestionItem>()

    // Mandatory Current Affairs Slots per 17-Question Game:
    // Slot 1: randomly placed in Q1..Q5 (1st Padaav window)
    // Slot 2: randomly placed in Q6..Q10 (2nd Padaav window)
    private var sessionCurrentAffairsSlots = setOf(2, 8)

    init {
        // Silent startup online intelligence sync (24-hour rolling window)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = repository.getUserProfile()
                repository.syncCurrentAffairsSilently(profile)
            } catch (_: Exception) {}
        }
    }

    private fun calculateBaseTimeLimitForQuestion(qNum: Int): Int? {
        return when {
            qNum in 1..5 -> 60 // Stage 1: Base 60s
            qNum in 6..10 -> 120 // Stage 2: Base 120s
            else -> null // Stage 3 (Q11-Q17): Second Padaav Cleared -> Untimed Expert Reasoning
        }
    }

    fun toggleLanguage() {
        val current = languageMode.value.uppercase()
        val next = when (current) {
            "ENGLISH" -> "HINDI"
            else -> "ENGLISH"
        }
        setLanguage(next)
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val normalized = if (lang.uppercase() == "HINDI") "HINDI" else "ENGLISH"
            val updated = current.copy(languageMode = normalized)
            repository.saveUserProfile(updated)
        }
    }

    fun navigateToHome() {
        cleanupSessionResources()
        _uiState.value = QuizUiState.HomeScreen
    }

    fun navigateToProfile() {
        stopTimer()
        _uiState.value = QuizUiState.ProfileScreen
    }

    fun navigateToHistory() {
        stopTimer()
        _uiState.value = QuizUiState.HistoryScreen
    }

    fun navigateToItHub() {
        stopTimer()
        _uiState.value = QuizUiState.ItProfessionalHubScreen
    }

    private var isStartingGame = false

    fun saveProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TarkShastra", "START_GAME_REQUEST: Profile save initiated for ${updatedProfile.name}")
                _uiState.value = QuizUiState.ProfileInstalling(0.15f, "प्रोफ़ाइल सहेजी जा रही है (Saving profile)...")
                delay(400)
                repository.saveUserProfile(updatedProfile)
                android.util.Log.d("TarkShastra", "PROFILE_VALIDATED: Profile successfully persisted")

                _uiState.value = QuizUiState.ProfileInstalling(0.4f, "लाइव करंट अफेयर्स और ज्ञान वैक्टर सिंक हो रहे हैं...")
                delay(500)
                try {
                    repository.syncCurrentAffairsSilently(updatedProfile)
                } catch (e: Exception) {
                    android.util.Log.e("TarkShastra", "BANK_DOWNLOAD_FAILED: Current affairs sync error: ${e.message}")
                }

                _uiState.value = QuizUiState.ProfileInstalling(0.75f, "ऑफ़लाइन उपयोग के लिए प्रश्न और उत्तर बैंक तैयार किए जा रहे हैं...")
                android.util.Log.d("TarkShastra", "BANK_CHECK_STARTED / BANK_DOWNLOAD_STARTED")
                delay(500)

                _uiState.value = QuizUiState.ProfileInstalling(1.0f, "100% ऑफ़लाइन गेम बैंक तैयार है! लॉन्च हो रहा है...")
                delay(400)

                startNewGame()
            } catch (e: Exception) {
                android.util.Log.e("TarkShastra", "SESSION_CREATION_FAILED or START_GAME_FAILED: ${e.message}")
                _uiState.value = QuizUiState.PermissionRequired(
                    listOf(),
                    "⚠️ Game Start Error: ${e.message}. Please retry."
                )
            }
        }
    }

    private var identityMonitoringJob: Job? = null
    private var identityWarningCount = 0

    private fun startIdentityMonitoring() {
        identityMonitoringJob?.cancel()
        identityWarningCount = 0
        monitoringManager.startMonitoring()
        
        identityMonitoringJob = viewModelScope.launch {
            launch {
                monitoringManager.waveform.collect { wave ->
                    val currentState = _uiState.value
                    if (currentState is QuizUiState.InGame) {
                        _uiState.value = currentState.copy(audioWaveform = wave, isMonitoringActive = true)
                    }
                }
            }
            launch {
                monitoringManager.audioState.collect { state ->
                    val currentState = _uiState.value
                    if (currentState is QuizUiState.InGame) {
                        _uiState.value = currentState.copy(audioState = state)
                    }
                }
            }
            launch {
                monitoringManager.warningEvent.collect { eventTime ->
                    if (eventTime > 0) {
                        identityWarningCount++
                        val currentState = _uiState.value
                        if (currentState is QuizUiState.InGame) {
                            if (identityWarningCount >= 3) {
                                // Disqualify
                                _uiState.value = currentState.copy(
                                    identityWarningCount = identityWarningCount,
                                    disqualificationNotice = "DISQUALIFIED: Multiple audio violations detected."
                                )
                                val lang = userProfile.value.languageMode
                                val gender = userProfile.value.hostGender
                                val msg = if (lang.equals("HINDI", true)) {
                                    "नियम उल्लंघन। आप खेल से अयोग्य घोषित किए गए हैं।"
                                } else {
                                    "Rule violation. You have been disqualified from the game."
                                }
                                speechNarrator.speakQuestionBounded(msg, lang, gender) {
                                    finishGame(
                                        wonPoints = 0L,
                                        highestQ = currentState.currentQNumber,
                                        isGrandWin = false,
                                        reason = "DISQUALIFIED",
                                        lastQ = currentState.question
                                    )
                                }
                                stopIdentityMonitoring()
                            } else {
                                // Warning
                                _uiState.value = currentState.copy(identityWarningCount = identityWarningCount)
                                val lang = userProfile.value.languageMode
                                val gender = userProfile.value.hostGender
                                val msg = if (lang.equals("HINDI", true)) {
                                    "चेतावनी $identityWarningCount. कृपया शांति बनाए रखें।"
                                } else {
                                    "Warning $identityWarningCount of 3. Please maintain silence."
                                }
                                speechNarrator.speakQuestionBounded(msg, lang, gender)
                            }
                        }
                    }
                }
            }
        }

        try {
            viewModelScope.launch(Dispatchers.IO) {
                db.gameSessionDao().insertEvent(
                    GameSessionEventEntity(
                        sessionId = currentSessionId,
                        eventType = "IDENTITY_MONITORING_ACTIVE",
                        timestampMillis = System.currentTimeMillis(),
                        metadata = "Anti-cheating monitoring active with real microphone audio analysis"
                    )
                )
            }
        } catch (_: Exception) {}
    }

    private fun stopIdentityMonitoring() {
        monitoringManager.stopMonitoring()
        identityMonitoringJob?.cancel()
        identityMonitoringJob = null
        val currentState = _uiState.value
        if (currentState is QuizUiState.InGame) {
            _uiState.value = currentState.copy(isMonitoringActive = false)
        }
    }

    fun startNewGame() {
        if (isStartingGame) return
        isStartingGame = true

        viewModelScope.launch {
            try {
                android.util.Log.d("TarkShastra", "START_GAME_REQUEST invoked")
                val context = getApplication<Application>()
                val isTv = com.example.util.DeviceCapabilities.isTv(context)
                val deviceHasCamera = com.example.util.DeviceCapabilities.hasCamera(context)
                val deviceHasMic = com.example.util.DeviceCapabilities.hasMicrophone(context)

                val hasCamera = if (isTv && !deviceHasCamera) true else androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasMic = if (isTv && !deviceHasMic) true else androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasNotification = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

                val missing = mutableListOf<String>()
                if (!hasCamera && !(isTv && !deviceHasCamera)) missing.add("Camera")
                if (!hasMic && !(isTv && !deviceHasMic)) missing.add("Microphone")
                if (!hasNotification && !isTv) missing.add("Notifications")

                if (missing.isNotEmpty()) {
                    _uiState.value = QuizUiState.PermissionRequired(
                        missingPermissions = missing,
                        message = "⚠️ Anti-Cheating Game Access Blocked: Missing required permissions (${missing.joinToString(", ")}). Camera and Microphone are required for active-game anti-cheating verification, and Notifications are required for game updates. Please grant them in the Profile settings."
                    )
                    isStartingGame = false
                    return@launch
                }

                android.util.Log.d("TarkShastra", "PROFILE_VALIDATED for game start")
                cleanupSessionResources()
                stopIdentityMonitoring()
                isFinalized.set(false)
                isLocking.set(false)
                flippedQuestionIds.clear()
                currentSessionId = generateSessionId()
                android.util.Log.d("TarkShastra", "SESSION_CREATED: sessionId=$currentSessionId")

                currentSessionLifelinesUsed = 0
                currentSessionHintsUsed = 0
                currentSessionWrongCount = 0
                currentSessionTotalResponseSeconds = 0f
                currentSessionStartTime = System.currentTimeMillis()
                currentSessionCorrectCount = 0
                currentConsecutiveBonusSeconds = 0

                // Randomize Current Affairs slots satisfying: 1 in Q1-5, 1 in Q6-10
                val caSlot1 = (1..5).random()
                val caSlot2 = (6..10).random()
                sessionCurrentAffairsSlots = setOf(caSlot1, caSlot2)

                val profile = repository.getUserProfile()
                android.util.Log.d("TarkShastra", "BANK_CHECK_STARTED & BANK_DOWNLOAD_STARTED")

                val preloaded = repository.prepareGameSessionBank(
                    sessionId = currentSessionId,
                    userProfile = profile
                ) { progress ->
                    _uiState.value = QuizUiState.QuestionBankPreparing(progress)
                }

                if (preloaded.isEmpty() || preloaded[1] == null) {
                    android.util.Log.e("TarkShastra", "BANK_EMPTY or FIRST_QUESTION_FAILED")
                    _uiState.value = QuizUiState.PermissionRequired(
                        listOf(),
                        "⚠️ Game Start Error: Could not load question bank or first question."
                    )
                    isStartingGame = false
                    return@launch
                }

                android.util.Log.d("TarkShastra", "BANK_VALIDATED & FIRST_QUESTION_READY")
                sessionLadder.clear()
                sessionLadder.putAll(preloaded)

                startIdentityMonitoring()
                android.util.Log.d("TarkShastra", "GAME_NAVIGATION: Opening Game screen for Q1")
                loadPreloadedQuestionForTier(
                    targetQNum = 1,
                    accumulatedPoints = 0L,
                    guaranteedPoints = 0L,
                    lifelines = LifelineState()
                )
            } catch (e: Exception) {
                android.util.Log.e("TarkShastra", "GAME_NAVIGATION_FAILED: ${e.message}")
                _uiState.value = QuizUiState.PermissionRequired(
                    listOf(),
                    "⚠️ Game Start Error: ${e.message}"
                )
            } finally {
                isStartingGame = false
            }
        }
    }

    private fun loadPreloadedQuestionForTier(
        targetQNum: Int,
        accumulatedPoints: Long,
        guaranteedPoints: Long,
        lifelines: LifelineState,
        isFlipReplacement: Boolean = false
    ) {
        stopTimer()
        isLocking.set(false)
        currentQuestionPresentationTimestamp = System.currentTimeMillis()

        val question = sessionLadder[targetQNum] ?: run {
            val profile = userProfile.value
            CurrentAffairsReasoningGenerator.generateReasoningQuestion(targetQNum, profile)
        }

        val baseTime = calculateBaseTimeLimitForQuestion(targetQNum)
        val isTimed = baseTime != null
        val allocatedTime = baseTime

        _uiState.value = QuizUiState.InGame(
            question = question,
            currentQNumber = targetQNum,
            phase = QuestionPhase.QUESTION_READING,
            timerMode = if (isTimed) TimerMode.TIMED else TimerMode.UNLIMITED_ELAPSED,
            elapsedThinkingSeconds = 0,
            isOptionsVisible = true,
            selectedOptionIndex = null,
            lockedOptionIndex = null,
            isLockedIn = false,
            isAnswerRevealed = false,
            isCorrect = false,
            timeRemainingSeconds = allocatedTime,
            totalTimeAllocated = allocatedTime,
            baseTimeSeconds = baseTime,
            lifelineUsedInCurrentQuestion = isFlipReplacement,
            isTimerRunning = false,
            discardedOptionIndices = emptySet(),
            currentPointsWon = accumulatedPoints,
            guaranteedSecuredPoints = guaranteedPoints,
            lifelineState = lifelines,
            isLadderDrawerOpen = false,
            expertDialogContent = null,
            isExpertLoading = false,
            fiftyFiftyProofDialog = null,
            showCheckpointFanfare = if (question.isCheckpoint && !isFlipReplacement) question.checkpointTitle else null
        )

        val langMode = languageMode.value
        val qText = when (langMode) {
            "ENGLISH" -> question.questionEnglish
            "BILINGUAL" -> "${question.questionHindi}\n${question.questionEnglish}"
            else -> question.questionHindi
        }

        speechNarrator.stop()
        currentNarrationToken = System.currentTimeMillis()
        val token = currentNarrationToken
        val sessionId = currentSessionId
        Log.d("QuizViewModel", "QUESTION_READING_STARTED: Q$targetQNum (sessionId=$sessionId, narrationToken=$token, timed=$isTimed, allocated=${allocatedTime}s)")

        if (_isVoiceNarrationEnabled.value) {
            speechNarrator.speakQuestionBounded(qText, langMode, userProfile.value.hostGender) {
                if (currentSessionId == sessionId && currentNarrationToken == token) {
                    Log.d("QuizViewModel", "TTS_COMPLETED: Genuinely finished question narration callback received for Q$targetQNum")
                    transitionToAnswerActive(targetQNum, allocatedTime, isTimed, token, sessionId)
                } else {
                    Log.w("QuizViewModel", "TTS_CALLBACK_IGNORED: Stale question narration callback discarded (currentQ=$targetQNum, callbackToken=$token, activeToken=$currentNarrationToken)")
                }
            }
        } else {
            // Voice narration is explicitly turned off by user: transition to ANSWER_ACTIVE directly
            Log.d("QuizViewModel", "VOICE_NARRATION_DISABLED: Transitioning directly to ANSWER_ACTIVE for Q$targetQNum")
            transitionToAnswerActive(targetQNum, allocatedTime, isTimed, token, sessionId)
        }
    }

    private fun transitionToAnswerActive(
        targetQNum: Int,
        mainTimeLimit: Int?,
        isTimed: Boolean,
        narrationToken: Long = currentNarrationToken,
        sessionId: String = currentSessionId
    ) {
        // Authoritative Gating Verification:
        // 1. Session must still match
        if (currentSessionId != sessionId) {
            Log.w("QuizViewModel", "TRANSITION_REJECTED: Session mismatch (expected $sessionId, current $currentSessionId)")
            return
        }
        // 2. Narration token must match
        if (currentNarrationToken != narrationToken) {
            Log.w("QuizViewModel", "TRANSITION_REJECTED: Narration token mismatch (expected $narrationToken, current $currentNarrationToken)")
            return
        }
        // 3. Game must not be finalized/ended
        if (isFinalized.get()) {
            Log.w("QuizViewModel", "TRANSITION_REJECTED: Game is already finalized")
            return
        }

        val currentState = _uiState.value
        // 4. Must be InGame, matching target question number, and strictly in QUESTION_READING phase
        if (currentState is QuizUiState.InGame &&
            currentState.currentQNumber == targetQNum &&
            currentState.phase == QuestionPhase.QUESTION_READING
        ) {
            Log.d("QuizViewModel", "ANSWER_ACTIVE_ENTERED: Q$targetQNum entered ANSWER_ACTIVE (isTimed=$isTimed, timeLimit=${mainTimeLimit}s)")
            soundPlayer.playOptionSelected()
            if (mainTimeLimit != null) {
                _uiState.value = currentState.copy(
                    phase = QuestionPhase.ANSWER_ACTIVE,
                    timerMode = TimerMode.TIMED,
                    isOptionsVisible = true,
                    isTimerRunning = true,
                )
                startTimer(mainTimeLimit)
            } else {
                _uiState.value = currentState.copy(
                    phase = QuestionPhase.ANSWER_ACTIVE,
                    timerMode = TimerMode.UNLIMITED_ELAPSED,
                    isOptionsVisible = true,
                    isTimerRunning = false,
                )
                startUnlimitedThinkingTimer()
            }
        } else {
            Log.w("QuizViewModel", "TRANSITION_IGNORED: State not in QUESTION_READING or Q number mismatch (current=$currentState)")
        }
    }

    private fun startTimer(totalSeconds: Int) {
        // HARD DEFENSIVE GATE:
        // Must NEVER start timer if game is not in ANSWER_ACTIVE or if question is still being read.
        val stateBeforeStart = _uiState.value
        if (stateBeforeStart !is QuizUiState.InGame ||
            stateBeforeStart.phase != QuestionPhase.ANSWER_ACTIVE ||
            !stateBeforeStart.isTimerRunning ||
            stateBeforeStart.isLockedIn ||
            isFinalized.get()
        ) {
            Log.w("QuizViewModel", "START_TIMER_REJECTED: Cannot start countdown timer outside of active ANSWER_ACTIVE phase (phase=${(stateBeforeStart as? QuizUiState.InGame)?.phase})")
            return
        }

        timerJob?.cancel()
        Log.d("QuizViewModel", "TIMER_STARTED: Authoritative countdown started from ${totalSeconds}s for Q${stateBeforeStart.currentQNumber}")

        // Start tension background synthesizer pressure track ONLY after entering ANSWER_ACTIVE
        soundPlayer.startTimerPressureMusic(
            getRemainingSeconds = {
                val s = _uiState.value
                if (s is QuizUiState.InGame) s.timeRemainingSeconds else null
            },
            getTotalSeconds = { totalSeconds }
        )

        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            val profile = userProfile.value

            while (remaining > 0) {
                delay(1000)
                remaining--
                val currentState = _uiState.value
                if (currentState is QuizUiState.InGame &&
                    currentState.phase == QuestionPhase.ANSWER_ACTIVE &&
                    currentState.isTimerRunning &&
                    !currentState.isLockedIn
                ) {
                    _uiState.value = currentState.copy(
                        timeRemainingSeconds = remaining
                    )
                } else {
                    break
                }
            }

            // Time Expired! (strictly unreachable if phase != ANSWER_ACTIVE)
            val state = _uiState.value
            if (state is QuizUiState.InGame &&
                state.phase == QuestionPhase.ANSWER_ACTIVE &&
                !state.isLockedIn &&
                remaining == 0
            ) {
                handleTimeExpired(state)
            }
        }
    }

    private fun cleanupAntiCheatingSession() {
        stopIdentityMonitoring()
        stopTimer()
        identityWarningCount = 0
    }

    private fun startUnlimitedThinkingTimer() {
        // HARD DEFENSIVE GATE:
        // Must NEVER start thinking timer during QUESTION_READING.
        val stateBeforeStart = _uiState.value
        if (stateBeforeStart !is QuizUiState.InGame ||
            stateBeforeStart.phase != QuestionPhase.ANSWER_ACTIVE ||
            stateBeforeStart.isLockedIn ||
            isFinalized.get()
        ) {
            Log.w("QuizViewModel", "START_UNLIMITED_TIMER_REJECTED: Cannot start thinking timer outside of ANSWER_ACTIVE phase (phase=${(stateBeforeStart as? QuizUiState.InGame)?.phase})")
            return
        }

        timerJob?.cancel()
        Log.d("QuizViewModel", "TIMER_STARTED: Unlimited thinking timer started for Q${stateBeforeStart.currentQNumber}")

        // Mystical ambient deep-thinking music for Q11-Q17 starts strictly in ANSWER_ACTIVE
        soundPlayer.startUnlimitedDeepThinkingMusic {
            val s = _uiState.value
            if (s is QuizUiState.InGame) s.elapsedThinkingSeconds else 0
        }

        timerJob = viewModelScope.launch {
            var elapsed = 0
            while (true) {
                delay(1000)
                elapsed++
                val currentState = _uiState.value
                if (currentState is QuizUiState.InGame &&
                    currentState.phase == QuestionPhase.ANSWER_ACTIVE &&
                    !currentState.isLockedIn &&
                    currentState.timerMode == TimerMode.UNLIMITED_ELAPSED
                ) {
                    _uiState.value = currentState.copy(elapsedThinkingSeconds = elapsed)
                } else {
                    break
                }
            }
        }
    }

    private var hintJob: Job? = null

    fun useHintLifeline() {
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.lifelineState.isHintAvailable && !state.lifelineState.isHintUsedInCurrentQ && state.phase == QuestionPhase.ANSWER_ACTIVE) {
            currentSessionHintsUsed++
            
            // Generate hint text from available clues, hint, and deduction paths
            val langMode = languageMode.value
            val hintText = when (langMode) {
                "ENGLISH" -> {
                    when {
                        state.question.hintEnglish.isNotBlank() -> state.question.hintEnglish
                        state.question.cluesEnglish.isNotEmpty() -> state.question.cluesEnglish.joinToString("\n• ", prefix = "• ")
                        state.question.deductionPathEnglish.isNotBlank() -> state.question.deductionPathEnglish
                        else -> "Analyze the deductive constraints and intermediate relations."
                    }
                }
                "BILINGUAL" -> {
                    val h = when {
                        state.question.hintHindi.isNotBlank() -> state.question.hintHindi
                        state.question.cluesHindi.isNotEmpty() -> state.question.cluesHindi.joinToString("\n• ", prefix = "• ")
                        state.question.deductionPathHindi.isNotBlank() -> state.question.deductionPathHindi
                        else -> "तार्किक बाधाओं का परीक्षण करें।"
                    }
                    val e = when {
                        state.question.hintEnglish.isNotBlank() -> state.question.hintEnglish
                        state.question.cluesEnglish.isNotEmpty() -> state.question.cluesEnglish.joinToString("\n• ", prefix = "• ")
                        state.question.deductionPathEnglish.isNotBlank() -> state.question.deductionPathEnglish
                        else -> "Analyze deductive constraints."
                    }
                    "$h\n\n$e"
                }
                else -> {
                    when {
                        state.question.hintHindi.isNotBlank() -> state.question.hintHindi
                        state.question.cluesHindi.isNotEmpty() -> state.question.cluesHindi.joinToString("\n• ", prefix = "• ")
                        state.question.deductionPathHindi.isNotBlank() -> state.question.deductionPathHindi
                        else -> "तार्किक बाधाओं और मुख्य संबंधों का परीक्षण करें।"
                    }
                }
            }

            // Pause existing timer if running
            val timeRemaining = state.timeRemainingSeconds
            if (state.isTimerRunning) {
                stopTimer()
            }
            
            // Mark hint used and show it
            _uiState.value = state.copy(
                lifelineState = state.lifelineState.copy(isHintUsedInCurrentQ = true),
                isHintLifelineActive = true,
                activeHintContent = hintText,
                isTimerRunning = false // visually pause
            )
            soundPlayer.playLifelineExpert()

            // Auto-dismiss after 6 seconds if not manually dismissed
            hintJob?.cancel()
            hintJob = viewModelScope.launch {
                delay(6000)
                dismissHint()
            }
        }
    }

    fun dismissHint() {
        hintJob?.cancel()
        hintJob = null
        val currentState = _uiState.value
        if (currentState is QuizUiState.InGame && currentState.isHintLifelineActive) {
            val timeRemaining = currentState.timeRemainingSeconds
            _uiState.value = currentState.copy(
                isHintLifelineActive = false,
                activeHintContent = null,
                isTimerRunning = currentState.timerMode == TimerMode.TIMED
            )
            // Resume timer
            if (currentState.timerMode == TimerMode.TIMED && timeRemaining != null) {
                startTimer(timeRemaining)
            } else if (currentState.timerMode == TimerMode.UNLIMITED_ELAPSED) {
                startUnlimitedThinkingTimer()
            }
        }
    }

    private fun stopTimer() {
        readOnlyJob?.cancel()
        readOnlyJob = null
        timerJob?.cancel()
        timerJob = null
        stopIdentityMonitoring()
        soundPlayer.stopTimerPressureMusic()
    }

    fun pauseGame() {
        val state = _uiState.value
        if (state is QuizUiState.InGame && !state.isPaused && state.phase == QuestionPhase.ANSWER_ACTIVE && state.isTimerRunning) {
            stopTimer()
            savedTimeRemainingBeforePause = state.timeRemainingSeconds
            _uiState.value = state.copy(
                isPaused = true,
                pauseSecondsRemaining = 10,
                isTimerRunning = false
            )
            pauseJob?.cancel()
            pauseJob = viewModelScope.launch {
                var pRemaining = 10
                while (pRemaining > 0) {
                    delay(1000)
                    pRemaining--
                    val s = _uiState.value
                    if (s is QuizUiState.InGame && s.isPaused) {
                        _uiState.value = s.copy(pauseSecondsRemaining = pRemaining)
                    } else {
                        break
                    }
                }
                val s = _uiState.value
                if (s is QuizUiState.InGame && s.isPaused) {
                    resumeGame()
                }
            }
        }
    }

    fun resumeGame() {
        pauseJob?.cancel()
        pauseJob = null
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.isPaused) {
            val restoredTime = savedTimeRemainingBeforePause ?: state.timeRemainingSeconds ?: 60
            _uiState.value = state.copy(
                isPaused = false,
                timeRemainingSeconds = restoredTime,
                isTimerRunning = true
            )
            startTimer(restoredTime)
        }
    }

    /**
     * Option Selection with Strict Anti-Cheat Immutability.
     * Rejects any edit if state is LOCKED, REVEALED, or in READING_WINDOW.
     */
    fun selectOption(index: Int) {
        val state = _uiState.value
        if (state !is QuizUiState.InGame || isFinalized.get() || isLocking.get()) return

        // IMMUTABILITY & ANTI-CHEAT GUARD:
        if (state.phase != QuestionPhase.ANSWER_ACTIVE || state.isLockedIn || state.lockedOptionIndex != null) {
            return
        }
        if (state.discardedOptionIndices.contains(index)) {
            return
        }

        soundPlayer.playOptionSelected()
        _uiState.value = state.copy(selectedOptionIndex = index)
    }

    /**
     * Lock In Answer ("ताला लगाएँ").
     * IRREVERSIBLE STATE TRANSITION:
     * UNLOCKED -> LOCK ANSWER -> LOCKED
     * Once locked:
     * ❌ Option change forbidden
     * ❌ Selection of another option rejected
     * ❌ Cannot revert to previous option
     * ❌ Answer edit impossible
     * ❌ Navigation/recomposition cannot overwrite
     */
    fun lockInAnswer() {
        val state = _uiState.value
        if (state !is QuizUiState.InGame || isFinalized.get()) return

        // Anti-cheat guard: must be in ANSWER_ACTIVE with an option selected and not already locked
        if (state.phase != QuestionPhase.ANSWER_ACTIVE || state.selectedOptionIndex == null || state.isLockedIn) {
            return
        }

        if (!isLocking.compareAndSet(false, true)) {
            return
        }

        val chosenIndex = state.selectedOptionIndex

        // Stop all timers and tension music immediately
        stopTimer()
        speechNarrator.stop()

        // Set authoritative locked state
        _uiState.value = state.copy(
            phase = QuestionPhase.ANSWER_LOCKED,
            isLockedIn = true,
            lockedOptionIndex = chosenIndex,
            isTimerRunning = false
        )

        // ⚡ <= 0.1 SEC REVEAL REQUIREMENT:
        // Validate answer against local precomputed question model with zero network/AI latency
        val isAnswerCorrect = chosenIndex == state.question.correctAnswerIndex
        val duration = ((System.currentTimeMillis() - currentQuestionPresentationTimestamp) / 1000f).coerceAtLeast(0.5f)
        currentSessionTotalResponseSeconds += duration

        if (isAnswerCorrect) {
            currentSessionCorrectCount++
            val newWonPoints = state.question.points
            val isCheckpoint = state.question.isCheckpoint
            val newGuaranteed = if (isCheckpoint) newWonPoints else state.guaranteedSecuredPoints

            if (isCheckpoint) {
                soundPlayer.playCheckpointFanfare()
            } else {
                soundPlayer.playCorrectAnswer()
            }

            // Reveal correct status immediately (<= 0.1s)
            _uiState.value = state.copy(
                phase = QuestionPhase.ANSWER_RESULT,
                timerMode = TimerMode.RESULT,
                isAnswerRevealed = true,
                isCorrect = true,
                currentPointsWon = newWonPoints,
                guaranteedSecuredPoints = newGuaranteed,
                lockedOptionIndex = chosenIndex,
                isLockedIn = true,
                isTimerRunning = false
            )

            val advanceLogic = {
                viewModelScope.launch {
                    delay(1000)
                    if (state.currentQNumber == 17) {
                        finishGame(
                            wonPoints = newWonPoints,
                            highestQ = 17,
                            isGrandWin = true,
                            reason = "CLEARED_7_CRORE",
                            lastQ = state.question
                        )
                    } else {
                        advanceToNextQuestion(
                            nextQNum = state.currentQNumber + 1,
                            accumulatedPoints = newWonPoints,
                            guaranteedPoints = newGuaranteed,
                            lifelines = state.lifelineState.copy(
                                is5050UsedInCurrentQ = false,
                                isExpertUsedInCurrentQ = false,
                                isHintUsedInCurrentQ = false
                            )
                        )
                    }
                }
            }

            if (_isVoiceNarrationEnabled.value) {
                speechNarrator.speakResultAnnouncement(isCorrect = true, languageMode.value, userProfile.value.hostGender) {
                    advanceLogic()
                }
            } else {
                viewModelScope.launch {
                    delay(1200)
                    advanceLogic()
                }
            }
        } else {
            // A WRONG LOCKED ANSWER = GAME OVER
            currentSessionWrongCount++
            soundPlayer.playWrongAnswer()
            _uiState.value = state.copy(
                phase = QuestionPhase.ANSWER_RESULT,
                timerMode = TimerMode.RESULT,
                isAnswerRevealed = true,
                isCorrect = false,
                lockedOptionIndex = chosenIndex,
                isLockedIn = true,
                isTimerRunning = false
            )

            val onGameOver: () -> Unit = {
                viewModelScope.launch {
                    delay(1200)
                    finishGame(
                        wonPoints = state.guaranteedSecuredPoints,
                        highestQ = state.currentQNumber,
                        isGrandWin = false,
                        reason = "WRONG_ANSWER",
                        lastQ = state.question
                    )
                }
            }

            if (_isVoiceNarrationEnabled.value) {
                speechNarrator.speakResultAnnouncement(isCorrect = false, languageMode.value, userProfile.value.hostGender) {
                    onGameOver()
                }
            } else {
                onGameOver()
            }
        }
    }

    /**
     * Advances to the next question within <= 0.5s without any network/AI latency.
     */
    private fun advanceToNextQuestion(
        nextQNum: Int,
        accumulatedPoints: Long,
        guaranteedPoints: Long,
        lifelines: LifelineState
    ) {
        _uiState.value = QuizUiState.QuestionLoading
        val isValid = validateSessionIntegrity(nextQNum)
        if (!isValid) {
            _uiState.value = QuizUiState.PermissionRequired(
                listOf(),
                "⚠️ Game State Error: Invalid or stale question bank detected by watchdog. Please start a new session."
            )
            return
        }

        loadPreloadedQuestionForTier(
            targetQNum = nextQNum,
            accumulatedPoints = accumulatedPoints,
            guaranteedPoints = guaranteedPoints,
            lifelines = lifelines
        )
    }

    private fun validateSessionIntegrity(targetQNum: Int): Boolean {
        // Watchdog: Check if the question exists and belongs to the current session logic
        val question = sessionLadder[targetQNum] ?: return false
        return true
    }

    fun quitGame() {
        val state = _uiState.value
        if (state !is QuizUiState.InGame || isFinalized.get()) return
        stopTimer()
        speechNarrator.stop()
        soundPlayer.stopAllMusic()
        stopIdentityMonitoring()

        finishGame(
            wonPoints = state.currentPointsWon,
            highestQ = state.currentQNumber,
            isGrandWin = false,
            reason = "QUIT",
            lastQ = state.question
        )
    }

    fun onHomeOrBackgroundExit() {
        val state = _uiState.value
        if (state !is QuizUiState.InGame || isFinalized.get()) return
        stopTimer()
        speechNarrator.stop()
        soundPlayer.stopAllMusic()
        stopIdentityMonitoring()

        finishGame(
            wonPoints = state.currentPointsWon,
            highestQ = state.currentQNumber,
            isGrandWin = false,
            reason = "HOME_EXIT",
            lastQ = state.question
        )
    }

    private fun handleTimeExpired(state: QuizUiState.InGame) {
        if (isFinalized.get()) return

        stopTimer()
        speechNarrator.stop()
        soundPlayer.playWrongAnswer()

        val duration = (state.baseTimeSeconds ?: 60).toFloat()
        currentSessionTotalResponseSeconds += duration
        currentSessionWrongCount++

        val hadSelection = state.selectedOptionIndex != null
        val wasSelectedCorrect = hadSelection && state.selectedOptionIndex == state.question.correctAnswerIndex
        val timeoutReason = when {
            !hadSelection -> "TIMEOUT_NO_SELECTION"
            wasSelectedCorrect -> "TIMEOUT_SELECTED_CORRECT"
            else -> "TIMEOUT_SELECTED_INCORRECT"
        }

        // Reveal correct answer on screen without locking in as answered
        _uiState.value = state.copy(
            phase = QuestionPhase.ANSWER_RESULT,
            timerMode = TimerMode.RESULT,
            isAnswerRevealed = true,
            isCorrect = false,
            lockedOptionIndex = null,
            isLockedIn = false,
            isTimerRunning = false
        )

        val onTimeoutDone: () -> Unit = {
            viewModelScope.launch {
                delay(1200)
                finishGame(
                    wonPoints = state.guaranteedSecuredPoints,
                    highestQ = state.currentQNumber,
                    isGrandWin = false,
                    reason = timeoutReason,
                    lastQ = state.question
                )
            }
        }

        if (_isVoiceNarrationEnabled.value) {
            speechNarrator.speakSequential(
                hindiText = "समय सीमा समाप्त! खेल यहीं समाप्त होता है।",
                englishText = "Time has expired! Game over.",
                onComplete = onTimeoutDone
            )
        } else {
            onTimeoutDone()
        }
    }

    private fun finishGame(
        wonPoints: Long,
        highestQ: Int,
        isGrandWin: Boolean,
        reason: String,
        lastQ: QuestionItem?
    ) {
        if (!isFinalized.compareAndSet(false, true)) {
            return
        }
        cleanupSessionResources()
        val avgResponseTime = if (highestQ > 0) currentSessionTotalResponseSeconds / highestQ else 0f
        val accuracy = if (highestQ > 0) ((currentSessionCorrectCount.toFloat() / highestQ) * 100).toInt() else 0
        val profile = userProfile.value

        val finalPoints = if (reason == "DISQUALIFIED") 0L else wonPoints
        val securedAmount = if (reason == "DISQUALIFIED") 0L else if (reason == "WRONG_ANSWER" || reason.startsWith("TIMEOUT")) wonPoints else wonPoints

        val result = GameSessionResult(
            sessionId = currentSessionId,
            userName = profile.name,
            totalPointsWon = finalPoints,
            highestQuestionReached = highestQ,
            isCompletedWon = isGrandWin,
            guaranteedPointsSecured = securedAmount,
            reasonEnded = reason,
            questionsAnsweredCount = highestQ,
            correctCount = currentSessionCorrectCount,
            wrongCount = currentSessionWrongCount,
            lifelinesUsedCount = currentSessionLifelinesUsed,
            hintsUsedCount = currentSessionHintsUsed,
            totalResponseTimeSec = currentSessionTotalResponseSeconds,
            averageResponseTimeSec = avgResponseTime,
            logicAccuracyPercentage = accuracy,
            gameMode = if (profile.isStudentMode) "Junior (${profile.studentClass})" else "Adult",
            examContext = profile.preparationDomain
        )

        viewModelScope.launch {
            repository.saveGameSession(result)
            _uiState.value = QuizUiState.GameSummary(result, lastQ)
            
            // Prefetch next game session in background for zero-latency start
            launch(Dispatchers.IO) {
                try {
                    repository.preloadNextGameSession(profile)
                } catch (_: Exception) {}
            }

            // Payout Reporting - only if not disqualified and winning amount > 0
            if (reason != "DISQUALIFIED" && !profile.upiId.isNullOrBlank() && finalPoints > 0) {
                val dto = com.example.data.api.PayoutReportDto(
                    userName = profile.name,
                    upiId = profile.upiId,
                    pointsWon = finalPoints,
                    correctAnswers = currentSessionCorrectCount,
                    incorrectAnswers = currentSessionWrongCount,
                    resultId = currentSessionId
                )
                com.example.data.api.DefaultPayoutReportingService().reportPayout(dto)
            }

            // Speak Result
            if (_isVoiceNarrationEnabled.value) {
                if (reason == "DISQUALIFIED") {
                    speechNarrator.speakSequential(
                        hindiText = "नियम उल्लंघन! आप खेल से अयोग्य घोषित किए गए हैं।",
                        englishText = "Game disqualified due to rules violation."
                    )
                } else {
                    speechNarrator.speakFinalResult(
                        correct = currentSessionCorrectCount,
                        incorrect = currentSessionWrongCount,
                        pointsWon = finalPoints,
                        language = profile.languageMode,
                        gender = profile.hostGender
                    )
                }
            }
        }
    }

    // ==========================================
    // LIFELINE IMPLEMENTATIONS
    // ==========================================

    private fun cancelBonusOnLifelineUsage(state: QuizUiState.InGame): Pair<Boolean, Int?> {
        currentConsecutiveBonusSeconds = 0 // Break progression immediately
        if (!state.hasBonusTime || state.baseTimeSeconds == null) {
            return Pair(false, state.timeRemainingSeconds)
        }
        val cur = state.timeRemainingSeconds ?: state.baseTimeSeconds
        val adjusted = cur.coerceAtMost(state.baseTimeSeconds)
        return Pair(false, adjusted)
    }

    fun use5050() {
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn && state.lifelineState.is5050Available && !state.lifelineState.is5050Exhausted) {
            soundPlayer.playLifeline5050()
            currentSessionLifelinesUsed++

            val discards = state.question.fiftyFiftyDiscardIndices.toSet()
            val langMode = languageMode.value
            val proof = when (langMode) {
                "ENGLISH" -> state.question.fiftyFiftyProofEnglish
                "BILINGUAL" -> "${state.question.fiftyFiftyProofHindi}\n${state.question.fiftyFiftyProofEnglish}"
                else -> state.question.fiftyFiftyProofHindi
            }

            val updatedLifelines = state.lifelineState.copy(
                is5050Available = false,
                is5050UsedInCurrentQ = true,
                is5050Exhausted = true
            )

            val (bonusActive, adjustedTime) = cancelBonusOnLifelineUsage(state)

            _uiState.value = state.copy(
                discardedOptionIndices = discards,
                lifelineState = updatedLifelines,
                fiftyFiftyProofDialog = proof,
                hasBonusTime = bonusActive,
                accumulatedBonusSeconds = 0,
                lifelineUsedInCurrentQuestion = true,
                timeRemainingSeconds = adjustedTime,
                bonusLostNotice = state.hasBonusTime && !bonusActive
            )
        }
    }

    fun dismiss5050Proof() {
        val state = _uiState.value
        if (state is QuizUiState.InGame) {
            _uiState.value = state.copy(fiftyFiftyProofDialog = null)
        }
    }

    /**
     * Ask the Expert (Tark Guru).
     * STRICT CONTRACT: Screen display only. ❌ Zero voice reading.
     */
    fun useAskExpert() {
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn && state.lifelineState.isExpertAvailable && !state.lifelineState.isExpertExhausted) {
            soundPlayer.playLifelineExpert()
            currentSessionLifelinesUsed++

            val updatedLifelines = state.lifelineState.copy(
                isExpertAvailable = false,
                isExpertUsedInCurrentQ = true,
                isExpertExhausted = true
            )

            val langMode = languageMode.value
            val fallback = when (langMode) {
                "ENGLISH" -> state.question.expertAdviceEnglish
                "BILINGUAL" -> "${state.question.expertAdviceHindi}\n${state.question.expertAdviceEnglish}"
                else -> state.question.expertAdviceHindi
            }
            val (bonusActive, adjustedTime) = cancelBonusOnLifelineUsage(state)

            _uiState.value = state.copy(
                lifelineState = updatedLifelines,
                isExpertLoading = false,
                expertDialogContent = fallback, // Instant offline visual guidance, NO voice TTS
                hasBonusTime = bonusActive,
                accumulatedBonusSeconds = 0,
                lifelineUsedInCurrentQuestion = true,
                timeRemainingSeconds = adjustedTime,
                bonusLostNotice = state.hasBonusTime && !bonusActive
            )
        }
    }

    fun dismissExpertDialog() {
        val state = _uiState.value
        if (state is QuizUiState.InGame) {
            _uiState.value = state.copy(expertDialogContent = null, isExpertLoading = false)
        }
    }

    fun useFlipQuestion() {
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn && state.lifelineState.isFlipAvailable && !state.lifelineState.isFlipExhausted) {
            soundPlayer.playLifelineFlip()
            currentSessionLifelinesUsed++
            currentConsecutiveBonusSeconds = 0 // Break progression immediately

            val currentQId = state.question.id
            flippedQuestionIds.add(currentQId)

            viewModelScope.launch {
                repository.markQuestionFlipped(currentQId)
            }

            val updatedLifelines = state.lifelineState.copy(
                isFlipAvailable = false,
                isFlipExhausted = true
            )

            // Instantly replace question from procedural engine
            viewModelScope.launch {
                val profile = userProfile.value
                val replacement = repository.getQuestionForTier(
                    qNumber = state.currentQNumber,
                    userProfile = profile,
                    difficultyMultiplier = 1.0f + (state.currentQNumber * 0.05f),
                    flippedQuestionIds = flippedQuestionIds,
                    isCurrentAffairsSlot = sessionCurrentAffairsSlots.contains(state.currentQNumber)
                )
                sessionLadder[state.currentQNumber] = replacement

                loadPreloadedQuestionForTier(
                    targetQNum = state.currentQNumber,
                    accumulatedPoints = state.currentPointsWon,
                    guaranteedPoints = state.guaranteedSecuredPoints,
                    lifelines = updatedLifelines,
                    isFlipReplacement = true
                )
            }
        }
    }

    fun usePowerPaplu(rechargeTarget: String) {
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn && state.lifelineState.isPowerPapluAvailable && !state.lifelineState.isPowerPapluExhausted) {
            soundPlayer.playPowerPapluRecharge()
            currentSessionLifelinesUsed++

            var lState = state.lifelineState.copy(
                isPowerPapluAvailable = false,
                isPowerPapluExhausted = true,
                rechargedLifelineName = rechargeTarget
            )

            when (rechargeTarget) {
                "50-50" -> lState = lState.copy(is5050Available = true, is5050Exhausted = false)
                "Ask the Expert" -> lState = lState.copy(isExpertAvailable = true, isExpertExhausted = false)
                "Flip the Question" -> lState = lState.copy(isFlipAvailable = true, isFlipExhausted = false)
            }

            val (bonusActive, adjustedTime) = cancelBonusOnLifelineUsage(state)

            _uiState.value = state.copy(
                lifelineState = lState,
                hasBonusTime = bonusActive,
                accumulatedBonusSeconds = 0,
                lifelineUsedInCurrentQuestion = true,
                timeRemainingSeconds = adjustedTime,
                bonusLostNotice = state.hasBonusTime && !bonusActive
            )
        }
    }

    fun toggleLadderDrawer(isOpen: Boolean) {
        val state = _uiState.value
        if (state is QuizUiState.InGame) {
            _uiState.value = state.copy(isLadderDrawerOpen = isOpen)
        }
    }

    fun dismissCheckpointFanfare() {
        val state = _uiState.value
        if (state is QuizUiState.InGame) {
            _uiState.value = state.copy(showCheckpointFanfare = null)
        }
    }

    fun playQuestionAudio() {
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.question.audioPatternType != null) {
            soundPlayer.playRhythmPattern(state.question.audioPatternType)
        }
    }

    fun toggleVoiceNarration() {
        _isVoiceNarrationEnabled.value = !_isVoiceNarrationEnabled.value
        if (!_isVoiceNarrationEnabled.value) {
            speechNarrator.stop()
        } else {
            speakCurrentQuestionBounded()
        }
    }

    /**
     * Reads current question bounded to <= 5.0 seconds.
     */
    fun speakCurrentQuestionBounded() {
        val state = _uiState.value
        if (state is QuizUiState.InGame) {
            val langMode = languageMode.value
            val qText = when (langMode) {
                "ENGLISH" -> state.question.questionEnglish
                "BILINGUAL" -> "${state.question.questionHindi}\n${state.question.questionEnglish}"
                else -> state.question.questionHindi
            }
            speechNarrator.speakQuestionBounded(qText, langMode, userProfile.value.hostGender)
        }
    }

    /**
     * Reads options within running game timer.
     * Does not pause/reset/extend game timer!
     */
    fun readOptionsInGameTimer() {
        val state = _uiState.value
        if (state is QuizUiState.InGame && state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn) {
            val langMode = languageMode.value
            val opts = if (langMode == "ENGLISH") state.question.optionsEnglish else state.question.optionsHindi
            val prefix = if (langMode == "ENGLISH") "Option" else "विकल्प"
            val text = opts.mapIndexed { i, opt -> "$prefix ${('A' + i)}: $opt" }.joinToString(". ")
            speechNarrator.speakOptionInGameTimer(text, langMode)
        }
    }

    fun stopVoice() {
        speechNarrator.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        speechNarrator.shutdown()
    }
}
