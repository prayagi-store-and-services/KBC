package com.example.data.repository

import com.example.data.model.isJuniorPlayer
import com.example.data.model.effectiveClassNumber
import com.example.data.model.QuestionItem
import com.example.data.model.UserProfile
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Procedural Dynamic Logic Engine for TarkShastra.
 * Generates mathematically rigorous, fully reasoned questions with dynamic parameters,
 * randomized entities, varying numerical coefficients, recalculated answers and proofs.
 * 
 * Guarantees:
 * 1. Multi-candidate pools per tier (no 1-to-1 static tier mapping).
 * 2. Multi-layer SHA-256 fingerprinting (Semantic, Logic, Concept, Pattern).
 * 3. Junior mode grade appropriateness (Class 1-5, Class 6-8, Class 9-10, Class 11-12).
 * 4. Adult competitive reasoning & regional personalization (State, City).
 * 5. 100% deductive solvability with verified clues, elimination reasons, and 50-50 discard proofs.
 */
object DynamicLogicEngine {

    data class TierInfo(
        val difficultyTitle: String,
        val timeLimitSeconds: Int?,
        val points: Long,
        val isCheckpoint: Boolean,
        val checkpointTitle: String? = null
    )

    fun getTierMeta(qNumber: Int): TierInfo = when (qNumber) {
        1 -> TierInfo("Very Easy", 60, 10L, false)
        2 -> TierInfo("Very Easy", 60, 15L, false)
        3 -> TierInfo("Easy", 60, 20L, false)
        4 -> TierInfo("Easy", 60, 25L, false)
        5 -> TierInfo("Easy+", 60, 30L, true, "पहला पड़ाव (1st Checkpoint)")
        6 -> TierInfo("Medium", 120, 40L, false)
        7 -> TierInfo("Medium", 120, 50L, false)
        8 -> TierInfo("Medium+", 120, 60L, false)
        9 -> TierInfo("Medium+", 120, 70L, false)
        10 -> TierInfo("Hard", 120, 180L, true, "दूसरा पड़ाव (2nd Checkpoint)")
        11 -> TierInfo("Hard", null, 50L, false)
        12 -> TierInfo("Hard+", null, 60L, false)
        13 -> TierInfo("Very Hard", null, 70L, false)
        14 -> TierInfo("Very Hard", null, 80L, false)
        15 -> TierInfo("Expert", null, 90L, false)
        16 -> TierInfo("Expert+", null, 100L, true, "तीसरा पड़ाव (3rd Checkpoint)")
        17 -> TierInfo("Extreme Grandmaster", null, 50L, true, "अंतिम महा-तर्क (Final Crown)")
        else -> TierInfo("Standard", 60, 10L, false)
    }

    data class GeneratorDescriptor(
        val id: String,
        val familyKey: String,
        val category: String,
        val tierRange: IntRange,
        val isJuniorSuitable: Boolean,
        val isAdultSuitable: Boolean,
        val minAge: Int = 5,
        val maxAge: Int = 99,
        val generate: (qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile) -> QuestionItem
    )

    // =========================================================================
    // GENERATOR REGISTRY ACROSS ALL 4 TIERS
    // =========================================================================
    val allGenerators = listOf(
        // Band 1: Tiers 1 - 5 (Foundation / Early Logic)
        GeneratorDescriptor(
            id = "spatial_vector",
            familyKey = "spatial_vector",
            category = "Spatial Coordinate Vector",
            tierRange = 1..4,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 6,
            maxAge = 99,
            generate = ::generateSpatialVector
        ),
        GeneratorDescriptor(
            id = "balance_scale",
            familyKey = "balance_scale",
            category = "Transitive Weight & Balance Deduction",
            tierRange = 1..5,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 6,
            maxAge = 99,
            generate = ::generateBalanceScale
        ),
        GeneratorDescriptor(
            id = "clock_geometry",
            familyKey = "clock_geometry",
            category = "Clock Geometry & Cyclic Angle",
            tierRange = 2..5,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 8,
            maxAge = 99,
            generate = ::generateClockGeometry
        ),
        GeneratorDescriptor(
            id = "food_chain",
            familyKey = "food_chain",
            category = "Ecological Energy Flow & Food Web",
            tierRange = 1..4,
            isJuniorSuitable = true,
            isAdultSuitable = false,
            minAge = 6,
            maxAge = 14,
            generate = ::generateFoodChain
        ),
        GeneratorDescriptor(
            id = "calendar_cyclic",
            familyKey = "calendar_cyclic",
            category = "Calendar Modulo & Day Cycles",
            tierRange = 2..6,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 8,
            maxAge = 99,
            generate = ::generateCalendarCyclic
        ),
        GeneratorDescriptor(
            id = "number_sequence",
            familyKey = "number_sequence",
            category = "Mathematical Sequence & Pattern",
            tierRange = 1..5,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 6,
            maxAge = 99,
            generate = ::generateNumberSequence
        ),


        // Band 2: Tiers 6 - 10 (Intermediate / Multi-Step Logic)
        GeneratorDescriptor(
            id = "word_cipher",
            familyKey = "word_cipher",
            category = "Cryptographic Letter-Shift Cipher",
            tierRange = 6..8,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 11,
            maxAge = 99,
            generate = ::generateWordCipher
        ),
        GeneratorDescriptor(
            id = "river_crossing",
            familyKey = "river_crossing",
            category = "Constraint Optimization & River Crossing",
            tierRange = 6..9,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 10,
            maxAge = 99,
            generate = ::generateRiverCrossing
        ),
        GeneratorDescriptor(
            id = "venn_sets",
            familyKey = "venn_sets",
            category = "Set Theory & 3-Circle Venn Logic",
            tierRange = 6..10,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 11,
            maxAge = 99,
            generate = ::generateVennSets
        ),
        GeneratorDescriptor(
            id = "speed_distance",
            familyKey = "speed_distance",
            category = "Kinematics & Relative Speed",
            tierRange = 6..10,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 11,
            maxAge = 99,
            generate = ::generateSpeedDistance
        ),
        GeneratorDescriptor(
            id = "age_algebra",
            familyKey = "age_algebra",
            category = "Age-Algebra Linear System",
            tierRange = 6..10,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 11,
            maxAge = 99,
            generate = ::generateAgeAlgebra
        ),
        GeneratorDescriptor(
            id = "successive_percentage",
            familyKey = "successive_percentage",
            category = "Quantitative Data Interpretation",
            tierRange = 6..10,
            isJuniorSuitable = false,
            isAdultSuitable = true,
            minAge = 14,
            maxAge = 99,
            generate = ::generateSuccessivePercentage
        ),
        GeneratorDescriptor(
            id = "pythagoras_vector",
            familyKey = "pythagoras_vector",
            category = "Spatial Coordinate Vector & Pythagoras",
            tierRange = 6..10,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 13,
            maxAge = 99,
            generate = ::generatePythagorasVector
        ),
        GeneratorDescriptor(
            id = "seating_arrangement",
            familyKey = "seating_arrangement",
            category = "Linear & Circular Seating Logic",
            tierRange = 7..10,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 11,
            maxAge = 99,
            generate = ::generateSeatingArrangement
        ),

        // Band 3: Tiers 11 - 15 (Advanced Deduction & Inference)
        GeneratorDescriptor(
            id = "pigeonhole_draw",
            familyKey = "pigeonhole_draw",
            category = "Pigeonhole Principle (Dirichlet)",
            tierRange = 11..14,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 12,
            maxAge = 99,
            generate = ::generatePigeonholeDraw
        ),
        GeneratorDescriptor(
            id = "shadow_optics",
            familyKey = "shadow_optics",
            category = "Solar Angle & Shadow Trigonometry",
            tierRange = 11..14,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 12,
            maxAge = 99,
            generate = ::generateShadowOptics
        ),
        GeneratorDescriptor(
            id = "matrix_rotation",
            familyKey = "matrix_rotation",
            category = "3x3 Matrix Grid Transformation",
            tierRange = 11..14,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 12,
            maxAge = 99,
            generate = ::generateMatrixRotation
        ),
        GeneratorDescriptor(
            id = "cryptarithm",
            familyKey = "cryptarithm",
            category = "Alphametic Cryptarithm",
            tierRange = 12..15,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 13,
            maxAge = 99,
            generate = ::generateCryptarithm
        ),
        GeneratorDescriptor(
            id = "forensic_timeline",
            familyKey = "forensic_timeline",
            category = "Forensic Chronology & Alibi Invalidation",
            tierRange = 12..15,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 13,
            maxAge = 99,
            generate = ::generateForensicTimeline
        ),
        GeneratorDescriptor(
            id = "probability_risk",
            familyKey = "probability_risk",
            category = "Probability & Risk Trees",
            tierRange = 11..15,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 14,
            maxAge = 99,
            generate = ::generateProbabilityRisk
        ),

        // Band 4: Tiers 16 - 17 (Master & Grandmaster Logic)
        GeneratorDescriptor(
            id = "knights_knaves",
            familyKey = "knights_knaves",
            category = "Knights & Knaves (Smullyan Island)",
            tierRange = 15..17,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 14,
            maxAge = 99,
            generate = ::generateKnightsKnaves
        ),
        GeneratorDescriptor(
            id = "master_syllogism",
            familyKey = "master_syllogism",
            category = "Grand Syllogistic Deduction (महा-तर्क)",
            tierRange = 16..17,
            isJuniorSuitable = true,
            isAdultSuitable = true,
            minAge = 14,
            maxAge = 99,
            generate = ::generateMasterSyllogism
        ),
        GeneratorDescriptor(
            id = "game_theory_minimax",
            familyKey = "game_theory_minimax",
            category = "Game Theory & Dominant Strategy",
            tierRange = 16..17,
            isJuniorSuitable = false,
            isAdultSuitable = true,
            minAge = 16,
            maxAge = 99,
            generate = ::generateGameTheoryMinimax
        )
    )

    /**
     * Generates a unique, non-repeated question for `qNumber`.
     * Filters candidate generators dynamically matching the tier, mode, and age.
     * Evaluates multi-layer fingerprints to ensure zero duplicates.
     */
    fun generateUniqueQuestion(
        qNumber: Int,
        userProfile: UserProfile,
        history: MultiLayerQuestionValidator.HistoricalRegistry,
        currentSessionQuestions: Collection<QuestionItem>,
        salt: Int = Random.nextInt(1, 1000000)
    ): QuestionItem {
        val isStudent = userProfile.isJuniorPlayer()
        val studentAge = userProfile.age

        // Filter valid generators for this tier and user profile
        val candidates = allGenerators.filter { g ->
            qNumber in g.tierRange &&
            if (isStudent) {
                g.isJuniorSuitable && studentAge in g.minAge..g.maxAge
            } else {
                g.isAdultSuitable
            }
        }.shuffled(Random(salt + qNumber * 101))

        val meta = getTierMeta(qNumber)

        // Pass 1 avoids the template used in the previous game; pass 2 allows it but still requires a fresh question.
        for (avoidRecent in listOf(true, false)) {
            for (gen in candidates) {
                for (attempt in 0..10) {
                    val rand = Random(salt + attempt * 79 + gen.id.hashCode())
                    val question = gen.generate(qNumber, meta, rand, userProfile)
                    val validation = MultiLayerQuestionValidator.validateCandidate(
                        candidate = question,
                        history = history,
                        currentSessionQuestions = currentSessionQuestions,
                        avoidRecentConcepts = avoidRecent
                    )
                    if (validation.isValid) {
                        return question
                    }
                }
            }
        }

        // Widen the search with many more variations. Never hand back an unchecked repeat while a valid one exists.
        val pool = candidates.ifEmpty { allGenerators.filter { qNumber in it.tierRange } }
        for (extra in 1..300) {
            val gen = pool[extra % pool.size]
            val question = gen.generate(qNumber, meta, Random(salt + extra * 7919 + gen.id.hashCode()), userProfile)
            if (MultiLayerQuestionValidator.validateCandidate(question, history, currentSessionQuestions).isValid) {
                return question
            }
        }

        // Truly fingerprint-exhausted (every variation already served): the
        // no-repeated-questions promise is text-level, so keep drawing fresh
        // parameter variations until the wording is new. Numeric and name
        // parameters change the text, so this succeeds in practice; the bound
        // only guards a mathematically tiny pool.
        var lastResort: QuestionItem? = null
        for (retry in 0..400) {
            val gen = pool[retry % pool.size]
            val candidate = gen.generate(qNumber, meta, Random(salt + 999 + retry * 31337), userProfile)
            if (lastResort == null) lastResort = candidate
            if (!MultiLayerQuestionValidator.isTextRepeat(candidate, history, currentSessionQuestions)) {
                if (retry > 0) {
                    android.util.Log.w("TarkShastra", "Tier $qNumber fingerprint pool exhausted; serving text-fresh variant after $retry retries")
                }
                return candidate
            }
        }
        android.util.Log.e("TarkShastra", "Tier $qNumber text pool mathematically exhausted; serving least-bad variant")
        return lastResort!!
    }

    // =========================================================================
    // 1. SPATIAL VECTOR (Tiers 1 - 4)
    // =========================================================================
    private fun generateSpatialVector(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val names = listOf("Aarav", "Rohan", "Riya", "Ananya", "Kabir", "Meera", "Vikram", "Priya")
        val startName = names.random(rand)
        val d1 = listOf(4, 6, 8, 10, 12).random(rand)
        val d2 = listOf(3, 5, 7, 9).random(rand)
        val dir1 = listOf("उत्तर (North)" to "North", "दक्षिण (South)" to "South").random(rand)
        val turn = listOf("दाएँ (Right)" to "East", "बाएँ (Left)" to "West").random(rand)

        val expectedDirHi = if (dir1.second == "North" && turn.second == "East") "उत्तर-पूर्व (North-East)"
        else if (dir1.second == "North" && turn.second == "West") "उत्तर-पश्चिम (North-West)"
        else if (dir1.second == "South" && turn.second == "East") "दक्षिण-पूर्व (South-East)"
        else "दक्षिण-पश्चिम (South-West)"

        val expectedDirEn = if (dir1.second == "North" && turn.second == "East") "North-East"
        else if (dir1.second == "North" && turn.second == "West") "North-West"
        else if (dir1.second == "South" && turn.second == "East") "South-East"
        else "South-West"

        val allDirsHi = listOf("उत्तर-पूर्व (North-East)", "उत्तर-पश्चिम (North-West)", "दक्षिण-पूर्व (South-East)", "दक्षिण-पश्चिम (South-West)")
        val allDirsEn = listOf("North-East", "North-West", "South-East", "South-West")
        val correctIdx = allDirsEn.indexOf(expectedDirEn).coerceAtLeast(0)

        val qHi = "$startName बिंदु A से ${dir1.first} की ओर $d1 किमी चलता है, फिर ${turn.first} मुड़कर $d2 किमी चलता है। अब वह अपने प्रारंभिक बिंदु A से किस दिशा में स्थित है?"
        val qEn = "$startName walks $d1 km ${dir1.second} from point A, then turns ${turn.second} and walks $d2 km. In which direction is he/she located relative to starting point A?"

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(expectedDirEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Spatial Coordinate Vector",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("पहला विस्थापन: ${dir1.first} = $d1 किमी।", "दूसरा विस्थापन: ${turn.first} मुड़ने पर = $d2 किमी।", "प्रारंभिक बिंदु (0,0) से दोनों अक्षों पर स्थिति का परीक्षण करें।"),
            cluesEnglish = listOf("First displacement: ${dir1.second} = $d1 km.", "Second displacement: After ${turn.second} turn = $d2 km.", "Inspect net coordinate relative to origin (0,0)."),
            optionsHindi = allDirsHi,
            optionsEnglish = allDirsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "बिंदु (0,0) से ${dir1.first} जाने के बाद ${turn.first} जाने पर अंतिम स्थिति सीधे '$expectedDirHi' चतुर्थांश में बनती है।",
            deductionPathEnglish = "Moving ${dir1.second} followed by a ${turn.second} displacement places the endpoint unambiguously in the $expectedDirEn quadrant.",
            eliminationReasonsHindi = allDirsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "सही: दोनों दिशा विस्थापन का प्रत्यक्ष परिणाम $opt है।" else "गलत: यह दिशा दिए गए मोड़ और प्रारंभिक विस्थापन के विपरीत है।" },
            eliminationReasonsEnglish = allDirsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Correct: Net vector points into $opt." else "False: Violates the directional turn sequence." },
            expertAdviceHindi = "कागज़ पर + का निशान बनाकर उत्तर, दक्षिण, पूर्व, पश्चिम को चिन्हित करें।",
            expertAdviceEnglish = "Draw a compass cross on paper to track orthogonal movements.",
            fiftyFiftyDiscardIndices = listOf((correctIdx + 1) % 4, (correctIdx + 2) % 4),
            fiftyFiftyProofHindi = "दो विपरीत दिशाएं स्पष्ट रूप से निरस्त होती हैं।",
            fiftyFiftyProofEnglish = "The two opposite quadrants are demonstrably false.",
            diagramType = "coordinate_path",
            diagramData = "${dir1.second}:$d1,${turn.second}:$d2",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("spatial_vector", "${dir1.second}_${turn.second}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Spatial Reasoning", "Direction Compass", "${dir1.second}_${turn.second}"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("spatial_reasoning", "coordinate_displacement"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 2. BALANCE SCALE (Tiers 1 - 5)
    // =========================================================================
    private fun generateBalanceScale(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val objects = listOf("सेब (Apple)" to "Apple", "संतरा (Orange)" to "Orange", "आम (Mango)" to "Mango", "केला (Banana)" to "Banana", "नाशपाती (Pear)" to "Pear").shuffled(rand)
        val a = objects[0]
        val b = objects[1]
        val c = objects[2]
        val n1 = listOf(2, 3).random(rand)
        val n2 = listOf(2, 4).random(rand)
        val answerMult = n1 * n2

        val qHi = "एक तराजू में $n1 ${a.first} का भार $n2 ${b.first} के भार के बराबर है। यदि 1 ${b.first} का भार 2 ${c.first} के बराबर है, तो $n1 ${a.first} का भार कितने ${c.first} के बराबर होगा?"
        val qEn = "On a balance scale, $n1 ${a.second}s weigh the same as $n2 ${b.second}s. If 1 ${b.second} weighs the same as 2 ${c.second}s, how many ${c.second}s balance $n1 ${a.second}s?"

        val correctOptionStrHi = "$answerMult ${c.first}"
        val correctOptionStrEn = "$answerMult ${c.second}s"
        val optsEn = listOf(correctOptionStrEn, "${answerMult + 2} ${c.second}s", "${maxOf(1, answerMult - 2)} ${c.second}s", "${n1 + n2} ${c.second}s").distinct().shuffled(rand)
        val optsHi = optsEn.map { it.replace("${c.second}s", c.first) }
        val correctIdx = optsEn.indexOf(correctOptionStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctOptionStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Transitive Weight & Balance Deduction",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("$n1 ${a.first} = $n2 ${b.first}", "1 ${b.first} = 2 ${c.first}", "प्रतिस्थापन नियम: $n2 ${b.first} = $n2 × 2 ${c.first}"),
            cluesEnglish = listOf("$n1 ${a.second}s = $n2 ${b.second}s", "1 ${b.second} = 2 ${c.second}s", "Substitution: $n2 ${b.second}s = $n2 × 2 = $answerMult ${c.second}s"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "$n1 A = $n2 B और B = 2 C, अतः $n1 A = $n2 × 2 C = $answerMult C।",
            deductionPathEnglish = "Since $n1 A = $n2 B and B = 2 C, by direct transitive substitution $n1 A = $n2 × 2 C = $answerMult C.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "सटीक गुणन नियम द्वारा सिद्ध।" else "असंगत अनुपात गणना।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived via transitive substitution." else "Violates the transitive ratio equality." },
            expertAdviceHindi = "मध्यवर्ती वस्तु (B) का मान तीसरे पद (C) में प्रतिस्थापित करें।",
            expertAdviceEnglish = "Substitute intermediate variable B with its equivalent in terms of C.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत गुणनफल विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Non-multiple options eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("balance_transitive", "${n1}_${n2}_2"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Algebraic Reasoning", "Transitive Substitution", "balance_scale"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "transitive_equality"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 3. CLOCK GEOMETRY (Tiers 2 - 5)
    // =========================================================================
    private fun generateClockGeometry(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val hours = listOf(3, 4, 8, 9).random(rand)
        val angleDeg = when (hours) {
            3, 9 -> 90
            4, 8 -> 120
            else -> 90
        }
        val qHi = "ठीक $hours:00 बजे एक मानक एनालॉग घड़ी में घंटे की सुई और मिनट की सुई के बीच का छोटा आंतरिक कोण कितने डिग्री का होता है?"
        val qEn = "At exactly $hours:00 on a standard analog clock, what is the smaller interior angle between the hour hand and the minute hand in degrees?"

        val correctStrEn = "$angleDeg°"
        val optsEn = listOf(correctStrEn, "${angleDeg - 30}°", "${angleDeg + 30}°", "${180 - angleDeg}°").distinct().shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Clock Geometry & Cyclic Angle",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("घड़ी का पूरा डायल = 360°", "प्रत्येक 1 घंटे का अंतर = 360° ÷ 12 = 30°", "मिनट सुई 12 पर और घंटे सुई $hours पर है।"),
            cluesEnglish = listOf("Complete clock dial = 360°", "Each 1-hour interval = 360° / 12 = 30°", "Minute hand is at 12; hour hand is at $hours."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "अंतर = $hours घंटे के ब्लॉक × 30° = $angleDeg°।",
            deductionPathEnglish = "Separation = $hours hour units × 30° per hour = $angleDeg°.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "सटीक 30° प्रति घंटे के नियम से सिद्ध।" else "गलत कोणीय माप।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Validated by 30° per hour rule." else "Incorrect angular degree." },
            expertAdviceHindi = "याद रखें: प्रत्येक घंटे के निशान के बीच 30 अंश का कोण होता है।",
            expertAdviceEnglish = "Remember each one-hour tick represents exactly 30 degrees.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "30° के गुणज से असंगत विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Inconsistent angular options discarded.",
            diagramType = "clock_angle",
            diagramData = "hour:$hours,min:0",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("clock_angle", "h$hours"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Geometry", "Clock Cyclic Angles", "clock_dial"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("spatial_reasoning", "angular_geometry"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 4. ECOLOGICAL FOOD CHAIN (Tiers 1 - 4, Junior Focus)
    // =========================================================================
    private fun generateFoodChain(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val chains = listOf(
            Triple("घास (Grass)", "हिरण (Deer)", "बाघ (Tiger)"),
            Triple("पत्ते (Leaves)", "कीड़ा (Caterpillar)", "पक्षी (Bird)"),
            Triple("अनाज (Grain)", "चूहा (Rat)", "उल्लू (Owl)")
        )
        val chain = chains.random(rand)
        val qHi = "एक पारितंत्र में खाद्य श्रृंखला: ${chain.first} -> ${chain.second} -> ${chain.third} है। यदि अत्यधिक शिकार के कारण ${chain.third} की संख्या अचानक शून्य हो जाए, तो निकट भविष्य में ${chain.second} की संख्या पर क्या प्राथमिक प्रभाव पड़ेगा?"
        val qEn = "In an ecosystem food chain: ${chain.first} -> ${chain.second} -> ${chain.third}. If top predator (${chain.third}) population suddenly drops to zero, what will be the immediate primary effect on the population of ${chain.second}?"

        val correctStrHi = "संख्या तेजी से बढ़ेगी (Population will increase rapidly)"
        val correctStrEn = "Population will increase rapidly due to lack of predation"
        val optsEn = listOf(
            correctStrEn,
            "Population will decrease immediately to zero",
            "No change in population at all",
            "They will immediately transform into carnivores"
        ).shuffled(rand)
        val optsHi = optsEn.map { if (it == correctStrEn) correctStrHi else "गलत पारिस्थितिकी प्रभाव" }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Ecological Energy Flow & Food Web",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("शिकारी की अनुपस्थिति में शाकाहारी की मृत्यु दर घट जाती है।", "भोजन (${chain.first}) की प्रचुरता के कारण प्रजनन बढ़ेगा।"),
            cluesEnglish = listOf("Without the apex predator, mortality rate drops.", "Surplus producer vegetation allows rapid reproduction."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "शिकारी के अभाव में शाकाहारी जीवों पर जैविक नियंत्रण समाप्त हो जाता है, जिससे उनकी संख्या में तीव्र वृद्धि होती है।",
            deductionPathEnglish = "Removing predatory top-down pressure leads directly to unchecked rapid population surge.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "पारिस्थितिकीय नियम द्वारा सिद्ध।" else "पारिस्थितिकीय संतुलन के विपरीत।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Confirmed by ecological population dynamics." else "Contradicts predator-prey principles." },
            expertAdviceHindi = "शिकारी और शिकार के संतुलन के सिद्धांत पर विचार करें।",
            expertAdviceEnglish = "Apply the trophic predator-prey feedback dynamic.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत पारिस्थितिक विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Unbiological assertions discarded.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("food_chain_predator", chain.first),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Ecology", "Trophic Cascade", "food_web"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("cause_and_effect", "ecological_dynamic"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 5. CALENDAR CYCLIC (Tiers 2 - 6)
    // =========================================================================
    private fun generateCalendarCyclic(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val days = listOf("सोमवार (Monday)" to "Monday", "मंगलवार (Tuesday)" to "Tuesday", "बुधवार (Wednesday)" to "Wednesday", "गुरुवार (Thursday)" to "Thursday", "शुक्रवार (Friday)" to "Friday")
        val startDay = days.random(rand)
        val daysToAdd = listOf(15, 22, 29, 36).random(rand)
        val rem = daysToAdd % 7
        val allDayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val startIdx = allDayNames.indexOf(startDay.second)
        val targetIdx = (startIdx + rem) % 7
        val targetDayEn = allDayNames[targetIdx]
        val targetDayHi = when (targetDayEn) {
            "Monday" -> "सोमवार (Monday)"
            "Tuesday" -> "मंगलवार (Tuesday)"
            "Wednesday" -> "बुधवार (Wednesday)"
            "Thursday" -> "गुरुवार (Thursday)"
            "Friday" -> "शुक्रवार (Friday)"
            "Saturday" -> "शनिवार (Saturday)"
            else -> "रविवार (Sunday)"
        }

        val qHi = "यदि किसी सामान्य वर्ष में आज ${startDay.first} है, तो आज से ठीक $daysToAdd दिनों के बाद कौन सा दिन होगा?"
        val qEn = "If today is ${startDay.second}, which day of the week will it be exactly $daysToAdd days from today?"

        val optsEn = listOf(targetDayEn, allDayNames[(targetIdx + 1) % 7], allDayNames[(targetIdx + 2) % 7], allDayNames[(targetIdx + 6) % 7]).distinct().shuffled(rand)
        val optsHi = optsEn.map { en ->
            when (en) {
                "Monday" -> "सोमवार (Monday)"
                "Tuesday" -> "मंगलवार (Tuesday)"
                "Wednesday" -> "बुधवार (Wednesday)"
                "Thursday" -> "गुरुवार (Thursday)"
                "Friday" -> "शुक्रवार (Friday)"
                "Saturday" -> "शनिवार (Saturday)"
                else -> "रविवार (Sunday)"
            }
        }
        val correctIdx = optsEn.indexOf(targetDayEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(targetDayEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Calendar Modulo & Day Cycles",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("सप्ताह में 7 दिन होते हैं, अतः चक्र 7 से विभाजित होता है।", "$daysToAdd ÷ 7 = ${daysToAdd / 7} पूर्ण सप्ताह, शेषफल = $rem दिन।", "प्रारंभिक दिन (${startDay.first}) में शेषफल $rem दिन जोड़ें।"),
            cluesEnglish = listOf("7 days per week constitutes cyclic modulus 7.", "$daysToAdd ÷ 7 yields remainder $rem days.", "Advance start day (${startDay.second}) by $rem days."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "$daysToAdd = (${daysToAdd / 7} × 7) + $rem। अतः ${startDay.first} से $rem दिन आगे बढ़ने पर $targetDayHi आता है।",
            deductionPathEnglish = "$daysToAdd mod 7 = $rem. Shifting forward from ${startDay.second} by $rem days arrives at $targetDayEn.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "सटीक शेषफल गणना द्वारा सिद्ध।" else "गलत शेषफल जोड़।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Direct result of modular division." else "Arithmetic remainder mismatch." },
            expertAdviceHindi = "दिनों की संख्या को 7 से विभाजित करें और केवल शेषफल पर ध्यान दें।",
            expertAdviceEnglish = "Divide total days by 7 and advance only by the remainder.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "गलत शेषफल वाले विकल्प हटा दिए गए।",
            fiftyFiftyProofEnglish = "Non-matching remainder days eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("calendar_mod7", "${startDay.second}_$daysToAdd"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Arithmetic", "Cyclic Modulo 7", "calendar"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "modular_arithmetic"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 6. NUMBER SEQUENCE (Tiers 1 - 5)
    // =========================================================================
    private fun generateNumberSequence(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val diff = listOf(3, 4, 5, 6, 7).random(rand)
        val start = listOf(2, 5, 7, 11).random(rand)
        val seq = listOf(start, start + diff, start + 2 * diff, start + 3 * diff)
        val nextVal = start + 4 * diff

        val qHi = "दी गई संख्या श्रृंखला में अगला पद क्या होगा: ${seq.joinToString(", ")} , ?"
        val qEn = "What is the next number in the sequence: ${seq.joinToString(", ")}, ?"

        val correctStr = nextVal.toString()
        val optsEn = listOf(correctStr, (nextVal + diff).toString(), (nextVal - 2).toString(), (nextVal + 1).toString()).distinct().shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Mathematical Sequence & Pattern",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("क्रमिक पदों का अंतर ज्ञात करें: ${seq[1]} - ${seq[0]} = $diff।", "श्रृंखला में प्रत्येक पद $diff से बढ़ रहा है (समानांतर श्रेणी)।"),
            cluesEnglish = listOf("Find constant difference: ${seq[1]} - ${seq[0]} = $diff.", "Every successive step increases by an arithmetic constant of $diff."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "प्रत्येक पद में $diff का निश्चित अंतर है: ${seq.last()} + $diff = $nextVal।",
            deductionPathEnglish = "Constant step increment = $diff. Thus next term = ${seq.last()} + $diff = $nextVal.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "समानांतर श्रेणी अंतर $diff द्वारा सिद्ध।" else "गलत पद गणना।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Validated by arithmetic progression d = $diff." else "Fails progression rule." },
            expertAdviceHindi = "लगातार दो संख्याओं के बीच का अंतर निकालें।",
            expertAdviceEnglish = "Calculate the common difference between adjacent terms.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "श्रृंखला अंतर का उल्लंघन करने वाले विकल्प हटाए गए।",
            fiftyFiftyProofEnglish = "Inconsistent step values removed.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("seq_ap", "${start}_$diff"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Sequences", "Arithmetic Progression", "ap_series"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("pattern_recognition", "linear_difference"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 8. WORD CIPHER (Tiers 6 - 8)
    // =========================================================================
    private fun generateWordCipher(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val shift = listOf(1, 2, 3).random(rand)
        val words = listOf("LOGIC", "SMART", "BRAIN", "THINK", "SOLVE")
        val word = words.random(rand)
        val coded = word.map { (('A'.code + ((it.code - 'A'.code + shift) % 26)).toChar()) }.joinToString("")
        val testWord = "TARK"
        val correctCoded = testWord.map { (('A'.code + ((it.code - 'A'.code + shift) % 26)).toChar()) }.joinToString("")

        val qHi = "एक निश्चित कूट भाषा में यदि '$word' को '$coded' लिखा जाता है, तो उसी नियम के अनुसार 'TARK' को किस प्रकार लिखा जाएगा?"
        val qEn = "In a certain substitution code, if '$word' is coded as '$coded', how will the word 'TARK' be coded using the exact same rule?"

        val optsEn = listOf(correctCoded, "UBSL", "SCQJ", "VBTK").distinct().shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctCoded).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctCoded)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Cryptographic Letter-Shift Cipher",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("पहले अक्षर का अंतर देखें: ${word[0]} -> ${coded[0]} (+$shift का बदलाव)।", "सभी अक्षरों में समान रूप से +$shift जोड़ा गया है।"),
            cluesEnglish = listOf("Compare first letter: ${word[0]} -> ${coded[0]} is a shift of +$shift.", "Every letter uniformly advances by +$shift positions in alphabet."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "नियम: प्रत्येक अक्षर +$shift आगे बढ़ता है। T(20)+$shift, A(1)+$shift, R(18)+$shift, K(11)+$shift = $correctCoded।",
            deductionPathEnglish = "Caesar shift of +$shift. TARK with +$shift produces $correctCoded.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "+$shift शिफ्ट नियम से सिद्ध।" else "गलत अक्षर विस्थापन।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived via uniform +$shift shift." else "Inconsistent shift offset." },
            expertAdviceHindi = "वर्णमाला क्रम में अक्षरों की स्थिति (A=1, B=2...) लिखकर अंतर जांचें।",
            expertAdviceEnglish = "Map letters to numerical indices (A=1, Z=26) to identify the constant offset.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत शिफ्ट वाले विकल्प हटा दिए गए।",
            fiftyFiftyProofEnglish = "Invalid offset codes discarded.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("word_cipher", "${word}_$shift"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Cryptography", "Caesar Shift", "letter_substitution"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("pattern_recognition", "alphabetic_shift"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 9. RIVER CROSSING (Tiers 6 - 9)
    // =========================================================================
    private fun generateRiverCrossing(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val people = listOf(4, 5, 6).random(rand)
        val boatCapacity = 2
        val minTrips = 2 * people - 3

        val qHi = "$people व्यक्तियों को एक नाव से नदी पार करनी है जो एक बार में अधिकतम $boatCapacity व्यक्तियों को ले जा सकती है। नाव को चलाने के लिए कम से कम 1 व्यक्ति का सवार होना अनिवार्य है। सभी $people व्यक्तियों को दूसरे किनारे पहुंचाने के लिए नाव को कम से कम कितने एकल फेरे (one-way trips) लगाने होंगे?"
        val qEn = "$people people must cross a river using a boat that holds at most $boatCapacity people at a time. At least 1 person must row the boat back each time. What is the MINIMUM number of one-way trips required to get all $people people across?"

        val correctStr = minTrips.toString()
        val optsEn = listOf(correctStr, (minTrips + 2).toString(), (minTrips - 1).toString(), (2 * people).toString()).distinct().shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Constraint Optimization & River Crossing",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("प्रत्येक आगे जाने वाले फेरे (2 लोग) के बाद 1 व्यक्ति को नाव वापस लानी होगी।", "प्रत्येक राउंड ट्रिप (जाना + आना = 2 फेरे) में शुद्ध रूप से 1 व्यक्ति पार होता है।", "अंतिम फेरे में 2 लोग एक साथ पार होकर रुक जाते हैं (वापसी की आवश्यकता नहीं)।"),
            cluesEnglish = listOf("Each forward crossing carries 2, but 1 must row back.", "Net progress per round-trip (2 trips) is exactly 1 person across.", "The final trip takes the last 2 people across with no return trip needed."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "पहले ${people - 2} लोगों को पार कराने में 2 × ${people - 2} = ${2 * (people - 2)} फेरे + अंतिम 1 फेरा = $minTrips फेरे।",
            deductionPathEnglish = "Formula: 2(N - 2) + 1 = 2N - 3. For N = $people, minimum trips = $minTrips.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "गणितीय अनुकूलन सूत्र 2N - 3 द्वारा सिद्ध।" else "नियमों के उल्लंघन या अतिरिक्त फेरों के कारण गलत।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived via optimal crossing induction 2N - 3." else "Fails boundary condition." },
            expertAdviceHindi = "ध्यान दें: अंतिम फेरे में नाव को वापस लाने की आवश्यकता नहीं होती।",
            expertAdviceEnglish = "Account for the fact that the last crossing does not require a return leg.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत ट्रिप संख्या निरस्त।",
            fiftyFiftyProofEnglish = "Suboptimal trip counts eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("river_crossing", "n$people"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Combinatorics", "River Crossing Optimization", "min_trips"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deduction", "optimization_recurrence"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 10. VENN SETS (Tiers 6 - 10)
    // =========================================================================
    private fun generateVennSets(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val total = 100
        val nA = listOf(50, 60).random(rand)
        val nB = listOf(40, 50).random(rand)
        val both = listOf(20, 25).random(rand)
        val neither = total - (nA + nB - both)

        val qHi = "100 विद्यार्थियों की एक कक्षा में $nA विद्यार्थी गणित (Maths) पसंद करते हैं और $nB विद्यार्थी विज्ञान (Science) पसंद करते हैं। यदि $both विद्यार्थी दोनों विषय पसंद करते हैं, तो कितने विद्यार्थी इन दोनों में से कोई भी विषय पसंद नहीं करते?"
        val qEn = "In a group of 100 students, $nA like Mathematics and $nB like Science. If $both like both subjects, how many students like NEITHER Mathematics nor Science?"

        val correctStr = neither.toString()
        val optsEn = listOf(correctStr, (neither + 5).toString(), (neither - 5).toString(), (100 - (nA + nB)).coerceAtLeast(5).toString()).distinct().shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Set Theory & 3-Circle Venn Logic",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("n(A ∪ B) = n(A) + n(B) - n(A ∩ B)", "कम से कम एक विषय पसंद करने वाले = $nA + $nB - $both = ${nA + nB - both}", "कोई नहीं = कुल (100) - n(A ∪ B)"),
            cluesEnglish = listOf("Inclusion-Exclusion: n(A ∪ B) = n(A) + n(B) - n(A ∩ B)", "Union = $nA + $nB - $both = ${nA + nB - both}", "Neither = 100 - Union = $neither"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "n(A ∪ B) = $nA + $nB - $both = ${nA + nB - both}। कोई नहीं = 100 - ${nA + nB - both} = $neither।",
            deductionPathEnglish = "Union = $nA + $nB - $both = ${nA + nB - both}. Complement = 100 - ${nA + nB - both} = $neither.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "वेन आरेख समावेशन-अपवर्जन सूत्र द्वारा सिद्ध।" else "गलत समुच्चय गणना।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived via Principle of Inclusion-Exclusion." else "Double-counting fallacy." },
            expertAdviceHindi = "दोनों विषयों को पसंद करने वालों को दो बार गिनने से बचें (उभयनिष्ठ भाग घटाएं)।",
            expertAdviceEnglish = "Subtract the intersection once to avoid double-counting.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "दोहरी गणना वाले त्रुटिपूर्ण विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Double-counted values eliminated.",
            diagramType = "venn_logic",
            diagramData = "A:$nA,B:$nB,both:$both,total:100",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("venn_inclusion_exclusion", "${nA}_${nB}_$both"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Set Theory", "Inclusion-Exclusion", "venn_complement"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deduction", "set_intersection"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 11. SPEED DISTANCE (Tiers 6 - 10)
    // =========================================================================
    private fun generateSpeedDistance(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val s1 = listOf(40, 50, 60).random(rand)
        val s2 = listOf(20, 30, 40).random(rand)
        val dist = (s1 + s2) * 2

        val qHi = "दो ट्रेनें स्टेशन A और स्टेशन B से, जिनके बीच की दूरी $dist किमी है, एक ही समय में एक-दूसरे की ओर क्रमशः $s1 किमी/घंटा और $s2 किमी/घंटा की गति से चलती हैं। वे कितने घंटे बाद एक-दूसरे से मिलेंगी?"
        val qEn = "Two trains start at the same time towards each other from stations A and B, which are $dist km apart, at speeds of $s1 km/h and $s2 km/h respectively. After how many hours will they meet?"

        val timeHours = 2
        val correctStr = "$timeHours Hours (2 घंटे)"
        val correctStrEn = "$timeHours Hours"
        val optsEn = listOf(correctStrEn, "3 Hours", "1.5 Hours", "4 Hours").shuffled(rand)
        val optsHi = optsEn.map { it.replace("Hours", "घंटे") }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Kinematics & Relative Speed",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("विपरीत दिशा में सापेक्ष गति = S1 + S2 = $s1 + $s2 = ${s1 + s2} किमी/घंटा", "समय = कुल दूरी ÷ सापेक्ष चाल = $dist ÷ ${s1 + s2} = $timeHours घंटे"),
            cluesEnglish = listOf("Relative speed in opposite directions = S1 + S2 = ${s1 + s2} km/h", "Meeting Time = Total Distance / Relative Speed = $dist / ${s1 + s2} = $timeHours hours"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "सापेक्ष चाल = ${s1 + s2} किमी/घंटा। समय = $dist / ${s1 + s2} = $timeHours घंटे।",
            deductionPathEnglish = "Relative velocity = $s1 + $s2 = ${s1 + s2} km/h. Meeting time = $dist / ${s1 + s2} = $timeHours hours.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "सापेक्ष गति सूत्र द्वारा सिद्ध।" else "असंगत चाल-दूरी अनुपात।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Directly derived from relative velocity." else "Incorrect velocity summation." },
            expertAdviceHindi = "जब दो वस्तुएं एक-दूसरे की ओर बढ़ती हैं, तो उनकी गतियां जुड़ जाती हैं।",
            expertAdviceEnglish = "When two objects move towards each other, sum their velocities for relative speed.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत समय वाले विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Non-matching time intervals eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("relative_speed_opposite", "${s1}_${s2}_$dist"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Kinematics", "Relative Velocity", "trains_meeting"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "relative_motion"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 12. AGE ALGEBRA (Tiers 6 - 10)
    // =========================================================================
    private fun generateAgeAlgebra(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        // Verified (multiplier m, years Y) pairs: parent = m*x now and
        // m*x + Y = 2*(x + Y) exactly, so x = Y/(m-2) is always an integer.
        val cases = listOf(
            Triple(3, 10, 10), Triple(3, 15, 15), Triple(3, 20, 20),
            Triple(4, 10, 5), Triple(4, 12, 6), Triple(4, 16, 8), Triple(4, 20, 10),
            Triple(5, 9, 3), Triple(5, 12, 4), Triple(5, 15, 5), Triple(5, 18, 6),
            Triple(6, 8, 2), Triple(6, 12, 3), Triple(6, 16, 4), Triple(6, 20, 5),
            Triple(7, 15, 3), Triple(7, 20, 4), Triple(7, 25, 5)
        )
        val (m, years, childAge) = cases.random(rand)
        val rels = listOf(
            Triple("father", "son", "पिता/पुत्र"),
            Triple("mother", "daughter", "माता/पुत्री"),
            Triple("uncle", "nephew", "चाचा/भतीजा")
        )
        val rel = rels.random(rand)
        val parentHi = rel.third.split("/")[0]
        val childHi = rel.third.split("/")[1]

        val qHi = "एक $parentHi की वर्तमान आयु अपने $childHi की वर्तमान आयु की $m गुनी है। $years वर्ष बाद $parentHi की आयु $childHi की आयु की ठीक दोगुनी हो जाएगी। $childHi की वर्तमान आयु क्या है?"
        val qEn = "A ${rel.first}'s current age is $m times their ${rel.second}'s current age. In $years years, the ${rel.first} will be exactly twice as old as the ${rel.second}. What is the ${rel.second}'s current age?"

        val correctStr = "$childAge Years"
        val optsEn = listOf(correctStr, "${childAge + 2} Years", "${childAge + 4} Years", "${maxOf(1, childAge - 2)} Years").distinct().shuffled(rand)
        val optsHi = optsEn.map { it.replace("Years", "वर्ष") }
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Age-Algebra Linear System",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("माना $childHi = x, $parentHi = ${m}x", "$years वर्ष बाद: ${m}x + $years = 2(x + $years)", "(${m} - 2)x = $years => x = $childAge"),
            cluesEnglish = listOf("Let ${rel.second} = x, ${rel.first} = ${m}x", "In $years yrs: ${m}x + $years = 2(x + $years)", "(${m} - 2)x = $years => x = $childAge"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "${m}x + $years = 2(x + $years) => x = $childAge वर्ष।",
            deductionPathEnglish = "Linear solution: ${m}x + $years = 2(x + $years) gives x = $childAge years.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "रैखिक समीकरण समाधान द्वारा सिद्ध।" else "समीकरण को संतुष्ट नहीं करता।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Satisfies the dual temporal linear equations." else "Fails algebraic substitution." },
            expertAdviceHindi = "$childHi की आयु x मानकर भविष्य के समीकरण की रचना करें।",
            expertAdviceEnglish = "Express both ages in terms of variable x and equate at the future checkpoint.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "समीकरण में असंगत मान निरस्त।",
            fiftyFiftyProofEnglish = "Non-satisfying age values eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("age_linear_eq", "m${m}_y${years}_${rel.first}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Algebra", "Linear Age System", "age_ratios"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "linear_equation"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 13. SUCCESSIVE PERCENTAGE (Tiers 6 - 10, Adult Focus)
    // =========================================================================
    private fun generateSuccessivePercentage(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val p = listOf(10, 20, 25).random(rand)
        val netInc = (2 * p + (p * p) / 100)

        val qHi = "एक वस्तु के मूल्य में लगातार दो बार $p% की क्रमिक वृद्धि की जाती है। मूल्य में प्रभावी कुल प्रतिशत वृद्धि क्या है?"
        val qEn = "The price of an item is increased by $p% sequentially twice. What is the effective net percentage increase?"

        val correctStr = "$netInc%"
        val optsEn = listOf(correctStr, "${2 * p}%", "${netInc - 1}%", "${netInc + 2}%").distinct().shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Quantitative Data Interpretation",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("क्रमिक वृद्धि सूत्र: A + B + (A × B)/100", "पहली वृद्धि = $p%, दूसरी वृद्धि = $p%", "नेट = $p + $p + ($p × $p)/100 = $netInc%"),
            cluesEnglish = listOf("Compound increment formula: A + B + (A × B)/100", "A = $p%, B = $p%", "Net = $p + $p + ($p × $p)/100 = $netInc%"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "नेट = $p + $p + ($p*$p)/100 = $netInc%",
            deductionPathEnglish = "Net = $p + $p + ($p*$p)/100 = $netInc%",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "क्रमिक प्रतिशत वृद्धि नियम द्वारा सिद्ध।" else "सरल योग भ्रांति।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Compounded percentage change law." else "Simple addition error." },
            expertAdviceHindi = "क्रमिक वृद्धि में चक्रवृद्धि प्रभाव को जोड़ना न भूलें।",
            expertAdviceEnglish = "Always account for the compound base escalation in successive percentages.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "सरल योग वाले भ्रामक विकल्प हटा दिए गए।",
            fiftyFiftyProofEnglish = "Simple non-compounded distractors eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("successive_percent", "p$p"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Quantitative Aptitude", "Successive Percentage", "compound_change"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "percentage_compounding"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 14. PYTHAGORAS VECTOR (Tiers 6 - 10)
    // =========================================================================
    private fun generatePythagorasVector(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val pairs = listOf(Pair(3, 4), Pair(6, 8), Pair(5, 12), Pair(9, 12), Pair(12, 16))
        val (d1, d2) = pairs.random(rand)
        val net = sqrt((d1 * d1 + d2 * d2).toDouble()).toInt()

        val qHi = "एक अन्वेषक बिंदु O से $d1 किमी उत्तर की ओर और फिर $d2 किमी पूर्व की ओर चलता है। प्रारंभिक बिंदु से उसकी न्यूनतम सीधी दूरी क्या है?"
        val qEn = "An investigator travels $d1 km North from point O, and then $d2 km East. What is the shortest straight-line distance from the starting point?"

        val correctStr = "$net km"
        val optsEn = listOf(correctStr, "${net + 2} km", "${net - 1} km", "${d1 + d2} km").distinct().shuffled(rand)
        val optsHi = optsEn.map { it.replace("km", "किमी") }
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Spatial Coordinate Vector & Pythagoras",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("उत्तर दिशा = Y-अक्ष ($d1 किमी)", "पूर्व दिशा = X-अक्ष ($d2 किमी)", "पाइथागोरस प्रमेय: d² = x² + y²"),
            cluesEnglish = listOf("North displacement = Y axis ($d1 km)", "East displacement = X axis ($d2 km)", "Pythagoras theorem: d² = x² + y²"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "d = √($d1² + $d2²) = √${d1*d1 + d2*d2} = $net किमी।",
            deductionPathEnglish = "d = √($d1² + $d2²) = $net km.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "पाइथागोरस प्रमेय द्वारा सिद्ध।" else "दूरी योग भ्रांति।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Hypotenuse displacement formula verified." else "Arithmetic summation error." },
            expertAdviceHindi = "सीधी दूरी के लिए विकर्ण (कर्ण) की गणना करें।",
            expertAdviceEnglish = "Compute the diagonal hypotenuse for the straight-line displacement.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत दूरी विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Invalid displacement options eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("pythagoras_vector", "${d1}_$d2"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Geometry", "Pythagorean Theorem", "vector_hypotenuse"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("spatial_reasoning", "orthogonal_hypotenuse"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 15. SEATING ARRANGEMENT (Tiers 7 - 10)
    // =========================================================================
    private fun generateSeatingArrangement(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val names = listOf("Aarav", "Bhavna", "Chirag", "Deepa", "Eshan").shuffled(rand)
        val middle = names[2]

        val qHi = "पाँच मित्र: ${names.joinToString(", ")} एक पंक्ति में उत्तर की ओर मुंह करके बैठे हैं। ${names[0]}, ${names[1]} के ठीक बाएँ है। ${names[4]}, ${names[3]} के ठीक दाएँ है। यदि ${names[2]} ठीक मध्य स्थान पर बैठा है, तो पंक्ति के केंद्र में कौन है?"
        val qEn = "Five friends: ${names.joinToString(", ")} sit in a row facing North. ${names[0]} is immediately to the left of ${names[1]}. ${names[4]} is immediately to the right of ${names[3]}. If ${names[2]} is in the exact center, who sits in the middle?"

        val correctStr = middle
        val optsEn = names.take(4).shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Linear & Circular Seating Logic",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("पंक्ति में कुल 5 स्थान हैं: 1, 2, 3, 4, 5", "मध्य स्थान = स्थान 3", "शर्त के अनुसार $middle सीधे केंद्र में है।"),
            cluesEnglish = listOf("Five positions in row: 1, 2, 3, 4, 5", "Center position = 3", "Condition unambiguously assigns $middle to center position."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "कथन द्वारा $middle को सीधे मध्य स्थान 3 पर स्थापित किया गया है।",
            deductionPathEnglish = "$middle occupies rank position 3 by direct conditional assignment.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "स्थिति निर्धारण द्वारा सिद्ध।" else "किनारे या गलत स्थिति।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Explicitly seated in center." else "Positioned on peripheral flank." },
            expertAdviceHindi = "पंक्ति के 5 खानों में दी गई शर्तों को सीधे भरें।",
            expertAdviceEnglish = "Draw 5 slots and place anchored center elements first.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "किनारे वाले विकल्प हटा दिए गए।",
            fiftyFiftyProofEnglish = "Edge flank seats eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("linear_seating", middle),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Positional Logic", "Linear Seating", "row_ranking"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("scenario_reasoning", "positional_grid"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 16. PIGEONHOLE PRINCIPLE (Tiers 11 - 14)
    // =========================================================================
    private fun generatePigeonholeDraw(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val red = listOf(8, 10, 12).random(rand)
        val blue = listOf(7, 9, 11).random(rand)
        val minDraw = 3

        val qHi = "एक अंधेरे कमरे में एक दराज में $red लाल मोजे और $blue नीले मोजे रखे हैं। बिना देखे, कम से कम कितने मोजे निकालने होंगे ताकि यह निश्चित हो सके कि आपके पास एक ही रंग का कम से कम एक जोड़ा (matching pair) अवश्य आ जाए?"
        val qEn = "A drawer in a dark room contains $red red socks and $blue blue socks. What is the MINIMUM number of socks you must pull out blindly to be 100% CERTAIN of having at least one matching pair of the same color?"

        val correctStr = "$minDraw Socks (मोजे)"
        val correctStrEn = "$minDraw Socks"
        val optsEn = listOf(correctStrEn, "${minDraw + 1} Socks", "${minDraw + 2} Socks", "$red Socks").distinct().shuffled(rand)
        val optsHi = optsEn.map { it.replace("Socks", "मोजे") }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Pigeonhole Principle (Dirichlet)",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("रंगों की कुल श्रेणियां (Pigeonholes) = 2 (लाल, नीला)", "सबसे खराब स्थिति (Worst Case): पहला मोजा लाल, दूसरा नीला (2 अलग-अलग रंग)", "तीसरा मोजा निकालते ही या तो लाल या नीले के साथ जोड़ा बन जाएगा (2 + 1 = 3)।"),
            cluesEnglish = listOf("Available color categories (holes) = 2 (Red, Blue)", "Worst-case scenario: 1 Red + 1 Blue (2 distinct socks)", "The 3rd sock must match either red or blue (Pigeonhole: N + 1 = 2 + 1 = 3)."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "डिरिचलेट कबूतर सिद्धांत: यदि n श्रेणियां हों तो एक जोड़ा पाने के लिए n + 1 = 2 + 1 = 3 मोजे पर्याप्त हैं।",
            deductionPathEnglish = "Pigeonhole principle: With N = 2 colors, extracting N + 1 = 3 items guarantees a pair.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "कबूतर सिद्धांत N + 1 द्वारा सिद्ध।" else "अत्यधिक या अपर्याप्त संख्या।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Guaranteed by Dirichlet pigeonhole principle." else "Violates worst-case bound." },
            expertAdviceHindi = "सबसे खराब स्थिति की कल्पना करें जहां प्रत्येक रंग का केवल एक मोजा निकला हो।",
            expertAdviceEnglish = "Focus on the worst-case scenario where each color is picked once.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "अपर्याप्त संख्या (2) और अत्यधिक संख्याएं निरस्त।",
            fiftyFiftyProofEnglish = "Insufficient and excessive quantities eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("pigeonhole_pair", "colors2"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Combinatorics", "Pigeonhole Principle", "worst_case_pair"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deduction", "pigeonhole_bound"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 17. SHADOW OPTICS (Tiers 11 - 14)
    // =========================================================================
    private fun generateShadowOptics(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val h1 = listOf(6, 9, 12).random(rand)
        val s1 = h1 / 3
        val h2 = listOf(15, 18, 24).random(rand)
        val s2 = h2 / 3

        val qHi = "दिन के एक निश्चित समय पर, $h1 मीटर ऊंचे एक खंभे की छाया की लंबाई $s1 मीटर है। ठीक उसी समय, पास में स्थित $h2 मीटर ऊंचे एक वृक्ष की छाया की लंबाई कितनी होगी?"
        val qEn = "At a certain time of day, a pole $h1 meters high casts a shadow of $s1 meters. At the exact same time, what will be the length of the shadow cast by a nearby tree that is $h2 meters high?"

        val correctStr = "$s2 Meters (मीटर)"
        val correctStrEn = "$s2 Meters"
        val optsEn = listOf(correctStrEn, "${s2 + 2} Meters", "${s2 - 1} Meters", "${h2 / 2} Meters").distinct().shuffled(rand)
        val optsHi = optsEn.map { it.replace("Meters", "मीटर") }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Solar Angle & Shadow Trigonometry",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("सूर्य का उन्नयन कोण दोनों वस्तुओं के लिए समान है।", "ऊंचाई और छाया का अनुपात: $h1 / $s1 = 3 : 1", "अतः वृक्ष की छाया = $h2 ÷ 3 = $s2 मीटर"),
            cluesEnglish = listOf("Solar elevation angle θ is identical for both objects.", "Height-to-shadow ratio: $h1 / $s1 = 3 : 1", "Tree shadow = $h2 / 3 = $s2 meters"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "tan θ = $h1 / $s1 = 3। वृक्ष की छाया = $h2 / 3 = $s2 मीटर।",
            deductionPathEnglish = "tan(θ) = $h1 / $s1 = 3. Hence tree shadow = $h2 / 3 = $s2 meters.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "समान सूर्य कोण त्रिकोणमिति द्वारा सिद्ध।" else "असंगत अनुपात।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived from constant sun angle proportion." else "Violates geometric similarity." },
            expertAdviceHindi = "ऊंचाई और छाया के बीच का स्थिर अनुपात निकालें।",
            expertAdviceEnglish = "Calculate the constant height-to-shadow ratio.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "अनुपात का उल्लंघन करने वाले विकल्प हटा दिए गए।",
            fiftyFiftyProofEnglish = "Inconsistent ratio values eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("shadow_ratio", "${h1}_${s1}_$h2"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Trigonometry", "Solar Elevation Ratio", "shadow_similarity"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("spatial_reasoning", "geometric_similarity"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 18. MATRIX ROTATION (Tiers 11 - 14)
    // =========================================================================
    private fun generateMatrixRotation(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        // 3x3 rotation is fully deterministic: 3 rotations x 4 queried corners
        // x 4 marked tokens give 48 distinct texts with exact answers.
        val cornersEn = listOf("Top-Left", "Top-Right", "Bottom-Right", "Bottom-Left")
        val cornersHi = mapOf(
            "Top-Left" to "शीर्ष-बाएँ",
            "Top-Right" to "शीर्ष-दाएँ",
            "Bottom-Right" to "निचले-दाएँ",
            "Bottom-Left" to "निचले-बाएँ"
        )
        // (r,c) -> after rotation, indexed by cornersEn order
        val rotCw = listOf("Top-Right", "Bottom-Right", "Bottom-Left", "Top-Left")
        val rotCcw = listOf("Bottom-Left", "Top-Left", "Top-Right", "Bottom-Right")
        val rot180 = listOf("Bottom-Right", "Bottom-Left", "Top-Left", "Top-Right")
        val rotations = listOf(
            Triple("90° clockwise", "घड़ी की सुई की दिशा (Clockwise) में 90°", rotCw),
            Triple("90° counter-clockwise", "घड़ी की सुई की विपरीत दिशा (Counter-Clockwise) में 90°", rotCcw),
            Triple("180°", "180°", rot180)
        )
        val rotation = rotations.random(rand)
        val cornerIdx = rand.nextInt(4)
        val fromCorner = cornersEn[cornerIdx]
        val toCorner = rotation.third[cornerIdx]
        val token = listOf("star", "diamond", "circle", "triangle").random(rand)
        val tokenHi = mapOf("star" to "तारा", "diamond" to "हीरा", "circle" to "वृत्त", "triangle" to "त्रिभुज")[token]!!

        val qHi = "एक 3x3 ग्रिड में $tokenHi चिह्न ${cornersHi[fromCorner]} ($fromCorner) कोने पर अंकित है। यदि पूरे ग्रिड को ${rotation.second} घुमाया जाता है, तो यह चिह्न नए ग्रिड में किस स्थान पर स्थानांतरित होगा?"
        val qEn = "In a 3x3 grid, a $token symbol is marked at the ${fromCorner.uppercase()} corner. If the entire grid is rotated ${rotation.first}, to which corner does the symbol move?"

        val correctStrEn = toCorner
        val optsEn = cornersEn.shuffled(rand)
        val optsHi = optsEn.map { "$it (${cornersHi[it]})" }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "3x3 Matrix Grid Transformation",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("घूर्णन नियम प्रत्येक कोने को एक निश्चित नए कोने पर ले जाता है", "प्रारंभिक स्थान: $fromCorner", "${rotation.first} घूर्णन के बाद: $toCorner"),
            cluesEnglish = listOf("Each rotation maps every corner to a fixed new corner", "Initial position: $fromCorner", "After ${rotation.first}: position becomes $toCorner"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "$fromCorner -> $toCorner (${rotation.first} घूर्णन)।",
            deductionPathEnglish = "The ${rotation.first} transformation maps $fromCorner to $toCorner.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "${rotation.first} घूर्णन ज्यामिति द्वारा सिद्ध।" else "भिन्न घूर्णन या दिशा का स्थान।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Direct result of the ${rotation.first} rotation." else "Corresponds to a different rotation or direction." },
            expertAdviceHindi = "कोने को घूर्णन दिशा में मानसिक रूप से घुमाएं।",
            expertAdviceEnglish = "Mentally trace the corner through the rotation sweep.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "भिन्न घूर्णन के स्थान निरस्त।",
            fiftyFiftyProofEnglish = "Positions of other rotations eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("matrix_rotate", "${rotation.first}_${fromCorner}_${token}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Linear Algebra", "Matrix Rotation", "coordinate_transform"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("spatial_reasoning", "matrix_rotation"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 19. CRYPTARITHM (Tiers 12 - 15)
    // =========================================================================
    private fun generateCryptarithm(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        // LL' + L'L = 11 * (L + L'). For digit sum k in 3..17 a distinct
        // non-zero digit pair always exists, so "value of L + L'" is definite.
        // 15 sums x 12 letter pairs = 180 unique texts.
        val k = (3..17).random(rand)
        val sum = 11 * k
        val letterPairs = listOf(
            "A" to "B", "X" to "Y", "M" to "N", "P" to "Q",
            "R" to "S", "C" to "D", "E" to "F", "G" to "H",
            "J" to "K", "T" to "U", "V" to "W", "L" to "Z"
        )
        val (l1, l2) = letterPairs.random(rand)

        val qHi = "अंकगणितीय कूट पहेली में यदि: $l1$l2 + $l2$l1 = $sum है, जहां $l1 और $l2 दो अलग-अलग गैर-शून्य अंक (1-9) हैं, तो $l1 + $l2 का निश्चित मान क्या होगा?"
        val qEn = "In the alphametic cryptarithm: $l1$l2 + $l2$l1 = $sum, where $l1 and $l2 represent distinct non-zero digits (1-9), what is the definite value of $l1 + $l2?"

        val correctStr = "$k"
        val distractors = listOf(k + 1, k - 1, k + 2, k - 2).filter { it in 2..18 && it != k }.distinct().take(3)
        val optsEn = (listOf(correctStr) + distractors.map { "$it" }).shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Alphametic Cryptarithm",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("$l1$l2 = 10$l1 + $l2 और $l2$l1 = 10$l2 + $l1", "$l1$l2 + $l2$l1 = 11($l1 + $l2) = $sum", "$l1 + $l2 = $sum ÷ 11 = $k"),
            cluesEnglish = listOf("$l1$l2 = 10$l1 + $l2; $l2$l1 = 10$l2 + $l1", "Sum = 11($l1 + $l2) = $sum", "$l1 + $l2 = $sum ÷ 11 = $k"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "11($l1 + $l2) = $sum => $l1 + $l2 = $k।",
            deductionPathEnglish = "11($l1+$l2) = $sum yields $l1 + $l2 = $k.",
            eliminationReasonsHindi = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "आधार 10 विस्तार द्वारा सिद्ध।" else "योग $sum को संतुष्ट नहीं करता।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived via 11($l1+$l2) = $sum." else "Violates the base-10 decomposition." },
            expertAdviceHindi = "दहाई और इकाई के स्थानीय मानों (10$l1 + $l2) का विस्तार करें।",
            expertAdviceEnglish = "Expand positional place values into 10$l1 + $l2.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत अंकों के विकल्प हटा दिए गए।",
            fiftyFiftyProofEnglish = "Invalid digit options eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("cryptarithm_ab_ba", "sum${k}_${l1}${l2}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Number Theory", "Positional Base 10 Cryptarithm", "alphametic"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "positional_expansion"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 20. FORENSIC TIMELINE (Tiers 12 - 15)
    // =========================================================================
    private fun generateForensicTimeline(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        // The correct alibi is always the far-away timestamped CCTV sighting
        // whose window covers the crime time; the others fail on proximity or
        // lack of witnesses. Names, times, distances and places all vary.
        fun fmt(h24: Int, min: Int): String {
            val ampm = if (h24 < 12) "AM" else "PM"
            val h = if (h24 % 12 == 0) 12 else h24 % 12
            return String.format("%d:%02d %s", h, min, ampm)
        }
        fun fmtHi(h24: Int, min: Int): String {
            val ampm = if (h24 < 12) "सुबह" else if (h24 < 17) "दोपहर" else "शाम"
            val h = if (h24 % 12 == 0) 12 else h24 % 12
            return "$ampm $h:" + String.format("%02d", min)
        }
        val names = listOf("Sumit" to "सुमित", "Aman" to "अमन", "Rahul" to "राहुल", "Vikram" to "विक्रम", "Neha" to "नेहा", "Priya" to "प्रिया", "Arjun" to "अर्जुन", "Kabir" to "कबीर", "Rohan" to "रोहन", "Meera" to "मीरा", "Imran" to "इमरान", "Farhan" to "फरहान").shuffled(rand)
        val n1 = names[0]; val n2 = names[1]; val n3 = names[2]
        val crimeH = (13..22).random(rand)
        val crimeM = listOf(0, 15, 30, 45).random(rand)
        val dist = listOf(80, 100, 120, 150, 200).random(rand)
        val margin1 = listOf(10, 15, 20, 30).random(rand)
        val margin2 = listOf(10, 15, 20, 30).random(rand)
        val cctvStart = crimeH * 60 + crimeM - margin1
        val cctvEnd = crimeH * 60 + crimeM + margin2
        val nearDist = listOf(1, 2, 3).random(rand)
        // Near suspect's window deliberately does NOT cover the crime minute.
        val nearStart = crimeH * 60 + crimeM + 5
        val nearEnd = nearStart + 15
        val place = listOf("airport" to "हवाई अड्डे", "railway station" to "रेलवे स्टेशन", "metro station" to "मेट्रो स्टेशन").random(rand)

        val qEn = "A crime occurs at ${fmt(crimeH, crimeM)} in the city center. Three suspects present the following alibis:
1. ${n1.first}: 'I was on CCTV at ${if (place.first == "airport") "an" else "a"} ${place.first} $dist km away from ${fmt(cctvStart / 60, cctvStart % 60)} to ${fmt(cctvEnd / 60, cctvEnd % 60)}.'
2. ${n2.first}: 'I was in a bank $nearDist km from the crime scene from ${fmt(nearStart / 60, nearStart % 60)} to ${fmt(nearEnd / 60, nearEnd % 60)}.'
3. ${n3.first}: 'I was sleeping at home with no witnesses.'
Which suspect's alibi is physically and geographically the most indisputable?"
        val qHi = "एक अपराध ${fmtHi(crimeH, crimeM)} बजे शहर के केंद्र में हुआ। तीन संदिग्धों के बयान निम्नलिखित हैं:
1. ${n1.second}: 'मैं ${fmtHi(cctvStart / 60, cctvStart % 60)} से ${fmtHi(cctvEnd / 60, cctvEnd % 60)} तक $dist किमी दूर ${place.second} पर सीसीटीवी में दिखा/दिखी।'
2. ${n2.second}: 'मैं ${fmtHi(nearStart / 60, nearStart % 60)} से ${fmtHi(nearEnd / 60, nearEnd % 60)} तक अपराध स्थल से $nearDist किमी दूर बैंक में था/थी।'
3. ${n3.second}: 'मैं बिना किसी गवाह के घर पर सो रहा/रही था/थी।'
किस संदिग्ध का बहाना (Alibi) भौतिक एवं भौगोलिक रूप से सबसे अभेद्य (unbreakable) है?"

        val correctStrEn = "${n1.first} - Timestamped CCTV $dist km away covering the crime time"
        val correctStr = "${n1.second} - अपराध के समय $dist किमी दूर सीसीटीवी पुष्टि"
        val optsEn = listOf(
            correctStrEn,
            "${n2.first} - Bank $nearDist km away",
            "${n3.first} - Unwitnessed sleep at home",
            "None of them have a valid alibi"
        ).shuffled(rand)
        val optsHi = optsEn.map {
            when (it) {
                correctStrEn -> correctStr
                "${n2.first} - Bank $nearDist km away" -> "${n2.second} - $nearDist किमी दूर बैंक"
                "${n3.first} - Unwitnessed sleep at home" -> "${n3.second} - घर पर बिना गवाह नींद"
                else -> "किसी का भी वैध बहाना नहीं है"
            }
        }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Forensic Timeline Analysis",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("${n1.second} अपराध के ठीक समय $dist किमी दूर सीसीटीवी पर था/थी", "${n2.second} का बैंक का समय-सीमा अपराध के क्षण को ढकती नहीं है", "${n3.second} का कोई स्वतंत्र गवाह नहीं है"),
            cluesEnglish = listOf("${n1.first} was on CCTV $dist km away exactly at the crime time", "${n2.first}'s bank window does not cover the crime minute", "${n3.first} has no independent witness"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "अपराध के क्षण $dist किमी की दूरी तय करना भौतिक रूप से असंभव है, और सीसीटीवी निष्पक्ष पुष्टि करता है।",
            deductionPathEnglish = "Covering $dist km at the crime minute is physically impossible, and timestamped CCTV independently confirms ${n1.first}'s location.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "भौतिक गति असंभवता व निष्पक्ष सीसीटीवी द्वारा सिद्ध।" else "समीपता, समय-अंतर या साक्षी की कमी के कारण कमजोर।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Indisputable due to physical speed limits and video telemetry." else "Vulnerable to proximity, time gaps, or lack of corroboration." },
            expertAdviceHindi = "दूरी और समय के संबंध में भौतिक गति की सीमा का विश्लेषण करें।",
            expertAdviceEnglish = "Check whether each alibi's time window covers the crime minute and whether travel was physically possible.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "कमजोर और अपुष्ट बहाने हटा दिए गए।",
            fiftyFiftyProofEnglish = "Unverified alibis discarded.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("forensic_alibi", "${n1.first}_${crimeH}_${crimeM}_${dist}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Forensic Logic", "Alibi Invalidation", "timeline_analysis"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("scenario_reasoning", "forensic_contradiction"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 21. PROBABILITY RISK (Tiers 11 - 15)
    // =========================================================================
    private fun generateProbabilityRisk(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val red = listOf(3, 4).random(rand)
        val blue = listOf(3, 4).random(rand)
        val total = red + blue
        val numerator = red * (red - 1)
        val denominator = total * (total - 1)

        val qHi = "एक थैले में $red लाल और $blue नीली गेंदें हैं। बिना प्रतिस्थापन (without replacement) के लगातार दो गेंदें निकाली जाती हैं। दोनों गेंदों के लाल होने की प्रायिकता क्या है?"
        val qEn = "A bag contains $red red balls and $blue blue balls. Two balls are drawn sequentially without replacement. What is the probability that both drawn balls are RED?"

        val correctStr = "$numerator / $denominator"
        val optsEn = listOf(correctStr, "${numerator + 1} / $denominator", "${red * red} / ${total * total}", "1 / 2").distinct().shuffled(rand)
        val optsHi = optsEn
        val correctIdx = optsEn.indexOf(correctStr).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStr)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Probability & Risk Trees",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("पहली लाल गेंद की प्रायिकता = $red / $total", "दूसरी लाल गेंद (बिना प्रतिस्थापन) = ${red - 1} / ${total - 1}", "कुल प्रायिकता = ($red / $total) × (${red - 1} / ${total - 1})"),
            cluesEnglish = listOf("P(1st Red) = $red / $total", "P(2nd Red | 1st Red) = ${red - 1} / ${total - 1}", "Combined = ($red/$total) × (${red-1}/${total-1}) = $numerator / $denominator"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "P = ($red / $total) × (${red - 1} / ${total - 1}) = $numerator / $denominator।",
            deductionPathEnglish = "P = ($red/$total) × (${red-1}/${total-1}) = $numerator/$denominator.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "सशर्त प्रायिकता गुणन नियम द्वारा सिद्ध।" else "प्रतिस्थापन सहित की गलत गणना।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived via conditional sequential probability." else "Assumes replacement erroneously." },
            expertAdviceHindi = "पहली गेंद निकलने के बाद कुल गेंदों की संख्या में 1 की कमी का ध्यान रखें।",
            expertAdviceEnglish = "Remember that the sample space shrinks by 1 after the first draw without replacement.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "प्रतिस्थापन वाली गलत प्रायिकताएं निरस्त।",
            fiftyFiftyProofEnglish = "With-replacement distractor fractions discarded.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("prob_without_replacement", "${red}_$blue"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Probability", "Dependent Sequential Draws", "hypergeometric"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "conditional_probability"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 22. KNIGHTS & KNAVES (Tiers 15 - 17)
    // =========================================================================
    private fun generateKnightsKnaves(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        // Three verified schemas; letter pairs vary the text. Solutions:
        // S1 "Both of us are Knaves" -> speaker Knave, other Knight.
        // S2 A: "B is a Knight"; B: "A and I are opposite types" -> both Knaves.
        // S3 "At least one of us is a Knave" -> speaker Knight, other Knave.
        val letterPairs = listOf("A" to "B", "X" to "Y", "P" to "Q", "M" to "N", "R" to "S", "K" to "L")
        val (l1, l2) = letterPairs.random(rand)
        val schema = rand.nextInt(3)

        val statementEn: String
        val statementHi: String
        val answerEn: String
        val answerHi: String
        val logicTag: String
        when (schema) {
            0 -> {
                statementEn = "$l1 states: 'Both of us are Knaves.'"
                statementHi = "$l1 कहता/कहती है: 'हम दोनों झूठे (Knaves) हैं।'"
                answerEn = "$l1 is a Knave, $l2 is a Knight"
                answerHi = "$l1 झूठा (Knave) है, $l2 सच्चा (Knight) है"
                logicTag = "both_knaves_claim"
            }
            1 -> {
                statementEn = "$l1 states: '$l2 is a Knight.' $l2 states: '$l1 and I are of opposite types.'"
                statementHi = "$l1 कहता/कहती है: '$l2 सच्चा (Knight) है।' $l2 कहता/कहती है: '$l1 और मैं विपरीत प्रकार के हैं।'"
                answerEn = "Both are Knaves"
                answerHi = "दोनों झूठे (Knaves) हैं"
                logicTag = "opposite_types_claim"
            }
            else -> {
                statementEn = "$l1 states: 'At least one of us is a Knave.'"
                statementHi = "$l1 कहता/कहती है: 'हम में से कम से कम एक झूठा (Knave) है।'"
                answerEn = "$l1 is a Knight, $l2 is a Knave"
                answerHi = "$l1 सच्चा (Knight) है, $l2 झूठा (Knave) है"
                logicTag = "at_least_one_knave"
            }
        }

        val qEn = "On the Island of Logic, Knights always tell the truth and Knaves always lie. $statementEn What are the true identities of $l1 and $l2?"
        val qHi = "तर्क द्वीप पर नाइट (Knight) सदैव सत्य बोलते हैं और नेव (Knave) सदैव झूठ बोलते हैं। $statementHi $l1 और $l2 की वास्तविक पहचान क्या है?"

        val optsEn = listOf(
            answerEn,
            "Both are Knights",
            "Both are Knaves",
            "$l1 is a Knave, $l2 is a Knight",
            "$l1 is a Knight, $l2 is a Knave"
        ).distinct().shuffled(rand).take(4).let { if (answerEn in it) it else (it.drop(1) + answerEn).shuffled(rand) }
        val optsHi = optsEn.map {
            when (it) {
                answerEn -> answerHi
                "Both are Knights" -> "दोनों सच्चे (Knights) हैं"
                "Both are Knaves" -> "दोनों झूठे (Knaves) हैं"
                "$l1 is a Knave, $l2 is a Knight" -> "$l1 झूठा (Knave) है, $l2 सच्चा (Knight) है"
                else -> "$l1 सच्चा (Knight) है, $l2 झूठा (Knave) है"
            }
        }
        val correctIdx = optsEn.indexOf(answerEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(answerEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Knights and Knaves Logic",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("यदि कथन सत्य है तो वक्ता Knight है, अन्यथा Knave", "प्रत्येक पहचान-युग्म के लिए कथन की सत्यता जांचें", "केवल एक युग्म संगत है: $answerHi"),
            cluesEnglish = listOf("If the statement is true, the speaker is a Knight; otherwise a Knave", "Test the statement's truth value for each identity pair", "Only one pair is consistent: $answerEn"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "प्रतिज्ञान-विरोध (self-reference) विश्लेषण से $answerHi।",
            deductionPathEnglish = "Self-reference consistency analysis gives: $answerEn.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "कथन के साथ तार्किक रूप से संगत।" else "कथन के साथ विरोधाभास उत्पन्न करता है।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Logically consistent with the statement." else "Creates a contradiction with the statement." },
            expertAdviceHindi = "वक्ता को Knight मानें, फिर Knave मानें - जो स्थिति विरोधाभास न दे वही उत्तर है।",
            expertAdviceEnglish = "Assume the speaker is a Knight, then a Knave; the assignment without contradiction is the answer.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "विरोधाभासी पहचान युग्म निरस्त।",
            fiftyFiftyProofEnglish = "Contradictory identity pairs eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("knights_knaves", "${logicTag}_${l1}${l2}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Formal Logic", "Truth-Teller Liar Paradox", "self_reference"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deductive_logic", "knights_knaves"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 23. MASTER SYLLOGISM (Tiers 16 - 17)
    // =========================================================================
    private fun generateMasterSyllogism(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        // Verified chain: Some W are X; all X are Y => those W are Y;
        // no Z is Y => those W are not Z. Conclusion: "Some W are not Z".
        val xs = listOf("scientists" to "वैज्ञानिक", "doctors" to "डॉक्टर", "engineers" to "इंजीनियर", "artists" to "कलाकार", "bankers" to "बैंकर")
        val ys = listOf("logical" to "तार्किक", "disciplined" to "अनुशासित", "methodical" to "व्यवस्थित", "punctual" to "समयनिष्ठ")
        val zs = listOf("superstitious person" to "अंधविश्वासी", "careless person" to "लापरवाह व्यक्ति", "dishonest person" to "बेईमान व्यक्ति", "lazy person" to "आलसी व्यक्ति")
        val ws = listOf("teachers" to "शिक्षक", "students" to "छात्र", "pilots" to "पायलट", "writers" to "लेखक", "farmers" to "किसान")
        val x = xs.random(rand); val y = ys.random(rand); val z = zs.random(rand); val w = ws.random(rand)

        val qEn = "Premises:
1. All ${x.first} are ${y.first}.
2. No ${z.first} is ${y.first}.
3. Some ${w.first} are ${x.first}.
What conclusion is deductively CERTAIN?"
        val qHi = "कथन:
1. सभी ${x.second} ${y.second} हैं।
2. कोई भी ${z.second} ${y.second} नहीं है।
3. कुछ ${w.second} ${x.second} हैं।
निश्चित रूप से वैध तार्किक निष्कर्ष क्या है?"

        val correctStrEn = "Some ${w.first} are not ${z.first}s"
        val correctStrHi = "कुछ ${w.second} ${z.second} नहीं हैं"
        val optsEn = listOf(
            correctStrEn,
            "All ${w.first} are ${x.first}",
            "Some ${z.first}s are ${x.first}",
            "No ${w.first.dropLast(1)} is ${y.first}"
        ).shuffled(rand)
        val optsHi = optsEn.map {
            when (it) {
                correctStrEn -> correctStrHi
                "All ${w.first} are ${x.first}" -> "सभी ${w.second} ${x.second} हैं"
                "Some ${z.first}s are ${x.first}" -> "कुछ ${z.second} ${x.second} हैं"
                else -> "कोई भी ${w.second} ${y.second} नहीं है"
            }
        }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Aristotelian Syllogism",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("कुछ ${w.second} ${x.second} हैं और सभी ${x.second} ${y.second} हैं, अतः वे ${w.second} ${y.second} हैं", "कोई ${z.second} ${y.second} नहीं है", "अतः वे ${w.second} ${z.second} नहीं हो सकते"),
            cluesEnglish = listOf("Some ${w.first} are ${x.first} and all ${x.first} are ${y.first}, so those ${w.first} are ${y.first}", "No ${z.first} is ${y.first}", "Therefore those ${w.first} cannot be ${z.first}s"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "कुछ ${w.second} -> ${x.second} -> ${y.second} -> ${z.second} नहीं।",
            deductionPathEnglish = "Chain: some ${w.first} -> ${x.first} -> ${y.first} -> not ${z.first}.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "आवश्यक परिणाम (necessary consequence)।" else "परिसर (conversion) दोष या अनुमान मात्र।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Necessary consequence of the premise chain." else "Illicit conversion or merely possible, not certain." },
            expertAdviceHindi = "वेन आरेख बनाएं: ${w.second} और ${x.second} का प्रतिच्छेदन ${y.second} के भीतर, ${z.second} के बाहर।",
            expertAdviceEnglish = "Draw a Venn diagram: the ${w.first}-${x.first} overlap lies inside ${y.first} and outside ${z.first}.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "अनुमान-मात्र विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Merely-possible options eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("syllogism_chain", "${x.first}_${y.first}_${z.first}_${w.first}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Formal Logic", "Categorical Syllogism", "barbara_celarent"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deductive_logic", "syllogism"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 24. GAME THEORY MINIMAX (Tiers 16 - 17, Adult Focus)
    // =========================================================================
    private fun generateGameTheoryMinimax(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        // Pure maximin: pick the strategy with the larger worst-case floor.
        // Payoffs and strategy names vary; the higher floor is always correct.
        val a = (3..9).random(rand)
        val b = (1 until a).random(rand)
        val names = listOf("X" to "Y", "Alpha" to "Beta", "P" to "Q", "Hawk" to "Dove")
        val (s1, s2) = names.random(rand)
        val firstIsHigh = rand.nextBoolean()
        val highName = if (firstIsHigh) s1 else s2
        val lowName = if (firstIsHigh) s2 else s1

        val qEn = "In a two-player zero-sum game, Player A has two strategies: $highName and $lowName. Strategy $highName guarantees a minimum worst-case payoff of +$a, while Strategy $lowName guarantees a minimum worst-case payoff of +$b. Under Von Neumann's Minimax theorem, which is the risk-dominant optimal strategy?"
        val qHi = "एक द्विपक्षीय शून्य-योग खेल (Zero-Sum Game) में, खिलाड़ी A के पास दो रणनीतियाँ $highName और $lowName हैं। रणनीति $highName में न्यूनतम प्रतिफल (worst-case payoff) +$a है और रणनीति $lowName में न्यूनतम प्रतिफल +$b है। वॉन न्यूमैन के मिनिमैक्स (Minimax) प्रमेय के अनुसार, जोखिम-रहित सुरक्षित इष्टतम रणनीति क्या होगी?"

        val correctStrEn = "Strategy $highName (Maximin value of +$a)"
        val correctStr = "रणनीति $highName (अधिकतम न्यूनतम प्रतिफल = +$a)"
        val optsEn = listOf(
            correctStrEn,
            "Strategy $lowName (worst-case floor of +$b)",
            "Mix both strategies 50-50 arbitrarily",
            "No optimal solution exists"
        ).shuffled(rand)
        val optsHi = optsEn.map {
            when (it) {
                correctStrEn -> correctStr
                "Strategy $lowName (worst-case floor of +$b)" -> "रणनीति $lowName (प्रतिफल +$b)"
                "Mix both strategies 50-50 arbitrarily" -> "दोनों रणनीतियों का 50-50 मनमाना मिश्रण"
                else -> "कोई इष्टतम रणनीति नहीं है"
            }
        }
        val correctIdx = optsEn.indexOf(correctStrEn).coerceAtLeast(0)

        val normEn = MultiLayerQuestionValidator.normalizeText(qEn)
        val normAns = MultiLayerQuestionValidator.normalizeText(correctStrEn)

        return QuestionItem(
            id = UUID.randomUUID().toString(),
            qNumber = qNumber,
            difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds,
            points = meta.points,
            isCheckpoint = meta.isCheckpoint,
            checkpointTitle = meta.checkpointTitle,
            category = "Game Theory Minimax",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("मैक्सिमिन: प्रत्येक रणनीति का न्यूनतम प्रतिफल लें", "रणनीति $highName की न्यूनतम सीमा +$a, रणनीति $lowName की +$b", "बड़ी न्यूनतम सीमा वाली रणनीति चुनें"),
            cluesEnglish = listOf("Maximin: take each strategy's minimum payoff", "Strategy $highName floors at +$a, Strategy $lowName at +$b", "Choose the strategy with the larger floor"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "max(+$a, +$b) = +$a, अतः रणनीति $highName।",
            deductionPathEnglish = "max(+$a, +$b) = +$a, so Strategy $highName is the maximin choice.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "मैक्सिमिन नियम द्वारा सिद्ध।" else "न्यूनतम सीमा कम है या नियम का उल्लंघन।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Follows from the maximin rule." else "Lower floor or violates the maximin rule." },
            expertAdviceHindi = "सबसे खराब स्थिति की तुलना करें, सर्वश्रेष्ठ की नहीं।",
            expertAdviceEnglish = "Compare worst cases, not best cases.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "उप-इष्टतम विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Sub-optimal choices eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("minimax_maximin", "${highName}${a}_${lowName}${b}"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Game Theory", "Minimax Decision", "zero_sum"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("strategic_reasoning", "minimax"),
            generationVersion = 2
        )
    }
}
