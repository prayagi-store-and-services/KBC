package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural Audio Synthesizer for TarkShastra Quiz.
 * Generates:
 * - Dynamic Psychological Pressure Background Track (heartbeat bass pulse + escalating tension)
 * - Hot-seat lock-in suspense drone
 * - Triumphant checkpoint fanfare and victory chords
 * - Wrong answer buzzer & 50-50 elimination swoosh
 * - Audio analysis rhythm playback
 */
class SoundEffectsPlayer(private val context: Context? = null) {

    private val soundScope = CoroutineScope(Dispatchers.Default + Job())
    private var isMuted = false
    private var tensionMusicJob: Job? = null
    private val powerManager = context?.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val isPowerSaver = powerManager?.isPowerSaveMode == true

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stopTimerPressureMusic()
        }
    }

    fun isMuted(): Boolean = isMuted

    /**
     * Starts the psychological pressure background synthesizer track.
     * Gradually increases pulse rate, bass tension, and harmonic urgency
     * as remaining time decreases.
     */
    fun startTimerPressureMusic(getRemainingSeconds: () -> Int?, getTotalSeconds: () -> Int?) {
        if (isMuted || isPowerSaver) return // Muted or power save mode active (conserve battery)
        stopTimerPressureMusic()

        tensionMusicJob = soundScope.launch {
            while (isActive) {
                if (isMuted) break
                val remaining = getRemainingSeconds() ?: 30
                val total = getTotalSeconds() ?: 60
                val ratio = (remaining.toFloat() / total.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                // Urgency factor: 0.0 (full time) -> 1.0 (last seconds)
                val urgency = 1f - ratio

                // Pulse interval speeds up from 1000ms down to 350ms in final seconds
                val intervalMs = (1000 - (urgency * 650)).toLong().coerceIn(320L, 1000L)

                // Dynamic bass frequency and tension harmonic
                val bassFreq = 65.0 + (urgency * 45.0) // 65Hz -> 110Hz deep suspense pulse
                val clickFreq = 750.0 + (urgency * 400.0) // Subtle rhythmic tick click

                // 1. Bass Pulse
                generateTone(
                    frequency = bassFreq,
                    durationMs = (90 + (urgency * 40)).toInt(),
                    volume = (0.32f + (urgency * 0.28f)).coerceIn(0.2f, 0.65f)
                )

                // 2. High subtle accent tick
                delay(40)
                generateTone(
                    frequency = clickFreq,
                    durationMs = 25,
                    volume = (0.18f + (urgency * 0.30f)).coerceIn(0.1f, 0.5f)
                )

                // If in final 10 seconds, add secondary urgent counter-pulse
                if (remaining in 1..10) {
                    delay(intervalMs / 2)
                    generateTone(
                        frequency = bassFreq * 1.5,
                        durationMs = 35,
                        volume = 0.45f
                    )
                    delay((intervalMs / 2) - 40)
                } else {
                    delay(intervalMs - 40)
                }
            }
        }
    }

    fun stopTimerPressureMusic() {
        tensionMusicJob?.cancel()
        tensionMusicJob = null
    }

    fun stopAllMusic() {
        stopTimerPressureMusic()
    }

    /**
     * Starts the intellectual suspense & deep thinking ambient music for the Unlimited Phase (Q11-Q17).
     * Generates a slow, mystical harmonic drone pulse reflecting cognitive contemplation without countdown ticks.
     */
    fun startUnlimitedDeepThinkingMusic(getElapsedSeconds: () -> Int) {
        if (isMuted) return
        stopTimerPressureMusic()

        tensionMusicJob = soundScope.launch {
            val rootChords = listOf(110.0, 130.81, 146.83, 164.81) // A2, C3, D3, E3 deep ambient roots
            var chordIdx = 0

            while (isActive) {
                if (isMuted) break
                val elapsed = getElapsedSeconds()
                val root = rootChords[chordIdx % rootChords.size]
                chordIdx++

                // Deep ambient resonant swell
                generateTone(
                    frequency = root,
                    durationMs = 280,
                    volume = 0.30f
                )
                delay(120)
                generateTone(
                    frequency = root * 1.5, // Perfect 5th harmonic swell
                    durationMs = 220,
                    volume = 0.22f
                )

                // Calm intellectual pause between harmonic waves (2.2 seconds)
                delay(2200L)
            }
        }
    }

    fun playOptionSelected() {
        if (isMuted) return
        soundScope.launch {
            generateTone(frequency = 520.0, durationMs = 60, volume = 0.5f)
            delay(40)
            generateTone(frequency = 650.0, durationMs = 80, volume = 0.6f)
        }
    }

    fun playLockInSuspense() {
        stopTimerPressureMusic()
        if (isMuted) return
        soundScope.launch {
            // Suspense drone ramp
            for (i in 0 until 4) {
                generateTone(frequency = 320.0 + (i * 45.0), durationMs = 90, volume = 0.5f)
                delay(65)
            }
        }
    }

    fun playCorrectAnswer() {
        stopTimerPressureMusic()
        if (isMuted) return
        soundScope.launch {
            // Triumphant chord (C5 - E5 - G5 - C6)
            generateTone(frequency = 523.25, durationMs = 100, volume = 0.7f)
            delay(70)
            generateTone(frequency = 659.25, durationMs = 100, volume = 0.7f)
            delay(70)
            generateTone(frequency = 783.99, durationMs = 120, volume = 0.8f)
            delay(70)
            generateTone(frequency = 1046.50, durationMs = 300, volume = 0.9f)
        }
    }

    fun playWrongAnswer() {
        stopTimerPressureMusic()
        if (isMuted) return
        soundScope.launch {
            generateTone(frequency = 280.0, durationMs = 150, volume = 0.7f)
            delay(120)
            generateTone(frequency = 210.0, durationMs = 350, volume = 0.8f)
        }
    }

    fun playCheckpointFanfare() {
        stopTimerPressureMusic()
        if (isMuted) return
        soundScope.launch {
            val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
            for (note in notes) {
                generateTone(frequency = note, durationMs = 100, volume = 0.8f)
                delay(80)
            }
            generateTone(frequency = 1760.0, durationMs = 400, volume = 0.95f)
        }
    }

    fun playLifeline5050() {
        if (isMuted) return
        soundScope.launch {
            generateTone(frequency = 700.0, durationMs = 60, volume = 0.5f)
            delay(50)
            generateTone(frequency = 420.0, durationMs = 90, volume = 0.6f)
        }
    }

    fun playLifelineExpert() {
        if (isMuted) return
        soundScope.launch {
            generateTone(frequency = 587.33, durationMs = 80, volume = 0.6f)
            delay(70)
            generateTone(frequency = 880.00, durationMs = 160, volume = 0.7f)
        }
    }

    fun playLifelineFlip() {
        if (isMuted) return
        soundScope.launch {
            generateTone(frequency = 400.0, durationMs = 60, volume = 0.5f)
            delay(50)
            generateTone(frequency = 600.0, durationMs = 60, volume = 0.6f)
            delay(50)
            generateTone(frequency = 800.0, durationMs = 100, volume = 0.7f)
        }
    }

    fun playPowerPapluRecharge() {
        if (isMuted) return
        soundScope.launch {
            for (f in listOf(300.0, 450.0, 600.0, 750.0, 900.0, 1200.0)) {
                generateTone(frequency = f, durationMs = 50, volume = 0.7f)
                delay(40)
            }
        }
    }

    fun playRhythmPattern(patternType: String) {
        if (isMuted) return
        soundScope.launch {
            when (patternType) {
                "waltz_3_4" -> {
                    for (rep in 0 until 2) {
                        generateTone(frequency = 600.0, durationMs = 100, volume = 0.8f)
                        delay(350)
                        generateTone(frequency = 400.0, durationMs = 60, volume = 0.4f)
                        delay(350)
                        generateTone(frequency = 400.0, durationMs = 60, volume = 0.4f)
                        delay(350)
                    }
                }
                "syncopated_4_4" -> {
                    for (rep in 0 until 2) {
                        generateTone(frequency = 550.0, durationMs = 80, volume = 0.8f)
                        delay(280)
                        generateTone(frequency = 450.0, durationMs = 50, volume = 0.3f)
                        delay(140)
                        generateTone(frequency = 680.0, durationMs = 90, volume = 0.75f)
                        delay(280)
                        generateTone(frequency = 550.0, durationMs = 80, volume = 0.6f)
                        delay(280)
                    }
                }
                "harmonic_interval" -> {
                    generateTone(frequency = 330.0, durationMs = 300, volume = 0.6f)
                    delay(320)
                    generateTone(frequency = 495.0, durationMs = 300, volume = 0.7f)
                    delay(320)
                    generateTone(frequency = 660.0, durationMs = 400, volume = 0.8f)
                }
                else -> {
                    for (i in 0 until 4) {
                        val freq = if (i == 0) 700.0 else 500.0
                        val vol = if (i == 0) 0.8f else 0.4f
                        generateTone(frequency = freq, durationMs = 70, volume = vol)
                        delay(250)
                    }
                }
            }
        }
    }

    private fun generateTone(frequency: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            val samples = ShortArray(numSamples)
            val fadeSamples = (numSamples * 0.15).toInt().coerceAtLeast(1)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                var amplitude = sin(2.0 * Math.PI * frequency * time)

                // Envelope attack and decay to prevent audio clicks
                if (i < fadeSamples) {
                    amplitude *= (i.toDouble() / fadeSamples)
                } else if (i > numSamples - fadeSamples) {
                    amplitude *= ((numSamples - i).toDouble() / fadeSamples)
                }

                samples[i] = (amplitude * Short.MAX_VALUE * volume.coerceIn(0f, 1f)).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()

            // Release after playing
            soundScope.launch {
                delay((durationMs + 50).toLong())
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Fallback gracefully if hardware audio creation is unavailable
        }
    }
}
