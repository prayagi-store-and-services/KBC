package com.example

import com.example.data.model.UserProfile
import com.example.data.repository.DynamicLogicEngine
import com.example.data.repository.MultiLayerQuestionValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression for the 1 Nov "no repeated questions" roadmap item. Before the fix,
 * every generator's exhausted-pool fallback returned an unchecked question, so a
 * regular player eventually saw identical wording again. The promise is
 * text-level: the same question text must never be served twice.
 */
@RunWith(RobolectricTestRunner::class)
class QuestionUniquenessTest {

    private val profile = UserProfile(
        name = "Aarav",
        state = "Uttar Pradesh",
        age = 25,
        isStudentMode = false,
        preparationDomain = "SSC",
        languageMode = "ENGLISH"
    )

    @Test
    fun isTextRepeatDetectsHistoryAndSession() {
        val q = DynamicLogicEngine.generateUniqueQuestion(
            qNumber = 3, userProfile = profile,
            history = MultiLayerQuestionValidator.HistoricalRegistry(),
            currentSessionQuestions = emptyList(), salt = 42
        )
        val norm = MultiLayerQuestionValidator.normalizeText(q.questionEnglish.ifBlank { q.questionHindi })
        val none = MultiLayerQuestionValidator.HistoricalRegistry()
        assertFalse(MultiLayerQuestionValidator.isTextRepeat(q, none, emptyList()))
        val inHistory = MultiLayerQuestionValidator.HistoricalRegistry(servedNormalizedTexts = setOf(norm))
        assertTrue(MultiLayerQuestionValidator.isTextRepeat(q, inHistory, emptyList()))
        assertTrue(MultiLayerQuestionValidator.isTextRepeat(q, none, listOf(q)))
    }

    @Test
    fun servedQuestionsNeverRepeatTextAcrossManyGames() {
        val servedTexts = mutableSetOf<String>()
        val servedSem = mutableSetOf<String>()
        val servedLogic = mutableSetOf<String>()
        val servedConcept = mutableSetOf<String>()
        var salt = 12345
        // 340 draws = 20 full 17-question games, enough to exhaust the small
        // logic-fingerprint pools of several generators.
        repeat(340) { round ->
            val tier = round % 17 + 1
            val history = MultiLayerQuestionValidator.HistoricalRegistry(
                servedNormalizedTexts = servedTexts,
                servedSemanticFingerprints = servedSem,
                servedLogicFingerprints = servedLogic,
                servedConceptFingerprints = servedConcept
            )
            val q = DynamicLogicEngine.generateUniqueQuestion(
                qNumber = tier, userProfile = profile, history = history,
                currentSessionQuestions = emptyList(), salt = salt++
            )
            val norm = MultiLayerQuestionValidator.normalizeText(q.questionEnglish.ifBlank { q.questionHindi })
            assertTrue("Text repeat at draw $round (tier $tier): $norm", !servedTexts.contains(norm))
            servedTexts.add(norm)
            servedSem.add(q.semanticFingerprint.trim().lowercase())
            if (q.logicFingerprint.isNotBlank()) servedLogic.add(q.logicFingerprint.trim().lowercase())
            if (q.conceptFingerprint.isNotBlank()) servedConcept.add(q.conceptFingerprint.trim().lowercase())
        }
    }
}
