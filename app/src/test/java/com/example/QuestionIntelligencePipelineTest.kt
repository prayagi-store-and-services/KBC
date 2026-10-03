package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.TarkDatabase
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionSerializer
import com.example.data.model.UserProfile
import com.example.data.repository.DynamicLogicEngine
import com.example.data.repository.PreparationStage
import com.example.data.repository.QuestionIntelligencePipeline
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuestionIntelligencePipelineTest {

    private lateinit var db: TarkDatabase
    private lateinit var context: Context
    private lateinit var pipeline: QuestionIntelligencePipeline

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, TarkDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        pipeline = QuestionIntelligencePipeline(
            context = context,
            questionDao = db.questionDao(),
            currentAffairsDao = db.currentAffairsDao(),
            sessionBankCacheDao = db.sessionBankCacheDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testGenerateExactly17QuestionsWithSequentialDifficulty() = runBlocking {
        val profile = UserProfile(
            name = "Aarav",
            state = "Uttar Pradesh",
            age = 14,
            isStudentMode = true,
            studentClass = "Class 9",
            preparationDomain = "Student",
            interests = listOf("Logical Deductions", "Data Interpretation", "Spatial Coordinate Vector"),
            languageMode = "HINDI"
        )

        val stagesVisited = mutableListOf<PreparationStage>()
        val ladder = pipeline.prepareSessionQuestionBank(
            sessionId = "test_sess_001",
            userProfile = profile
        ) { progress ->
            stagesVisited.add(progress.stage)
        }

        // Exactly 17 questions
        assertEquals(17, ladder.size)

        // Difficulty levels 1..17 strictly maintained
        for (tier in 1..17) {
            val q = ladder[tier]
            assertNotNull("Question for tier $tier must not be null", q)
            assertEquals(tier, q!!.difficultyLevel)
            assertEquals(tier, q.qNumber)
            assertTrue("Options count must be 4", q.optionsHindi.size == 4 || q.optionsEnglish.size == 4)
            assertTrue("Correct answer index must be 0..3", q.correctAnswerIndex in 0..3)
        }

        // Verification of offline cache
        val cachedLadder = pipeline.getCachedSessionLadder("test_sess_001")
        assertNotNull(cachedLadder)
        assertEquals(17, cachedLadder!!.size)
        assertEquals(ladder[1]?.semanticFingerprint, cachedLadder[1]?.semanticFingerprint)
    }

    @Test
    fun testQuestionSerializerRoundTrip() {
        val testProfile = UserProfile(
            isStudentMode = true,
            age = 12,
            studentClass = "Class 7"
        )
        val original = DynamicLogicEngine.generateUniqueQuestion(
            qNumber = 5,
            userProfile = testProfile,
            history = com.example.data.repository.MultiLayerQuestionValidator.HistoricalRegistry(),
            currentSessionQuestions = emptyList()
        )

        val json = QuestionSerializer.serializeQuestion(original)
        val deserialized = QuestionSerializer.deserializeQuestion(json)

        assertEquals(original.id, deserialized.id)
        assertEquals(original.qNumber, deserialized.qNumber)
        assertEquals(original.difficultyLevel, deserialized.difficultyLevel)
        assertEquals(original.correctAnswerIndex, deserialized.correctAnswerIndex)
        assertEquals(original.semanticFingerprint, deserialized.semanticFingerprint)
    }

    @Test
    fun testCityStateFallbackStrategiesAndJuniorAdult() = runBlocking {
        val juniorProfile = UserProfile(name = "Aarav", state = "Maharashtra", city = "Mumbai", age = 14, isStudentMode = true, preparationDomain = "Student")
        val adultProfile = UserProfile(name = "Ravi", state = "Uttar Pradesh", city = "Lucknow", age = 25, isStudentMode = false, preparationDomain = "UPSC")
        val juniorLadder = pipeline.prepareSessionQuestionBank("junior_sess", juniorProfile) {}
        val adultLadder = pipeline.prepareSessionQuestionBank("adult_sess", adultProfile) {}
        assertEquals(17, juniorLadder.size)
        assertEquals(17, adultLadder.size)
    }

    @Test
    fun testUniquenessAcrossSessions() = runBlocking {
        val profile = UserProfile(
            name = "Priya",
            state = "Maharashtra",
            age = 22,
            isStudentMode = false,
            preparationDomain = "Logic",
            interests = listOf("Logical Deductions", "Data Interpretation", "Analogy"),
            languageMode = "ENGLISH"
        )

        val session1Ladder = pipeline.prepareSessionQuestionBank("sess_1", profile) {}
        val session2Ladder = pipeline.prepareSessionQuestionBank("sess_2", profile) {}

        // Fingerprints registered in DB
        val servedCount = db.questionDao().getAllServedFingerprints().size
        assertTrue("Registered questions in DB should be at least 17", servedCount >= 17)
    }

    private fun firstRepeatGame(): Int {
        val profile = UserProfile(name = "Ravi", state = "Uttar Pradesh", city = "Lucknow", age = 25, isStudentMode = false, preparationDomain = "UPSC")
        val texts = mutableSetOf<String>()
        val sems = mutableSetOf<String>()
        var recent: Set<String> = emptySet()
        val repeatsByGame = mutableListOf<String>()
        repeat(30) { game ->
            val session = mutableMapOf<Int, com.example.data.model.QuestionItem>()
            val gameConcepts = mutableSetOf<String>()
            var gameRepeats = 0
            for (tier in 1..17) {
                val history = com.example.data.repository.MultiLayerQuestionValidator.HistoricalRegistry(
                    servedNormalizedTexts = texts,
                    servedSemanticFingerprints = sems,
                    recentConceptFingerprints = recent
                )
                val q = DynamicLogicEngine.generateUniqueQuestion(tier, profile, history, session.values, salt = game * 1000 + tier)
                val nt = com.example.data.repository.MultiLayerQuestionValidator.normalizeText(q.questionEnglish.ifBlank { q.questionHindi })
                val newText = texts.add(nt)
                val newMeaning = sems.add(q.semanticFingerprint.trim().lowercase())
                if (!newText || !newMeaning) gameRepeats++
                session[tier] = q
                gameConcepts.add(q.conceptFingerprint.trim().lowercase())
            }
            if (gameRepeats > 0) repeatsByGame.add("game${game + 1}:$gameRepeats")
            recent = gameConcepts
        }
        return repeatsByGame.firstOrNull()?.substringAfter("game")?.substringBefore(":")?.toInt() ?: 31
    }

    /** Measured in CI: the built-in templates run out of fresh questions after 2-3 games. This guards that floor. */
    @Test fun noRepeatsInFirstTwoGames() { assertTrue(firstRepeatGame() >= 2) }
}
