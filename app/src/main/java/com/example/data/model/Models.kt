package com.example.data.model

data class UserProfile(
    val id: String = "primary_user",
    val name: String = "Rahul",
    val age: Int = 18,
    val state: String = "Uttar Pradesh",
    val city: String = "Lucknow",
    val educationLevel: String = "Graduate",
    val occupation: String = "Aspirant / Student",
    val preparationDomain: String = "Logic",
    val studentClass: String = "Class 8",
    val isStudentMode: Boolean = false,
    val interests: List<String> = listOf("Logical Deductions", "Spatial Vectors", "Forensic Timelines"),
    val selectedCategories: List<String> = emptyList(),
    val languageMode: String = "ENGLISH",
    val hostGender: String = "FEMALE",
    val upiId: String = "",
    val profileVector: KnowledgeProfileVector = KnowledgeProfileVector()
)

data class KnowledgeProfileVector(
    val generalKnowledge: Float = 0.6f,   // 0.0 to 1.0
    val logicalReasoning: Float = 0.8f,
    val historyChronology: Float = 0.5f,
    val scienceTech: Float = 0.6f,
    val financeEconomics: Float = 0.4f,
    val spatialVisual: Float = 0.7f,
    val domainStrength: Float = 0.75f,
    val regionalContext: String = "Uttar Pradesh",
    val regionalCity: String = "Lucknow"
)

data class QuestionItem(
    val id: String,
    val qNumber: Int,                     // 1 to 17
    val difficultyLevel: Int = qNumber,   // 1 to 17
    val difficultyTitle: String,          // "Very Easy", "Easy+", "Medium", "Hard", "Expert", "Extreme"
    val timeLimitSeconds: Int?,           // 60, 120, or null (no limit)
    val points: Long,                     // 1,000 to 70,000,000
    val isCheckpoint: Boolean,            // Q5, Q10, Q16, Q17
    val checkpointTitle: String? = null,  // "पहला पड़ाव", "दूसरा पड़ाव", "तीसरा पड़ाव", "अंतिम महा-तर्क"
    val category: String,                 // "Spatial Vector", "Shadow Optics", "Syllogism", "Forensic", "Rhythm Meter", "Chronology Logic"
    
    // Bilingual Content
    val questionHindi: String,
    val questionEnglish: String,
    val cluesHindi: List<String>,
    val cluesEnglish: List<String>,
    val optionsHindi: List<String>,       // 4 options
    val optionsEnglish: List<String>,     // 4 options
    val correctAnswerIndex: Int,          // 0..3
    
    // Pure Logic Proofs (Crucial differentiator!)
    val deductionPathHindi: String,
    val deductionPathEnglish: String,
    val eliminationReasonsHindi: List<String>, // Reason why each wrong option is logically impossible
    val eliminationReasonsEnglish: List<String>,
    
    // Normal Free Hint (Triggered at 50% timer in Q1-Q10 or Manual in Q11+)
    val hintHindi: String = "",
    val hintEnglish: String = "",
    
    // Lifeline Intelligence (Tark Guru / 50-50)
    val expertAdviceHindi: String,        // Deep thinking framework, never the direct answer
    val expertAdviceEnglish: String,
    val fiftyFiftyDiscardIndices: List<Int>, // Exactly 2 wrong option indices that are demonstrably eliminated
    val fiftyFiftyProofHindi: String,
    val fiftyFiftyProofEnglish: String,
    
    // Interactive Clue Visualizer / Audio
    val diagramType: String = "none",     // "none", "shadow_sun", "coordinate_path", "matrix_grid", "audio_wave", "venn_logic", "clock_angle"
    val diagramData: String = "",         // JSON / metadata describing parameters for visual canvas
    val audioPatternType: String? = null, // e.g. "waltz_3_4", "syncopated_4_4", "harmonic_interval"
    
    // Anti-Repeat Multi-Layer Fingerprints & Fresh Session Identity
    val semanticFingerprint: String,
    val logicFingerprint: String = "",
    val patternFingerprint: String = "",
    val conceptFingerprint: String = "",
    val sessionId: String = "",
    val generationVersion: Int = 2,
    val generatedAt: Long = System.currentTimeMillis(),
    val isFlippedOrUsed: Boolean = false
)

data class CurrentAffairItem(
    val currentAffairId: String,
    val eventId: String,
    val headline: String,
    val canonicalSummary: String,
    val eventDate: String,
    val firstSeenDate: Long = System.currentTimeMillis(),
    val lastVerifiedDate: Long = System.currentTimeMillis(),
    val sourceReferences: String,
    val country: String = "India",
    val state: String = "National",
    val districtRegion: String = "",
    val topic: String, // "National", "International", "Govt Schemes", "Science & Tech", "Environment", "Economy", "Sports", "Education", "Appointments", "Regional"
    val juniorEligibility: Boolean = true,
    val adultEligibility: Boolean = true,
    val minAge: Int = 5,
    val maxAge: Int = 99,
    val examRelevance: String = "All",
    val usedQuestionIds: List<String> = emptyList(),
    val isExpired: Boolean = false
)

data class LifelineState(
    val is5050Available: Boolean = true,
    val is5050UsedInCurrentQ: Boolean = false,
    val is5050Exhausted: Boolean = false,
    
    val isExpertAvailable: Boolean = true,
    val isExpertUsedInCurrentQ: Boolean = false,
    val isExpertExhausted: Boolean = false,
    
    val isFlipAvailable: Boolean = true,
    val isFlipExhausted: Boolean = false,
    
    val isPowerPapluAvailable: Boolean = true,
    val isPowerPapluExhausted: Boolean = false,
    val rechargedLifelineName: String? = null, // Name of the lifeline revived by Paplu

    val isHintAvailable: Boolean = true,
    val isHintUsedInCurrentQ: Boolean = false
)

data class PadaavTier(
    val questionNumber: Int,
    val points: Long,
    val labelHindi: String,
    val labelEnglish: String,
    val isCheckpoint: Boolean,
    val checkpointName: String? = null,
    val timeLimitSec: Int?
)

data class GameSessionResult(
    val sessionId: String,
    val userName: String = "Challenger",
    val totalPointsWon: Long,
    val highestQuestionReached: Int,
    val isCompletedWon: Boolean,
    val guaranteedPointsSecured: Long,
    val reasonEnded: String,
    val questionsAnsweredCount: Int,
    val correctCount: Int,
    val wrongCount: Int = 0,
    val lifelinesUsedCount: Int,
    val hintsUsedCount: Int = 0,
    val totalResponseTimeSec: Float = 0f,
    val averageResponseTimeSec: Float,
    val logicAccuracyPercentage: Int,
    val gameMode: String = "Adult",
    val examContext: String = "General Reasoning",
    val timestamp: Long = System.currentTimeMillis()
)

/** True for a junior player: Student mode/domain chosen, or a recorded age under 18 (ages 5 to 17). */
fun UserProfile.isJuniorPlayer(): Boolean =
    preparationDomain.contains("Student", true) || isStudentMode || age in 5..17

/**
 * School class used to pick question level. A player who chose a class uses it; a player known
 * only by age gets the class for that age, so a young child never falls back to a middle-school default.
 */
fun UserProfile.effectiveClassNumber(): Int {
    val chosen = preparationDomain.contains("Student", true) || isStudentMode
    if (chosen) return Regex("\\d+").find(studentClass)?.value?.toIntOrNull() ?: classForAge(age)
    return classForAge(age)
}

fun classForAge(age: Int): Int = when {
    age <= 6 -> 1
    age <= 8 -> 3
    age <= 10 -> 5
    age <= 12 -> 7
    age <= 14 -> 9
    age <= 16 -> 11
    else -> 12
}
