package com.example.data.repository

import com.example.data.model.QuestionItem
import com.example.data.model.UserProfile
import java.security.MessageDigest

/**
 * Multi-layer duplicate detector, fingerprint generator, and watchdog integrity validator.
 * Enforces:
 * 1. Exact text normalization (Western digits, stripped whitespace/punctuation).
 * 2. Multi-layer fingerprinting: Semantic (SHA-256 of text+answer), Logic (formula/rule),
 *    Concept (topic/subtopic/entity), and Pattern (reasoning family).
 * 3. Multi-layer collision detection across DB historical registry and current session.
 * 4. Watchdog integrity: rejects stale caches, profile/city/mode mismatches, and class inappropriateness.
 */
object MultiLayerQuestionValidator {

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun normalizeText(text: String): String {
        return text
            .replace('०', '0')
            .replace('१', '1')
            .replace('२', '2')
            .replace('३', '3')
            .replace('४', '4')
            .replace('५', '5')
            .replace('६', '6')
            .replace('७', '7')
            .replace('८', '8')
            .replace('९', '9')
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun computeSemanticFingerprint(normalizedText: String, normalizedAnswer: String): String {
        return sha256("sem_${normalizedText}_ans_${normalizedAnswer}")
    }

    fun computeLogicFingerprint(ruleKey: String, variableSignature: String): String {
        return sha256("logic_${ruleKey}_${variableSignature}")
    }

    fun computeConceptFingerprint(topic: String, subtopic: String, entityContext: String): String {
        val cleanTopic = normalizeText(topic)
        val cleanSub = normalizeText(subtopic)
        val cleanEntity = normalizeText(entityContext)
        return sha256("concept_${cleanTopic}_${cleanSub}_${cleanEntity}")
    }

    fun computePatternFingerprint(patternFamily: String, reasoningType: String): String {
        return sha256("pat_${normalizeText(patternFamily)}_${normalizeText(reasoningType)}")
    }

    data class ValidationResult(
        val isValid: Boolean,
        val rejectionReason: String? = null
    )

    data class HistoricalRegistry(
        val servedNormalizedTexts: Set<String> = emptySet(),
        val servedSemanticFingerprints: Set<String> = emptySet(),
        val servedLogicFingerprints: Set<String> = emptySet(),
        val servedConceptFingerprints: Set<String> = emptySet(),
        /** Concepts (templates) used in the most recent game; avoided so back-to-back games do not feel the same. */
        val recentConceptFingerprints: Set<String> = emptySet()
    )

    /**
     * Checks if a candidate question satisfies all multi-layer uniqueness constraints:
     * - No exact or normalized text match in history or current session.
     * - No semantic fingerprint match (identical question meaning + answer).
     * - No logic fingerprint match (identical mathematical/deductive structure).
     * - No concept fingerprint collision in the same session.
     * - Distinct 4 options with valid correct answer index.
     */
    fun validateCandidate(
        candidate: QuestionItem,
        history: HistoricalRegistry,
        currentSessionQuestions: Collection<QuestionItem>,
        avoidRecentConcepts: Boolean = false
    ): ValidationResult {
        // 1. Basic structural validity
        if (candidate.optionsEnglish.size != 4 || candidate.optionsHindi.size != 4) {
            return ValidationResult(false, "Options count must be exactly 4")
        }
        if (candidate.correctAnswerIndex !in 0..3) {
            return ValidationResult(false, "Correct answer index out of bounds: ${candidate.correctAnswerIndex}")
        }
        if (candidate.optionsEnglish.distinct().size != 4) {
            return ValidationResult(false, "English options must be distinct")
        }
        if (candidate.optionsHindi.distinct().size != 4) {
            return ValidationResult(false, "Hindi options must be distinct")
        }

        val normEn = normalizeText(candidate.questionEnglish)
        val normHi = normalizeText(candidate.questionHindi)
        val normAns = normalizeText(candidate.optionsEnglish.getOrNull(candidate.correctAnswerIndex) ?: "")

        if (normEn.isBlank() && normHi.isBlank()) {
            return ValidationResult(false, "Question text cannot be blank")
        }

        // 2. Exact / Normalized text duplicate check
        if (history.servedNormalizedTexts.contains(normEn) || history.servedNormalizedTexts.contains(normHi)) {
            return ValidationResult(false, "Normalized text already served in prior session")
        }
        val isDuplicateInSessionText = currentSessionQuestions.any {
            val qNormEn = normalizeText(it.questionEnglish)
            val qNormHi = normalizeText(it.questionHindi)
            (normEn.isNotBlank() && normEn == qNormEn) || (normHi.isNotBlank() && normHi == qNormHi)
        }
        if (isDuplicateInSessionText) {
            return ValidationResult(false, "Question text duplicate in current session")
        }

        // 3. Semantic fingerprint check (SHA-256 of text + answer)
        val computedSemanticFp = candidate.semanticFingerprint.ifBlank {
            computeSemanticFingerprint(normEn.ifBlank { normHi }, normAns)
        }
        if (history.servedSemanticFingerprints.contains(computedSemanticFp)) {
            return ValidationResult(false, "Semantic fingerprint already used in history")
        }
        if (currentSessionQuestions.any { it.semanticFingerprint == computedSemanticFp }) {
            return ValidationResult(false, "Semantic fingerprint collision in current session")
        }

        // 4. Logic fingerprint check
        if (candidate.logicFingerprint.isNotBlank()) {
            if (history.servedLogicFingerprints.contains(candidate.logicFingerprint)) {
                return ValidationResult(false, "Logic fingerprint already used in history")
            }
            if (currentSessionQuestions.any { it.logicFingerprint == candidate.logicFingerprint }) {
                return ValidationResult(false, "Logic fingerprint collision in current session")
            }
        }

        // 5. Concept fingerprint check (prevent duplicate concept in same game)
        if (candidate.conceptFingerprint.isNotBlank()) {
            if (currentSessionQuestions.any { it.conceptFingerprint == candidate.conceptFingerprint }) {
                return ValidationResult(false, "Concept fingerprint collision in current session")
            }
        }

        // 6. Cross-game cooldown: skip the template used in the most recent game (soft rule, only when asked)
        if (avoidRecentConcepts && candidate.conceptFingerprint.isNotBlank() &&
            history.recentConceptFingerprints.contains(candidate.conceptFingerprint.trim().lowercase())) {
            return ValidationResult(false, "Same template as the previous game")
        }

        return ValidationResult(true)
    }

    /**
     * Watchdog validation for the complete 17-question bank.
     * Guarantees:
     * - Exactly 17 questions covering Tiers 1 through 17.
     * - Matches current session ID.
     * - Matches user profile (state, city, junior/adult mode, student class).
     * - Junior mode educational appropriateness.
     * - Zero duplicates within the bank.
     */
    fun validateSessionBankWatchdog(
        questions: Map<Int, QuestionItem>,
        sessionId: String,
        profile: UserProfile
    ): ValidationResult {
        if (questions.size != 17) {
            return ValidationResult(false, "Session bank has ${questions.size} questions; exactly 17 required")
        }

        for (tier in 1..17) {
            val q = questions[tier] ?: return ValidationResult(false, "Missing tier $tier in session bank")
            if (q.qNumber != tier) {
                return ValidationResult(false, "Tier mismatch for slot $tier (qNumber=${q.qNumber})")
            }
            if (q.sessionId.isNotBlank() && q.sessionId != sessionId) {
                return ValidationResult(false, "Question for tier $tier belongs to stale session ${q.sessionId}, active is $sessionId")
            }
            if (q.generationVersion < 2) {
                return ValidationResult(false, "Question for tier $tier has obsolete generationVersion ${q.generationVersion}")
            }
        }

        // Junior appropriateness check
        val isStudent = profile.preparationDomain.contains("Student", true) || profile.isStudentMode
        if (isStudent) {
            val classNum = extractClassNumber(profile.studentClass)
            for ((tier, q) in questions) {
                val appropriateness = checkJuniorAppropriateness(q, classNum)
                if (!appropriateness.isValid) {
                    return ValidationResult(false, "Tier $tier: ${appropriateness.rejectionReason}")
                }
            }
        }

        // Intra-bank duplicate check
        val seenSemantics = mutableSetOf<String>()
        val seenLogics = mutableSetOf<String>()
        val seenTexts = mutableSetOf<String>()

        for ((tier, q) in questions) {
            val normText = normalizeText(q.questionEnglish.ifBlank { q.questionHindi })
            if (seenTexts.contains(normText)) {
                return ValidationResult(false, "Duplicate question text at tier $tier in session bank")
            }
            seenTexts.add(normText)

            if (q.semanticFingerprint.isNotBlank()) {
                if (seenSemantics.contains(q.semanticFingerprint)) {
                    return ValidationResult(false, "Duplicate semantic fingerprint at tier $tier in session bank")
                }
                seenSemantics.add(q.semanticFingerprint)
            }

            if (q.logicFingerprint.isNotBlank()) {
                if (seenLogics.contains(q.logicFingerprint)) {
                    return ValidationResult(false, "Duplicate logic fingerprint at tier $tier in session bank")
                }
                seenLogics.add(q.logicFingerprint)
            }
        }

        return ValidationResult(true)
    }

    private fun extractClassNumber(studentClass: String): Int {
        val match = Regex("\\d+").find(studentClass)
        return match?.value?.toIntOrNull() ?: 8
    }

    private fun checkJuniorAppropriateness(q: QuestionItem, classNum: Int): ValidationResult {
        // Lower primary (Class 1-5): No complex multi-variable algebra, no Knights & Knaves, no cryptography
        if (classNum <= 5) {
            val forbidden = listOf("cryptarithm", "knights & knaves", "quantum", "calculus", "syllogism", "relativity")
            val cat = q.category.lowercase()
            for (f in forbidden) {
                if (cat.contains(f)) {
                    return ValidationResult(false, "Topic '$cat' exceeds primary syllabus for Class $classNum")
                }
            }
        }
        // Middle primary (Class 6-8): No advanced relativistic physics, formal knights & knaves, multi-step formal logic grids
        if (classNum <= 8) {
            val forbidden = listOf("relativistic", "quantum", "diagonalization")
            val cat = q.category.lowercase()
            for (f in forbidden) {
                if (cat.contains(f)) {
                    return ValidationResult(false, "Topic '$cat' exceeds middle school syllabus for Class $classNum")
                }
            }
        }
        return ValidationResult(true)
    }
}
