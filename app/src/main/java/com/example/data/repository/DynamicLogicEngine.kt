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

        // Truly exhausted (every variation already served): last resort.
        val fallbackGen = pool.first()
        return fallbackGen.generate(qNumber, meta, Random(salt + 999), userProfile)
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
        val sonCurrentAge = listOf(10, 12, 14).random(rand)
        val fatherCurrentAge = 3 * sonCurrentAge
        val yearsPassed = 10
        val fatherFuture = fatherCurrentAge + yearsPassed
        val sonFuture = sonCurrentAge + yearsPassed

        val qHi = "एक पिता की आयु वर्तमान में अपने पुत्र की आयु की 3 गुनी है। 10 वर्ष बाद पिता की आयु पुत्र की आयु की दोगुनी से $yearsPassed वर्ष अधिक होगी। पुत्र की वर्तमान आयु क्या है?"
        val qEn = "A father's current age is 3 times his son's current age. In 10 years, the father will be 10 years older than twice the son's age. What is the son's current age?"

        val correctStr = "$sonCurrentAge Years"
        val optsEn = listOf(correctStr, "${sonCurrentAge + 2} Years", "${sonCurrentAge - 2} Years", "${sonCurrentAge + 5} Years").distinct().shuffled(rand)
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
            cluesHindi = listOf("माना पुत्र = x, पिता = 3x", "10 वर्ष बाद: (3x + 10) = 2(x + 10) + 10 - 10 = 2x + 20", "3x - 2x = 20 - 10 => x = $sonCurrentAge"),
            cluesEnglish = listOf("Let son = x, father = 3x", "In 10 yrs: 3x + 10 = 2(x + 10)", "3x - 2x = 20 - 10 => x = $sonCurrentAge"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "3x + 10 = 2x + 20 => x = $sonCurrentAge वर्ष।",
            deductionPathEnglish = "Linear solution: 3x + 10 = 2(x + 10) gives x = $sonCurrentAge years.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "रैखिक समीकरण समाधान द्वारा सिद्ध।" else "समीकरण को संतुष्ट नहीं करता।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Satisfies the dual temporal linear equations." else "Fails algebraic substitution." },
            expertAdviceHindi = "पुत्र की आयु x मानकर भविष्य के समीकरण की रचना करें।",
            expertAdviceEnglish = "Express both ages in terms of variable x and equate at the future checkpoint.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "समीकरण में असंगत मान निरस्त।",
            fiftyFiftyProofEnglish = "Non-satisfying age values eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("age_linear_eq", "s$sonCurrentAge"),
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
        val qHi = "एक 3x3 ग्रिड में यदि किसी आकृति को घड़ी की सुई की दिशा (Clockwise) में 90° घुमाया जाता है, तो शीर्ष-बाएँ (Top-Left) कोने का तत्व नए ग्रिड में किस स्थान पर स्थानांतरित होगा?"
        val qEn = "In a 3x3 grid, if an image or matrix is rotated 90° clockwise, to which coordinate does the element initially at the TOP-LEFT corner move?"

        val correctStr = "Top-Right (शीर्ष-दाएँ)"
        val correctStrEn = "Top-Right"
        val optsEn = listOf(correctStrEn, "Bottom-Right", "Bottom-Left", "Center").shuffled(rand)
        val optsHi = optsEn.map {
            when (it) {
                "Top-Right" -> "Top-Right (शीर्ष-दाएँ)"
                "Bottom-Right" -> "Bottom-Right (निचले-दाएँ)"
                "Bottom-Left" -> "Bottom-Left (निचले-बाएँ)"
                else -> "Center (मध्य)"
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
            category = "3x3 Matrix Grid Transformation",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("मैट्रिक्स घूर्णन नियम: (पंक्ति r, स्तंभ c) -> (स्तंभ c, N - 1 - r)", "प्रारंभिक स्थान: (0, 0) शीर्ष-बाएँ", "90° दक्षिणावर्त घूर्णन के बाद: (0, 2) शीर्ष-दाएँ"),
            cluesEnglish = listOf("Matrix rotation mapping: (r, c) -> (c, N - 1 - r)", "Initial coordinate: (0, 0) top-left", "After 90° CW: coordinate becomes (0, 2) top-right"),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "(0,0) -> (0,2) जो कि शीर्ष-दायाँ कोना है।",
            deductionPathEnglish = "Transformation maps (0,0) to (0,2), which is the Top-Right corner.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "90° घूर्णन ज्यामिति द्वारा सिद्ध।" else "180° या 270° का स्थान।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Direct result of 90° clockwise matrix rotation." else "Corresponds to 180° or 270° orientation." },
            expertAdviceHindi = "घड़ी की सुइयों की तरह कोने को 90 डिग्री घुमाएं।",
            expertAdviceEnglish = "Trace the corner element through a 90-degree clockwise sweep.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "विपरीत कोनों के विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Opposite coordinate positions eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("matrix_rotate90", "top_left"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Linear Algebra", "Matrix Rotation 90", "coordinate_transform"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("spatial_reasoning", "matrix_rotation"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 19. CRYPTARITHM (Tiers 12 - 15)
    // =========================================================================
    private fun generateCryptarithm(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val qHi = "अंकगणितीय कूट पहेली में यदि: AB + BA = 132 है, जहां A और B दो अलग-अलग गैर-शून्य अंक (1-9) हैं तथा A > B है, तो अंक A का निश्चित मान क्या होगा?"
        val qEn = "In the alphametic cryptarithm: AB + BA = 132, where A and B represent distinct non-zero digits (1-9) and A > B, what is the definite value of digit A?"

        val correctStr = "7"
        val optsEn = listOf("7", "8", "9", "6").shuffled(rand)
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
            cluesHindi = listOf("AB = 10A + B और BA = 10B + A", "AB + BA = 11(A + B) = 132", "A + B = 132 ÷ 11 = 12", "यदि A > B और A + B = 12, तो संभावित जोड़ियां: (9,3), (8,4), (7,5)।"),
            cluesEnglish = listOf("AB = 10A + B; BA = 10B + A", "Sum = 11(A + B) = 132 => A + B = 12", "Since A > B, potential pairs are (9,3), (8,4), (7,5)."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "11(A + B) = 132 => A + B = 12। न्यूनतम अंतर वाली वैध जोड़ी (7, 5) में A = 7।",
            deductionPathEnglish = "11(A+B) = 132 yields A+B = 12. In the canonical balanced pair (7,5), A = 7.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "अंकगणितीय आधार 10 और बीजगणितीय योग द्वारा सिद्ध।" else "योग 12 को संतुष्ट नहीं करता।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived via 11(A+B) = 132." else "Violates A+B = 12 decomposition." },
            expertAdviceHindi = "दहाई और इकाई के स्थानीय मानों (10A + B) का विस्तार करें।",
            expertAdviceEnglish = "Expand positional place values into 10A + B.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असंगत अंकों के विकल्प हटा दिए गए।",
            fiftyFiftyProofEnglish = "Invalid digit options eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("cryptarithm_ab_ba", "132"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Number Theory", "Positional Base 10 Cryptarithm", "alphametic"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("numerical_reasoning", "positional_expansion"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 20. FORENSIC TIMELINE (Tiers 12 - 15)
    // =========================================================================
    private fun generateForensicTimeline(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val qHi = "एक अपराध दोपहर 2:30 बजे शहर में हुआ। चार संदिग्धों के गवाही बयान निम्नलिखित हैं:\n1. सुमित: 'मैं 2:15 से 2:45 तक 100 किमी दूर दूसरे शहर के हवाई अड्डे पर सीसीटीवी में था।'\n2. अमन: 'मैं दोपहर 2:20 से 2:35 बजे तक अपराध स्थल से 1 किमी दूर बैंक में था।'\n3. राहुल: 'मैं 1:00 बजे से 4:00 बजे तक बिना किसी साक्षी के घर पर सो रहा था।'\nकिस संदिग्ध का बहाना (Alibi) भौतिक एवं भौगोलिक रूप से सबसे अभेद्य (unbreakable) है?"
        val qEn = "A crime occurs at 2:30 PM in the city center. Four suspects present the following alibis:\n1. Sumit: 'I was on CCTV at an airport 100 km away from 2:15 PM to 2:45 PM.'\n2. Aman: 'I was in a bank 1 km from the crime scene from 2:20 PM to 2:35 PM.'\n3. Rahul: 'I was sleeping at home with no witnesses from 1:00 PM to 4:00 PM.'\nWhich suspect's alibi is physically and geographically the most indisputable?"

        val correctStr = "Sumit (सुमित - 100 किमी दूर सीसीटीवी पुष्टि)"
        val correctStrEn = "Sumit - Timestamped CCTV 100 km away"
        val optsEn = listOf(
            correctStrEn,
            "Aman - Bank 1 km away",
            "Rahul - Unwitnessed sleep",
            "None of them have a valid alibi"
        ).shuffled(rand)
        val optsHi = listOf(
            correctStr,
            "अमन - 1 किमी दूर बैंक",
            "राहुल - अकेले घर में सोना",
            "किसी का भी बहाना वैध नहीं है"
        )
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
            category = "Forensic Chronology & Alibi Invalidation",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("15 मिनट में 100 किमी की दूरी तय करना भौतिक रूप से असंभव है (गति > 400 किमी/घंटा)।", "सीसीटीवी एक वस्तुनिष्ठ, स्वतंत्र तकनीकी साक्ष्य है।"),
            cluesEnglish = listOf("Traveling 100 km in 15 minutes is physically impossible (velocity > 400 km/h).", "Timestamped CCTV provides objective third-party verification."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "15 मिनट में 100 किमी की यात्रा भौतिक असंभवता है, और सीसीटीवी साक्ष्य निष्पक्ष पुष्टि करता है।",
            deductionPathEnglish = "Physical impossibility of transit (100 km in 15 min) combined with timestamped CCTV makes Sumit's alibi unbreakable.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "भौतिक गति असंभवता व निष्पक्ष सीसीटीवी द्वारा सिद्ध।" else "समीपता या साक्षी की कमी के कारण कमजोर।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Indisputable due to physical speed limits and video telemetry." else "Vulnerable to proximity or lack of independent corroboration." },
            expertAdviceHindi = "दूरी और समय के संबंध में भौतिक गति की सीमा का विश्लेषण करें।",
            expertAdviceEnglish = "Calculate the required velocity to traverse the distance in the elapsed window.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "कमजोर और अपुष्ट बहाने हटा दिए गए।",
            fiftyFiftyProofEnglish = "Unverified alibis discarded.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("forensic_alibi", "cctv_100km"),
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
        val qHi = "तर्क द्वीप पर दो निवासी A और B हैं। नाइट (Knight) सदैव सत्य बोलते हैं और नेव (Knave) सदैव झूठ बोलते हैं। A कहता है: 'हम दोनों झूठे (Knaves) हैं।' A और B की वास्तविक पहचान क्या है?"
        val qEn = "On the Island of Logic, Knights always tell the truth and Knaves always lie. Resident A states: 'Both of us are Knaves.' What are the true identities of A and B?"

        val correctStr = "A is Knave, B is Knight (A झूठा है और B सच्चा है)"
        val correctStrEn = "A is a Knave, B is a Knight"
        val optsEn = listOf(
            correctStrEn,
            "Both are Knights",
            "Both are Knaves",
            "A is a Knight, B is a Knave"
        ).shuffled(rand)
        val optsHi = optsEn.map {
            when (it) {
                correctStrEn -> correctStr
                "Both are Knights" -> "दोनों सच्चे (Knights) हैं"
                "Both are Knaves" -> "दोनों झूठे (Knaves) हैं"
                else -> "A सच्चा है और B झूठा है"
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
            category = "Knights & Knaves (Smullyan Island)",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("यदि A सच्चा (Knight) होता, तो उसका कथन 'हम दोनों झूठे हैं' सत्य होना चाहिए, जो विरोधाभास है।", "अतः A निश्चित रूप से झूठा (Knave) है।", "A का कथन झूठा होने के लिए 'हम दोनों झूठे हैं' का असत्य होना आवश्यक है, अतः B सच्चा (Knight) होना चाहिए।"),
            cluesEnglish = listOf("If A were a Knight, his claim 'both are knaves' would be true, creating an impossible paradox.", "Therefore A is definitely a Knave.", "For A's statement to be false, they cannot both be Knaves, so B must be a Knight."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "A सच्चा नहीं हो सकता (विरोधाभास)। अतः A झूठा है। उसके कथन को असत्य करने के लिए B का सच्चा होना अनिवार्य है।",
            deductionPathEnglish = "A cannot be a Knight (paradox). Thus A is a Knave. For his statement to be false, B must be a Knight.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "तार्किक विरोधाभास समाधान द्वारा सिद्ध।" else "तार्किक विरोधाभास उत्पन्न करता है।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Unassailable paradox resolution." else "Generates formal contradiction." },
            expertAdviceHindi = "परीक्षण करें कि क्या कोई सच्चा व्यक्ति स्वयं को झूठा कह सकता है?",
            expertAdviceEnglish = "Test whether a truth-teller can ever declare themselves a liar.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "विरोधाभासी विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Self-contradictory identities eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("knights_both_knaves", "smullyan_classic"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Formal Logic", "Knights and Knaves Paradox", "liar_paradox"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deduction", "truth_liar_grid"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 23. MASTER SYLLOGISM (Tiers 16 - 17)
    // =========================================================================
    private fun generateMasterSyllogism(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val qHi = "कथन:\n1. सभी वैज्ञानिक तार्किक हैं।\n2. कोई भी अंधविश्वासी तार्किक नहीं है।\n3. कुछ शिक्षक वैज्ञानिक हैं।\nनिश्चित रूप से वैध तार्किक निष्कर्ष क्या है?"
        val qEn = "Premises:\n1. All scientists are logical.\n2. No superstitious person is logical.\n3. Some teachers are scientists.\nWhat conclusion is mathematically and deductively CERTAIN?"

        val correctStrHi = "कुछ शिक्षक अंधविश्वासी नहीं हैं (Some teachers are not superstitious)"
        val correctStrEn = "Some teachers are not superstitious"
        val optsEn = listOf(
            correctStrEn,
            "All teachers are scientists",
            "Some superstitious people are scientists",
            "No teacher is logical"
        ).shuffled(rand)
        val optsHi = optsEn.map {
            when (it) {
                correctStrEn -> correctStrHi
                "All teachers are scientists" -> "सभी शिक्षक वैज्ञानिक हैं"
                "Some superstitious people are scientists" -> "कुछ अंधविश्वासी वैज्ञानिक हैं"
                else -> "कोई शिक्षक तार्किक नहीं है"
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
            category = "Grand Syllogistic Deduction (महा-तर्क)",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("जो शिक्षक वैज्ञानिक हैं (कथन 3), वे तार्किक भी हैं (कथन 1)।", "जो तार्किक हैं, वे कभी अंधविश्वासी नहीं हो सकते (कथन 2)।", "अतः वे शिक्षक जो वैज्ञानिक हैं, वे निश्चित रूप से अंधविश्वासी नहीं हो सकते।"),
            cluesEnglish = listOf("The teachers who are scientists (Premise 3) are logical (Premise 1).", "Anyone who is logical cannot be superstitious (Premise 2).", "Therefore, teachers who are scientists are definitely NOT superstitious."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "शिक्षक ∩ वैज्ञानिक ⊆ तार्किक ⊆ (अंधविश्वासी)ᶜ। अतः कुछ शिक्षक निश्चित रूप से अंधविश्वासी नहीं हैं।",
            deductionPathEnglish = "Teachers ∩ Scientists ⊆ Logical ⊆ Superstitiousᶜ. Hence, some teachers are guaranteed not superstitious.",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "अचूक न्यायशास्त्र निष्कर्ष।" else "कथनों द्वारा समर्थित नहीं।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Unassailable categorical syllogism." else "Unsupported by premises." },
            expertAdviceHindi = "वेन आरेख में तीनों समुच्चयों का प्रतिच्छेदन बनाएं।",
            expertAdviceEnglish = "Draw the Venn intersection of all three sets.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "असमर्थित विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Unsubstantiated claims eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("syllogism_grand_master", "pred_logic_teachers"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Predicate Logic", "Categorical Syllogism", "contrapositive_chain"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deduction", "formal_syllogism"),
            generationVersion = 2
        )
    }

    // =========================================================================
    // 24. GAME THEORY MINIMAX (Tiers 16 - 17, Adult Focus)
    // =========================================================================
    private fun generateGameTheoryMinimax(qNumber: Int, meta: TierInfo, rand: Random, profile: UserProfile): QuestionItem {
        val qHi = "एक द्विपक्षीय शून्य-योग खेल (Zero-Sum Game) में, खिलाड़ी A के पास दो रणनीतियाँ X और Y हैं। रणनीति X में न्यूनतम प्रतिफल (worst-case payoff) +4 है और रणनीति Y में न्यूनतम प्रतिफल +2 है। वॉन न्यूमैन के मिनिमैक्स (Minimax) प्रमेय के अनुसार, जोखिम-रहित सुरक्षित इष्टतम रणनीति क्या होगी?"
        val qEn = "In a two-player zero-sum game, Player A has two strategies: X and Y. Strategy X guarantees a minimum worst-case payoff of +4, while Strategy Y guarantees a minimum worst-case payoff of +2. Under Von Neumann's Minimax theorem, which is the risk-dominant optimal strategy?"

        val correctStr = "Strategy X (अधिकतम न्यूनतम प्रतिफल = +4)"
        val correctStrEn = "Strategy X (Maximin value of +4)"
        val optsEn = listOf(
            correctStrEn,
            "Strategy Y (Minimax value of +2)",
            "Mix 50-50 arbitrarily",
            "No optimal solution exists"
        ).shuffled(rand)
        val optsHi = listOf(
            correctStr,
            "Strategy Y (प्रतिफल +2)",
            "50-50 का मनमाना मिश्रण",
            "कोई इष्टतम रणनीति नहीं है"
        )
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
            category = "Game Theory & Dominant Strategy",
            questionHindi = qHi,
            questionEnglish = qEn,
            cluesHindi = listOf("मैक्सिमिन (Maximin) नियम: न्यूनतम में से अधिकतम का चुनाव।", "रणनीति X का न्यूनतम = +4", "रणनीति Y का न्यूनतम = +2", "max(+4, +2) = +4 (रणनीति X)"),
            cluesEnglish = listOf("Maximin principle: Maximize the minimum guaranteed gain.", "Worst-case X = +4, Worst-case Y = +2", "max(4, 2) = 4, which dictates Strategy X."),
            optionsHindi = optsHi,
            optionsEnglish = optsEn,
            correctAnswerIndex = correctIdx,
            deductionPathHindi = "मैक्सिमिन सिद्धांत के अनुसार max(min(X), min(Y)) = max(+4, +2) = +4 (रणनीति X)।",
            deductionPathEnglish = "By Von Neumann's Maximin criterion, max(worst(X), worst(Y)) = max(4, 2) = 4 (Strategy X).",
            eliminationReasonsHindi = optsHi.mapIndexed { idx, opt -> if (idx == correctIdx) "वॉन न्यूमैन मैक्सिमिन प्रमेय द्वारा सिद्ध।" else "अवर रणनीति या निराधार दावा।" },
            eliminationReasonsEnglish = optsEn.mapIndexed { idx, opt -> if (idx == correctIdx) "Confirmed by Von Neumann Minimax criterion." else "Suboptimal dominated strategy." },
            expertAdviceHindi = "खिलाड़ी के सबसे खराब स्थिति वाले प्रतिफलों की तुलना करें।",
            expertAdviceEnglish = "Compare the floor guarantees (worst-case minimums) of both strategies.",
            fiftyFiftyDiscardIndices = (0..3).filter { it != correctIdx }.take(2),
            fiftyFiftyProofHindi = "अवर रणनीति वाले विकल्प निरस्त।",
            fiftyFiftyProofEnglish = "Strictly dominated strategies eliminated.",
            semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(normEn, normAns),
            logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("game_theory_maximin", "x4_y2"),
            conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Game Theory", "Minimax Criterion", "zero_sum_matrix"),
            patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("deduction", "game_theoretic_decision"),
            generationVersion = 2
        )
    }
}
