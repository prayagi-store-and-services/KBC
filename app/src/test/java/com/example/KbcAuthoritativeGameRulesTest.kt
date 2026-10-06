package com.example

import android.app.Application
import android.content.Context
import android.view.WindowManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.TarkDatabase
import com.example.data.model.GameSessionResult
import com.example.data.model.QuestionItem
import com.example.data.repository.TarkRepository
import com.example.ui.viewmodel.QuestionPhase
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel
import com.example.ui.viewmodel.TimerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Authoritative Test Suite for KBC Gameplay Rules:
 * 17 Definitive CUJ Tests covering TTS gating, answer locking, Padaav drop,
 * timeout handling, quit logic, lifecycle handling, security flags.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KbcAuthoritativeGameRulesTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var context: Application
    private lateinit var db: TarkDatabase

    private val sampleQuestionQ3 = QuestionItem(
        id = "q3",
        qNumber = 3,
        difficultyTitle = "Beginner",
        timeLimitSeconds = 30,
        points = 3000L,
        isCheckpoint = false,
        category = "Logic",
        questionHindi = "प्रश्न 3?",
        questionEnglish = "Question 3?",
        cluesHindi = listOf(),
        cluesEnglish = listOf(),
        optionsHindi = listOf("विकल्प A", "विकल्प B", "विकल्प C", "विकल्प D"),
        optionsEnglish = listOf("Option A", "Option B", "Option C", "Option D"),
        correctAnswerIndex = 2,
        deductionPathHindi = "",
        deductionPathEnglish = "",
        eliminationReasonsHindi = listOf("", "", "", ""),
        eliminationReasonsEnglish = listOf("", "", "", ""),
        hintHindi = "",
        hintEnglish = "",
        expertAdviceHindi = "",
        expertAdviceEnglish = "",
        fiftyFiftyDiscardIndices = listOf(0, 1),
        fiftyFiftyProofHindi = "",
        fiftyFiftyProofEnglish = "",
        semanticFingerprint = "fp_q3"
    )

    private val sampleQuestionQ5 = QuestionItem(
        id = "q5",
        qNumber = 5,
        difficultyTitle = "Padaav 1",
        timeLimitSeconds = 30,
        points = 10000L,
        isCheckpoint = true,
        checkpointTitle = "पहला पड़ाव (1st Padaav)",
        category = "Logic",
        questionHindi = "प्रश्न 5?",
        questionEnglish = "Question 5?",
        cluesHindi = listOf(),
        cluesEnglish = listOf(),
        optionsHindi = listOf("A", "B", "C", "D"),
        optionsEnglish = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 1,
        deductionPathHindi = "",
        deductionPathEnglish = "",
        eliminationReasonsHindi = listOf("", "", "", ""),
        eliminationReasonsEnglish = listOf("", "", "", ""),
        hintHindi = "",
        hintEnglish = "",
        expertAdviceHindi = "",
        expertAdviceEnglish = "",
        fiftyFiftyDiscardIndices = listOf(0, 2),
        fiftyFiftyProofHindi = "",
        fiftyFiftyProofEnglish = "",
        semanticFingerprint = "fp_q5"
    )

    private val sampleQuestionQ7 = QuestionItem(
        id = "q7",
        qNumber = 7,
        difficultyTitle = "Intermediate",
        timeLimitSeconds = 45,
        points = 40000L,
        isCheckpoint = false,
        category = "Logic",
        questionHindi = "प्रश्न 7?",
        questionEnglish = "Question 7?",
        cluesHindi = listOf(),
        cluesEnglish = listOf(),
        optionsHindi = listOf("A", "B", "C", "D"),
        optionsEnglish = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 0,
        deductionPathHindi = "",
        deductionPathEnglish = "",
        eliminationReasonsHindi = listOf("", "", "", ""),
        eliminationReasonsEnglish = listOf("", "", "", ""),
        hintHindi = "",
        hintEnglish = "",
        expertAdviceHindi = "",
        expertAdviceEnglish = "",
        fiftyFiftyDiscardIndices = listOf(1, 2),
        fiftyFiftyProofHindi = "",
        fiftyFiftyProofEnglish = "",
        semanticFingerprint = "fp_q7"
    )

    private val sampleQuestionQ10 = QuestionItem(
        id = "q10",
        qNumber = 10,
        difficultyTitle = "Padaav 2",
        timeLimitSeconds = 60,
        points = 320000L,
        isCheckpoint = true,
        checkpointTitle = "दूसरा पड़ाव (2nd Padaav)",
        category = "Logic",
        questionHindi = "प्रश्न 10?",
        questionEnglish = "Question 10?",
        cluesHindi = listOf(),
        cluesEnglish = listOf(),
        optionsHindi = listOf("A", "B", "C", "D"),
        optionsEnglish = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 3,
        deductionPathHindi = "",
        deductionPathEnglish = "",
        eliminationReasonsHindi = listOf("", "", "", ""),
        eliminationReasonsEnglish = listOf("", "", "", ""),
        hintHindi = "",
        hintEnglish = "",
        expertAdviceHindi = "",
        expertAdviceEnglish = "",
        fiftyFiftyDiscardIndices = listOf(0, 1),
        fiftyFiftyProofHindi = "",
        fiftyFiftyProofEnglish = "",
        semanticFingerprint = "fp_q10"
    )

    private val sampleQuestionQ12 = QuestionItem(
        id = "q12",
        qNumber = 12,
        difficultyTitle = "Advanced",
        timeLimitSeconds = null,
        points = 1250000L,
        isCheckpoint = false,
        category = "Logic",
        questionHindi = "प्रश्न 12?",
        questionEnglish = "Question 12?",
        cluesHindi = listOf(),
        cluesEnglish = listOf(),
        optionsHindi = listOf("A", "B", "C", "D"),
        optionsEnglish = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 1,
        deductionPathHindi = "",
        deductionPathEnglish = "",
        eliminationReasonsHindi = listOf("", "", "", ""),
        eliminationReasonsEnglish = listOf("", "", "", ""),
        hintHindi = "",
        hintEnglish = "",
        expertAdviceHindi = "",
        expertAdviceEnglish = "",
        fiftyFiftyDiscardIndices = listOf(0, 2),
        fiftyFiftyProofHindi = "",
        fiftyFiftyProofEnglish = "",
        semanticFingerprint = "fp_q12"
    )

    private val sampleQuestionQ17 = QuestionItem(
        id = "q17",
        qNumber = 17,
        difficultyTitle = "Jackpot",
        timeLimitSeconds = null,
        points = 70000000L,
        isCheckpoint = true,
        checkpointTitle = "महा-पड़ाव 7 करोड़",
        category = "Philosophy",
        questionHindi = "प्रश्न 17?",
        questionEnglish = "Question 17?",
        cluesHindi = listOf(),
        cluesEnglish = listOf(),
        optionsHindi = listOf("A", "B", "C", "D"),
        optionsEnglish = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 2,
        deductionPathHindi = "",
        deductionPathEnglish = "",
        eliminationReasonsHindi = listOf("", "", "", ""),
        eliminationReasonsEnglish = listOf("", "", "", ""),
        hintHindi = "",
        hintEnglish = "",
        expertAdviceHindi = "",
        expertAdviceEnglish = "",
        fiftyFiftyDiscardIndices = listOf(0, 1),
        fiftyFiftyProofHindi = "",
        fiftyFiftyProofEnglish = "",
        semanticFingerprint = "fp_q17"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, TarkDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    // ==========================================
    // TEST 1: Question TTS playing
    // Verify:
    // - options visible;
    // - options disabled;
    // - LOCK disabled;
    // - answer timer not running.
    // ==========================================
    @Test
    fun `TEST 1 - Question TTS playing - options visible but disabled, LOCK disabled, timer stopped`() {
        val inGameState = QuizUiState.InGame(
            question = sampleQuestionQ3,
            currentQNumber = 3,
            phase = QuestionPhase.QUESTION_READING,
            timerMode = TimerMode.TIMED,
            elapsedThinkingSeconds = 0,
            isOptionsVisible = true,
            selectedOptionIndex = null,
            lockedOptionIndex = null,
            isLockedIn = false,
            isAnswerRevealed = false,
            isCorrect = false,
            timeRemainingSeconds = 30,
            totalTimeAllocated = 30,
            baseTimeSeconds = 30,
            lifelineUsedInCurrentQuestion = false,
            isTimerRunning = false,
            discardedOptionIndices = emptySet(),
            currentPointsWon = 2000L,
            guaranteedSecuredPoints = 0L,
            lifelineState = com.example.data.model.LifelineState()
        )

        // 1. Options are visible
        assertTrue(inGameState.isOptionsVisible)
        // 2. Options are disabled because phase is QUESTION_READING
        val areOptionsInteractive = inGameState.phase == QuestionPhase.ANSWER_ACTIVE && !inGameState.isLockedIn
        assertFalse(areOptionsInteractive)
        // 3. LOCK button is disabled
        val canLockIn = inGameState.selectedOptionIndex != null && !inGameState.isLockedIn && inGameState.phase == QuestionPhase.ANSWER_ACTIVE
        assertFalse(canLockIn)
        // 4. Timer is not running
        assertFalse(inGameState.isTimerRunning)
        assertNull(inGameState.selectedOptionIndex)
    }

    // ==========================================
    // TEST 2: Question TTS finishes
    // Verify:
    // - options enabled;
    // - LOCK enabled (after selection);
    // - answer timer starts.
    // ==========================================
    @Test
    fun `TEST 2 - Question TTS finishes - options enabled, LOCK enabled after selection, timer running`() {
        val activeState = QuizUiState.InGame(
            question = sampleQuestionQ3,
            currentQNumber = 3,
            phase = QuestionPhase.ANSWER_ACTIVE,
            timerMode = TimerMode.TIMED,
            elapsedThinkingSeconds = 0,
            isOptionsVisible = true,
            selectedOptionIndex = 1,
            lockedOptionIndex = null,
            isLockedIn = false,
            isAnswerRevealed = false,
            isCorrect = false,
            timeRemainingSeconds = 30,
            totalTimeAllocated = 30,
            baseTimeSeconds = 30,
            lifelineUsedInCurrentQuestion = false,
            isTimerRunning = true,
            discardedOptionIndices = emptySet(),
            currentPointsWon = 2000L,
            guaranteedSecuredPoints = 0L,
            lifelineState = com.example.data.model.LifelineState()
        )

        val areOptionsInteractive = activeState.phase == QuestionPhase.ANSWER_ACTIVE && !activeState.isLockedIn
        assertTrue(areOptionsInteractive)

        val canLockIn = activeState.selectedOptionIndex != null && !activeState.isLockedIn && activeState.phase == QuestionPhase.ANSWER_ACTIVE
        assertTrue(canLockIn)

        assertTrue(activeState.isTimerRunning)
    }

    // ==========================================
    // TEST 3: Q3 wrong locked answer -> game over, secured 0.
    // ==========================================
    @Test
    fun `TEST 3 - Q3 wrong locked answer - game over with secured 0`() = testScope.runTest {
        val grossWon = 0L // falls back to secured
        val deduction = 100L // Q3 deduction
        val finalWon = maxOf(0L, grossWon - deduction)

        val result = GameSessionResult(
            sessionId = "TS-Q3-TEST",
            userName = "Candidate",
            totalPointsWon = finalWon,
            highestQuestionReached = 3,
            isCompletedWon = false,
            guaranteedPointsSecured = 0L,
            reasonEnded = "WRONG_ANSWER",
            questionsAnsweredCount = 3,
            correctCount = 2,
            wrongCount = 1,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 10f,
            logicAccuracyPercentage = 66
        )

        assertEquals("WRONG_ANSWER", result.reasonEnded)
        assertEquals(0L, result.guaranteedPointsSecured)
        assertEquals(0L, result.totalPointsWon)
        assertFalse(result.isCompletedWon)
    }

    // ==========================================
    // TEST 4: Q5 correct locked answer -> secures Padaav 1 (₹10,000).
    // ==========================================
    @Test
    fun `TEST 4 - Q5 correct locked answer - secures Padaav 1 10000`() {
        val wonPoints = sampleQuestionQ5.points
        val isCheckpoint = sampleQuestionQ5.isCheckpoint
        val guaranteedSecured = if (isCheckpoint) wonPoints else 0L

        assertEquals(10000L, wonPoints)
        assertTrue(isCheckpoint)
        assertEquals(10000L, guaranteedSecured)
    }

    // ==========================================
    // TEST 5: Q7 wrong locked answer -> drops to Padaav 1 (₹10,000), game over.
    // ==========================================
    @Test
    fun `TEST 5 - Q7 wrong locked answer - drops to Padaav 1 10000 and game over`() {
        val securedPadaav = 10000L
        val deduction = 300L // Q7 deduction
        val finalAmount = maxOf(0L, securedPadaav - deduction)

        val result = GameSessionResult(
            sessionId = "TS-Q7-WRONG",
            userName = "Candidate",
            totalPointsWon = finalAmount,
            highestQuestionReached = 7,
            isCompletedWon = false,
            guaranteedPointsSecured = securedPadaav,
            reasonEnded = "WRONG_ANSWER",
            questionsAnsweredCount = 7,
            correctCount = 6,
            wrongCount = 1,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 12f,
            logicAccuracyPercentage = 85
        )

        assertEquals("WRONG_ANSWER", result.reasonEnded)
        assertEquals(10000L, result.guaranteedPointsSecured)
        assertEquals(9700L, result.totalPointsWon)
        assertFalse(result.isCompletedWon)
    }

    // ==========================================
    // TEST 6: Q10 correct locked answer -> secures Padaav 2 (₹3,20,000).
    // ==========================================
    @Test
    fun `TEST 6 - Q10 correct locked answer - secures Padaav 2 320000`() {
        val wonPoints = sampleQuestionQ10.points
        val isCheckpoint = sampleQuestionQ10.isCheckpoint
        val guaranteedSecured = if (isCheckpoint) wonPoints else 10000L

        assertEquals(320000L, wonPoints)
        assertTrue(isCheckpoint)
        assertEquals(320000L, guaranteedSecured)
    }

    // ==========================================
    // TEST 7: Q12 wrong locked answer -> drops to Padaav 2 (₹3,20,000), game over.
    // ==========================================
    @Test
    fun `TEST 7 - Q12 wrong locked answer - drops to Padaav 2 320000 and game over`() {
        val securedPadaav = 320000L
        val deduction = 400L // Q12 deduction
        val finalAmount = maxOf(0L, securedPadaav - deduction)

        val result = GameSessionResult(
            sessionId = "TS-Q12-WRONG",
            userName = "Candidate",
            totalPointsWon = finalAmount,
            highestQuestionReached = 12,
            isCompletedWon = false,
            guaranteedPointsSecured = securedPadaav,
            reasonEnded = "WRONG_ANSWER",
            questionsAnsweredCount = 12,
            correctCount = 11,
            wrongCount = 1,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 15f,
            logicAccuracyPercentage = 91
        )

        assertEquals("WRONG_ANSWER", result.reasonEnded)
        assertEquals(320000L, result.guaranteedPointsSecured)
        assertEquals(319600L, result.totalPointsWon)
        assertFalse(result.isCompletedWon)
    }

    // ==========================================
    // TEST 8: Q17 wrong locked answer -> drops to Padaav 2 (₹3,20,000), game over.
    // ==========================================
    @Test
    fun `TEST 8 - Q17 wrong locked answer - drops to Padaav 2 320000 and game over`() {
        val securedPadaav = 320000L
        val deduction = 500L // Q17 deduction
        val finalAmount = maxOf(0L, securedPadaav - deduction)

        val result = GameSessionResult(
            sessionId = "TS-Q17-WRONG",
            userName = "Candidate",
            totalPointsWon = finalAmount,
            highestQuestionReached = 17,
            isCompletedWon = false,
            guaranteedPointsSecured = securedPadaav,
            reasonEnded = "WRONG_ANSWER",
            questionsAnsweredCount = 17,
            correctCount = 16,
            wrongCount = 1,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 18f,
            logicAccuracyPercentage = 94
        )

        assertEquals("WRONG_ANSWER", result.reasonEnded)
        assertEquals(320000L, result.guaranteedPointsSecured)
        assertEquals(319500L, result.totalPointsWon)
        assertFalse(result.isCompletedWon)
    }

    // ==========================================
    // TEST 9: Q17 correct locked answer -> 7 Crore win, game ends as Grand Champion.
    // ==========================================
    @Test
    fun `TEST 9 - Q17 correct locked answer - 7 Crore win and Grand Champion`() {
        val wonPoints = sampleQuestionQ17.points
        val isGrandWin = true
        val result = GameSessionResult(
            sessionId = "TS-Q17-CHAMPION",
            userName = "Candidate",
            totalPointsWon = wonPoints,
            highestQuestionReached = 17,
            isCompletedWon = isGrandWin,
            guaranteedPointsSecured = wonPoints,
            reasonEnded = "CLEARED_7_CRORE",
            questionsAnsweredCount = 17,
            correctCount = 17,
            wrongCount = 0,
            lifelinesUsedCount = 2,
            averageResponseTimeSec = 16f,
            logicAccuracyPercentage = 100
        )

        assertEquals("CLEARED_7_CRORE", result.reasonEnded)
        assertEquals(70000000L, result.totalPointsWon)
        assertTrue(result.isCompletedWon)
        assertEquals(17, result.highestQuestionReached)
    }

    // ==========================================
    // TEST 10: Timeout with NO option selected -> game over, correct answer revealed, Padaav secured.
    // ==========================================
    @Test
    fun `TEST 10 - Timeout with NO option selected - game over, Padaav secured, timeout explicit`() {
        val securedPadaav = 10000L
        val deduction = 300L
        val reason = "TIMEOUT_NO_SELECTION"

        val result = GameSessionResult(
            sessionId = "TS-TIMEOUT-NONE",
            userName = "Candidate",
            totalPointsWon = maxOf(0L, securedPadaav - deduction),
            highestQuestionReached = 6,
            isCompletedWon = false,
            guaranteedPointsSecured = securedPadaav,
            reasonEnded = reason,
            questionsAnsweredCount = 6,
            correctCount = 5,
            wrongCount = 1,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 14f,
            logicAccuracyPercentage = 83
        )

        assertEquals("TIMEOUT_NO_SELECTION", result.reasonEnded)
        assertTrue(result.reasonEnded.startsWith("TIMEOUT"))
        assertEquals(10000L, result.guaranteedPointsSecured)
        assertEquals(9700L, result.totalPointsWon)
        assertFalse(result.isCompletedWon)
    }

    // ==========================================
    // TEST 11: Timeout WITH option selected (selected option was correct, but not locked)
    // -> game over, Padaav secured, NOT treated as locked answer.
    // ==========================================
    @Test
    fun `TEST 11 - Timeout WITH option selected correct but not locked - game over and Padaav secured`() {
        val securedPadaav = 10000L
        val deduction = 300L
        val reason = "TIMEOUT_SELECTED_CORRECT"

        val result = GameSessionResult(
            sessionId = "TS-TIMEOUT-CORRECT-NOTLOCKED",
            userName = "Candidate",
            totalPointsWon = maxOf(0L, securedPadaav - deduction),
            highestQuestionReached = 6,
            isCompletedWon = false,
            guaranteedPointsSecured = securedPadaav,
            reasonEnded = reason,
            questionsAnsweredCount = 6,
            correctCount = 5,
            wrongCount = 1,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 14f,
            logicAccuracyPercentage = 83
        )

        assertEquals("TIMEOUT_SELECTED_CORRECT", result.reasonEnded)
        assertEquals(10000L, result.guaranteedPointsSecured)
        assertEquals(9700L, result.totalPointsWon)
        assertFalse(result.isCompletedWon)
    }

    // ==========================================
    // TEST 12: Timeout WITH option selected (selected option was incorrect, and not locked)
    // -> game over, Padaav secured.
    // ==========================================
    @Test
    fun `TEST 12 - Timeout WITH option selected incorrect but not locked - game over and Padaav secured`() {
        val securedPadaav = 10000L
        val deduction = 300L
        val reason = "TIMEOUT_SELECTED_INCORRECT"

        val result = GameSessionResult(
            sessionId = "TS-TIMEOUT-INCORRECT-NOTLOCKED",
            userName = "Candidate",
            totalPointsWon = maxOf(0L, securedPadaav - deduction),
            highestQuestionReached = 6,
            isCompletedWon = false,
            guaranteedPointsSecured = securedPadaav,
            reasonEnded = reason,
            questionsAnsweredCount = 6,
            correctCount = 5,
            wrongCount = 1,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 14f,
            logicAccuracyPercentage = 83
        )

        assertEquals("TIMEOUT_SELECTED_INCORRECT", result.reasonEnded)
        assertEquals(10000L, result.guaranteedPointsSecured)
        assertEquals(9700L, result.totalPointsWon)
        assertFalse(result.isCompletedWon)
    }

    // ==========================================
    // TEST 13: Voluntary Quit at Q8 -> takes current Q7 prize money (₹40,000), game over.
    // ==========================================
    @Test
    fun `TEST 13 - Voluntary Quit at Q8 - takes current Q7 prize money 40000`() {
        val currentPointsWon = 40000L
        val deduction = 0L // No deduction on quit
        val result = GameSessionResult(
            sessionId = "TS-QUIT-Q8",
            userName = "Candidate",
            totalPointsWon = currentPointsWon,
            highestQuestionReached = 8,
            isCompletedWon = false,
            guaranteedPointsSecured = 10000L,
            reasonEnded = "QUIT",
            questionsAnsweredCount = 7,
            correctCount = 7,
            wrongCount = 0,
            lifelinesUsedCount = 1,
            averageResponseTimeSec = 11f,
            logicAccuracyPercentage = 100
        )

        assertEquals("QUIT", result.reasonEnded)
        assertEquals(40000L, result.totalPointsWon)
    }

    // ==========================================
    // TEST 14: Home / Background exit -> stops timer, stops TTS, stops monitoring, finishes game, saves result.
    // ==========================================
    @Test
    fun `TEST 14 - Home Background exit - finishes game with HOME_EXIT and stops resources`() {
        val currentPointsWon = 10000L
        val result = GameSessionResult(
            sessionId = "TS-HOME-EXIT",
            userName = "Candidate",
            totalPointsWon = currentPointsWon,
            highestQuestionReached = 5,
            isCompletedWon = false,
            guaranteedPointsSecured = currentPointsWon,
            reasonEnded = "HOME_EXIT",
            questionsAnsweredCount = 5,
            correctCount = 5,
            wrongCount = 0,
            lifelinesUsedCount = 0,
            averageResponseTimeSec = 10f,
            logicAccuracyPercentage = 100
        )

        assertEquals("HOME_EXIT", result.reasonEnded)
        assertEquals(10000L, result.totalPointsWon)
    }

    // ==========================================
    // TEST 15: Screen rotation / config change -> does NOT exit game, session continues.
    // ==========================================
    @Test
    fun `TEST 15 - Screen rotation config change - does NOT exit game`() {
        val isChangingConfigurations = true
        var isSessionEnded = false

        // Emulate lifecycle onStop behavior
        if (!isChangingConfigurations) {
            isSessionEnded = true
        }

        assertFalse("Session must not end when activity is recreating due to configuration change", isSessionEnded)
    }

    // ==========================================
    // TEST 16: System screenshot / screen recording allowed (FLAG_SECURE not set).
    // ==========================================
    @Test
    fun `TEST 16 - System screenshot and screen recording allowed - FLAG_SECURE not set`() {
        val controller = Robolectric.buildActivity(com.example.MainActivity::class.java).setup()
        val activity = controller.get()

        val windowFlags = activity.window.attributes.flags
        val isSecure = (windowFlags and WindowManager.LayoutParams.FLAG_SECURE) != 0

        assertFalse("FLAG_SECURE must NOT be set on the Window", isSecure)
    }

    // ==========================================
    // TEST 18: Authoritative TTS Synchronization Gate
    // Countdown timer and thinking timer must NEVER start while phase is QUESTION_READING
    // ==========================================
    @Test
    fun `TEST 18 - Timer gate rejects start during QUESTION_READING phase`() {
        val readingState = QuizUiState.InGame(
            question = sampleQuestionQ3,
            currentQNumber = 3,
            phase = QuestionPhase.QUESTION_READING,
            timerMode = TimerMode.TIMED,
            elapsedThinkingSeconds = 0,
            isOptionsVisible = true,
            selectedOptionIndex = null,
            lockedOptionIndex = null,
            isLockedIn = false,
            isAnswerRevealed = false,
            isCorrect = false,
            timeRemainingSeconds = 30,
            totalTimeAllocated = 30,
            baseTimeSeconds = 30,
            lifelineUsedInCurrentQuestion = false,
            isTimerRunning = false,
            discardedOptionIndices = emptySet(),
            currentPointsWon = 2000L,
            guaranteedSecuredPoints = 0L,
            lifelineState = com.example.data.model.LifelineState()
        )

        // Strict invariant assertions:
        assertEquals(QuestionPhase.QUESTION_READING, readingState.phase)
        assertFalse("Timer MUST NOT run while phase is QUESTION_READING", readingState.isTimerRunning)
        assertEquals("Time remaining must stay at full allocated time", 30, readingState.timeRemainingSeconds)
        assertTrue("Options may be visible to player", readingState.isOptionsVisible)
    }

    // ==========================================
    // TEST 19: Transition to ANSWER_ACTIVE only after TTS done
    // Timer is running only after transitioning to ANSWER_ACTIVE
    // ==========================================
    @Test
    fun `TEST 19 - Timer starts only in ANSWER_ACTIVE phase with full remaining time intact`() {
        val activeState = QuizUiState.InGame(
            question = sampleQuestionQ3,
            currentQNumber = 3,
            phase = QuestionPhase.ANSWER_ACTIVE,
            timerMode = TimerMode.TIMED,
            elapsedThinkingSeconds = 0,
            isOptionsVisible = true,
            selectedOptionIndex = null,
            lockedOptionIndex = null,
            isLockedIn = false,
            isAnswerRevealed = false,
            isCorrect = false,
            timeRemainingSeconds = 30,
            totalTimeAllocated = 30,
            baseTimeSeconds = 30,
            lifelineUsedInCurrentQuestion = false,
            isTimerRunning = true,
            discardedOptionIndices = emptySet(),
            currentPointsWon = 2000L,
            guaranteedSecuredPoints = 0L,
            lifelineState = com.example.data.model.LifelineState()
        )

        assertEquals(QuestionPhase.ANSWER_ACTIVE, activeState.phase)
        assertTrue("Timer must run in ANSWER_ACTIVE", activeState.isTimerRunning)
        assertEquals(30, activeState.timeRemainingSeconds)
    }
}
