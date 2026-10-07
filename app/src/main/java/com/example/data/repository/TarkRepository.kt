package com.example.data.repository

import com.example.data.model.isJuniorPlayer
import com.example.data.model.effectiveClassNumber
import android.content.Context
import com.example.data.api.GeminiApiClient
import com.example.data.db.CurrentAffairEntity
import com.example.data.db.CurrentAffairsDao
import com.example.data.db.GameHistoryDao
import com.example.data.db.GameHistoryEntity
import com.example.data.db.QuestionDao
import com.example.data.db.QuestionRegistryEntity
import com.example.data.db.SessionBankCacheDao
import com.example.data.db.UserProfileDao
import com.example.data.db.UserProfileEntity
import com.example.data.model.CurrentAffairItem
import com.example.data.model.GameSessionResult
import com.example.data.model.KnowledgeProfileVector
import com.example.data.model.QuestionItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class TarkRepository(
    private val context: Context,
    private val questionDao: QuestionDao,
    private val userProfileDao: UserProfileDao,
    private val gameHistoryDao: GameHistoryDao,
    private val currentAffairsDao: CurrentAffairsDao,
    private val sessionBankCacheDao: SessionBankCacheDao,
    private val geminiApiClient: GeminiApiClient = GeminiApiClient()
) {

    init {
        // Memory & Cache Hygiene: Prune session bank caches older than 24 hours on startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                sessionBankCacheDao.pruneOldSessionBanks(cutoff)
            } catch (_: Exception) {}
        }
    }

    suspend fun preloadNextGameSession(userProfile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            val prefetchSessionId = "prefetch_${System.currentTimeMillis()}"
            pipeline.prepareSessionQuestionBank(prefetchSessionId, userProfile) {}
        } catch (_: Exception) {}
    }

    private val onlineSyncEngine = OnlineIntelligenceSyncEngine(
        context = context,
        currentAffairsDao = currentAffairsDao,
        geminiApiClient = geminiApiClient
    )

    val pipeline = QuestionIntelligencePipeline(
        context = context,
        questionDao = questionDao,
        currentAffairsDao = currentAffairsDao,
        sessionBankCacheDao = sessionBankCacheDao,
        geminiApiClient = geminiApiClient
    )

    val userProfileFlow: Flow<UserProfile> = userProfileDao.getUserProfileFlow().map { entity ->
        if (entity != null) {
            entityToUserProfile(entity)
        } else {
            val defaultProfile = UserProfile()
            val vector = computeKnowledgeVector(defaultProfile)
            val defaultEntity = UserProfileEntity(
                userId = "primary_user",
                name = defaultProfile.name,
                age = defaultProfile.age,
                state = defaultProfile.state,
                city = defaultProfile.city,
                languageMode = defaultProfile.languageMode,
                hostGender = defaultProfile.hostGender,
                upiId = defaultProfile.upiId,
                educationLevel = defaultProfile.educationLevel,
                occupation = defaultProfile.occupation,
                preparationDomain = defaultProfile.preparationDomain,
                studentClass = defaultProfile.studentClass,
                isStudentMode = defaultProfile.isStudentMode,
                interestsJson = JSONArray(defaultProfile.interests).toString(),
                gkScore = vector.generalKnowledge,
                logicScore = vector.logicalReasoning,
                historyScore = vector.historyChronology,
                scienceScore = vector.scienceTech,
                financeScore = vector.financeEconomics,
                spatialScore = vector.spatialVisual
            )
            userProfileDao.saveUserProfile(defaultEntity)
            defaultProfile
        }
    }

    val gameHistoryFlow: Flow<List<GameHistoryEntity>> = gameHistoryDao.getAllHistory()
    val totalRegisteredQuestionsCount: Flow<Int> = questionDao.getRegisteredQuestionsCount()

    /**
     * Triggers silent background sync of 24h current affairs when online.
     * No popups, no blocking indicators.
     */
    suspend fun syncCurrentAffairsSilently(userProfile: UserProfile) = withContext(Dispatchers.IO) {
        onlineSyncEngine.syncSilently(userProfile)
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val vector = computeKnowledgeVector(profile)
        val isStudent = profile.isJuniorPlayer()
        val entity = UserProfileEntity(
            userId = profile.id.ifEmpty { "primary_user" },
            name = profile.name,
            age = profile.age,
            state = profile.state,
            city = profile.city,
            languageMode = profile.languageMode.uppercase().let { if (it in listOf("HINDI", "ENGLISH", "BILINGUAL")) it else "ENGLISH" },
            hostGender = profile.hostGender,
            upiId = profile.upiId,
            educationLevel = profile.educationLevel,
            occupation = if (isStudent) "Student" else profile.occupation,
            preparationDomain = profile.preparationDomain,
            studentClass = profile.studentClass,
            isStudentMode = isStudent,
            interestsJson = JSONArray(profile.interests).toString(),
            gkScore = vector.generalKnowledge,
            logicScore = vector.logicalReasoning,
            historyScore = vector.historyChronology,
            scienceScore = vector.scienceTech,
            financeScore = vector.financeEconomics,
            spatialScore = vector.spatialVisual
        )
        userProfileDao.saveUserProfile(entity)
    }

    suspend fun getUserProfile(): UserProfile = withContext(Dispatchers.IO) {
        val entity = userProfileDao.getUserProfile()
        if (entity != null) {
            entityToUserProfile(entity)
        } else {
            val defaultProfile = UserProfile()
            saveUserProfile(defaultProfile)
            defaultProfile
        }
    }

    /**
     * Preloads and prepares all 17 questions for a new game session using the Question Intelligence Pipeline.
     * Caches in local Room storage for 100% offline gameplay.
     */
    suspend fun prepareGameSessionBank(
        sessionId: String,
        userProfile: UserProfile,
        onProgress: (PreparationProgress) -> Unit = {}
    ): Map<Int, QuestionItem> = withContext(Dispatchers.IO) {
        pipeline.prepareSessionQuestionBank(sessionId, userProfile, onProgress)
    }

    suspend fun getCachedSessionLadder(sessionId: String): Map<Int, QuestionItem>? = withContext(Dispatchers.IO) {
        pipeline.getCachedSessionLadder(sessionId)
    }

    suspend fun invalidateSessionBank(sessionId: String) = withContext(Dispatchers.IO) {
        pipeline.invalidateSessionBank(sessionId)
    }

    suspend fun invalidateAllSessionBanks() = withContext(Dispatchers.IO) {
        pipeline.invalidateAllSessionBanks()
    }

    /**
     * Preloads all 17 questions for a new game session at once in background memory.
     * Guarantees that advancing to the next question is an instantaneous O(1) swap (<= 0.5s).
     */
    suspend fun preloadGameLadder(
        userProfile: UserProfile,
        currentAffairsSlots: Set<Int>,
        sessionId: String = "session_${System.currentTimeMillis()}"
    ): Map<Int, QuestionItem> = withContext(Dispatchers.IO) {
        pipeline.prepareSessionQuestionBank(sessionId, userProfile) {}
    }

    /**
     * Retrieves a unique, non-repeated, pure-deduction question for tier `qNumber`.
     * Operates 100% offline-ready with ZERO network latency on the gameplay critical path.
     */
    suspend fun getQuestionForTier(
        qNumber: Int,
        userProfile: UserProfile,
        difficultyMultiplier: Float,
        flippedQuestionIds: Set<String>,
        isCurrentAffairsSlot: Boolean = false,
        customFingerprints: Set<String>? = null,
        customLogicFingerprints: Set<String>? = null
    ): QuestionItem = withContext(Dispatchers.IO) {
        val servedNormalizedTexts = questionDao.getAllServedNormalizedTexts().toSet()
        val servedSemanticFingerprints = (questionDao.getAllServedSemanticFingerprints() + (customFingerprints ?: questionDao.getAllServedFingerprints())).toSet()
        val servedLogicFingerprints = (customLogicFingerprints ?: questionDao.getAllServedLogicFingerprints()).toSet()
        val servedConceptFingerprints = questionDao.getAllServedConceptFingerprints().toSet()

        val history = MultiLayerQuestionValidator.HistoricalRegistry(
            servedNormalizedTexts = servedNormalizedTexts,
            servedSemanticFingerprints = servedSemanticFingerprints,
            servedLogicFingerprints = servedLogicFingerprints,
            servedConceptFingerprints = servedConceptFingerprints
        )

        if (isCurrentAffairsSlot) {
            return@withContext getCurrentAffairsQuestionForTier(
                qNumber = qNumber,
                userProfile = userProfile,
                servedFingerprints = servedSemanticFingerprints,
                flippedQuestionIds = flippedQuestionIds
            )
        }

        // Instant Procedural Dynamic Logic Engine (< 2ms)
        val selectedQuestion = DynamicLogicEngine.generateUniqueQuestion(
            qNumber = qNumber,
            userProfile = userProfile,
            history = history,
            currentSessionQuestions = emptyList()
        )

        // Register in Room Database (Global permanent uniqueness)
        registerQuestionInDb(selectedQuestion, userProfile.id, userProfile.languageMode)

        selectedQuestion
    }

    private suspend fun getCurrentAffairsQuestionForTier(
        qNumber: Int,
        userProfile: UserProfile,
        servedFingerprints: Set<String>,
        flippedQuestionIds: Set<String>
    ): QuestionItem {
        // Instant verified canonical Current Affairs reasoning generator (< 2ms)
        val finalQuestion = CurrentAffairsReasoningGenerator.generateReasoningQuestion(
            qNumber = qNumber,
            userProfile = userProfile,
            excludedFingerprints = servedFingerprints
        )

        registerQuestionInDb(finalQuestion, userProfile.id, userProfile.languageMode)
        return finalQuestion
    }

    private fun entityToCurrentAffairItem(entity: CurrentAffairEntity): CurrentAffairItem {
        return CurrentAffairItem(
            currentAffairId = entity.currentAffairId,
            eventId = entity.eventId,
            headline = entity.headline,
            canonicalSummary = entity.canonicalSummary,
            eventDate = entity.eventDate,
            firstSeenDate = entity.firstSeenDate,
            lastVerifiedDate = entity.lastVerifiedDate,
            sourceReferences = entity.sourceReferences,
            country = entity.country,
            state = entity.state,
            districtRegion = entity.districtRegion,
            topic = entity.topic,
            juniorEligibility = entity.juniorEligibility,
            adultEligibility = entity.adultEligibility,
            minAge = entity.minAge,
            maxAge = entity.maxAge,
            examRelevance = entity.examRelevance,
            isExpired = entity.isExpired
        )
    }

    suspend fun markQuestionFlipped(questionId: String) = withContext(Dispatchers.IO) {
        // Handled via profile session question tracking
    }

    suspend fun getLiveExpertGuidance(
        questionText: String,
        clues: List<String>,
        options: List<String>,
        languageMode: String,
        fallbackAdvice: String
    ): String = withContext(Dispatchers.IO) {
        val aiClue = geminiApiClient.getLiveExpertClue(questionText, clues, options, languageMode)
        aiClue ?: fallbackAdvice
    }

    suspend fun saveGameSession(result: GameSessionResult) = withContext(Dispatchers.IO) {
        // Questions actually reached in this game are now used on this device and never offered again.
        try {
            val ids = pipeline.packRepository.ladderIds(result.sessionId)
            pipeline.packRepository.markUsed(ids.take(result.highestQuestionReached.coerceIn(0, ids.size)))
        } catch (_: Exception) { }
        val entity = GameHistoryEntity(
            sessionId = result.sessionId,
            finalPrize = result.totalPointsWon,
            grossPrize = result.totalPointsWon,
            totalDeduction = 0L,
            incorrectQuestionsJson = "[]",
            highestQuestionReached = result.highestQuestionReached,
            outcomeStatus = result.reasonEnded,
            correctAnswersCount = result.correctCount,
            lifelinesUsed = "Used: ${result.lifelinesUsedCount}",
            durationSeconds = (result.questionsAnsweredCount * result.averageResponseTimeSec).toInt(),
            timestamp = result.timestamp
        )
        gameHistoryDao.insertGameHistory(entity)
        try {
            val best = maxOf(gameHistoryDao.getHighestPrizeWon() ?: 0L, result.totalPointsWon)
            com.example.widget.KbcWidgetStore.save(context, result.totalPointsWon, result.highestQuestionReached, result.correctCount, result.reasonEnded, result.timestamp, best)
            com.example.widget.KbcWidgetProvider.refresh(context)
        } catch (_: Exception) { }

        // Update profile high scores
        val currentProfile = getUserProfile()
        val prevEntity = userProfileDao.getUserProfile()
        val currentBest = prevEntity?.highestPrizeWon ?: 0L
        val currentHighestTier = prevEntity?.highestTierReached ?: 0

        val newBest = maxOf(currentBest, result.totalPointsWon)
        val newHighestTier = maxOf(currentHighestTier, result.highestQuestionReached)

        val isStudent = currentProfile.isJuniorPlayer()
        val updatedEntity = UserProfileEntity(
            userId = currentProfile.id.ifEmpty { "primary_user" },
            name = currentProfile.name,
            age = currentProfile.age,
            state = currentProfile.state,
            city = currentProfile.city,
            languageMode = currentProfile.languageMode.uppercase().let { if (it in listOf("HINDI", "ENGLISH", "BILINGUAL")) it else "ENGLISH" },
            hostGender = currentProfile.hostGender,
            upiId = currentProfile.upiId,
            educationLevel = currentProfile.educationLevel,
            occupation = currentProfile.occupation,
            preparationDomain = currentProfile.preparationDomain,
            studentClass = currentProfile.studentClass,
            isStudentMode = isStudent,
            interestsJson = JSONArray(currentProfile.interests).toString(),
            gkScore = (currentProfile.profileVector.generalKnowledge + 0.02f * result.correctCount).coerceIn(0.1f, 1.0f),
            logicScore = (currentProfile.profileVector.logicalReasoning + 0.03f * result.correctCount).coerceIn(0.1f, 1.0f),
            historyScore = currentProfile.profileVector.historyChronology,
            scienceScore = currentProfile.profileVector.scienceTech,
            financeScore = currentProfile.profileVector.financeEconomics,
            spatialScore = currentProfile.profileVector.spatialVisual,
            totalGamesPlayed = (prevEntity?.totalGamesPlayed ?: 0) + 1,
            highestPrizeWon = newBest,
            highestTierReached = newHighestTier,
            lastUpdated = System.currentTimeMillis()
        )
        userProfileDao.saveUserProfile(updatedEntity)
    }

    private suspend fun registerQuestionInDb(question: QuestionItem, profileId: String, languageMode: String) {
        val validLang = languageMode.uppercase()
        if (validLang !in listOf("HINDI", "ENGLISH", "BILINGUAL")) {
            throw IllegalArgumentException("Invalid language mode: $languageMode.")
        }
        val qFp = question.semanticFingerprint.trim().lowercase()
        val lFp = question.logicFingerprint.trim().lowercase()
        val cFp = question.conceptFingerprint.trim().lowercase()
        val pFp = question.patternFingerprint.trim().lowercase()
        
        val entity = QuestionRegistryEntity(
            id = "reg_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}",
            questionId = question.id,
            questionFingerprint = qFp,
            logicFingerprint = lFp,
            conceptFingerprint = cFp,
            patternFingerprint = pFp,
            generationVersion = question.generationVersion,
            canonicalQuestion = question.questionEnglish.ifBlank { question.questionHindi },
            languageMode = validLang,
            difficultyTier = question.qNumber,
            servedByProfileId = profileId,
            usedAt = System.currentTimeMillis(),
            semanticFingerprint = qFp,
            normalizedQuestionText = MultiLayerQuestionValidator.normalizeText(question.questionEnglish.ifBlank { question.questionHindi })
        )
        try {
            questionDao.registerQuestion(entity)
        } catch (e: Exception) {
            android.util.Log.e("TarkShastra", "Global question already registered/consumed: ${e.message}")
        }
    }

    private fun selectCategoryForTierAndProfile(qNumber: Int, profile: UserProfile): String {
        val categories = listOf(
            "Spatial Coordinate Vector",
            "Visual Shadow Optics",
            "Syllogistic Deduction",
            "Acoustic Rhythm Meter",
            "Matrix Pattern Logic",
            "Economic Balance Logic",
            "Forensic Chronology",
            "Conservation Science",
            "Deductive Cryptic Cipher",
            "Bayesian Deduction Logic",
            "Acoustic Physics Logic",
            "Graph Theory Topology",
            "Recursive Combinatorics",
            "Knights & Knaves Logic",
            "Relativistic Physics Deduction",
            "Information Theory Logic",
            "Meta-Logic & Diagonalization"
        )
        val idx = (qNumber - 1).coerceIn(0, categories.size - 1)
        return categories[idx]
    }

    fun computeKnowledgeVector(profile: UserProfile): KnowledgeProfileVector {
        var gk = 0.5f
        var logic = 0.6f
        var history = 0.5f
        var science = 0.5f
        var finance = 0.4f
        var spatial = 0.6f

        when (profile.preparationDomain) {
            "UPSC / Civil Services" -> { gk += 0.3f; history += 0.3f; logic += 0.2f }
            "SSC / State Exams" -> { gk += 0.25f; logic += 0.25f; science += 0.15f }
            "Banking / IBPS" -> { finance += 0.4f; logic += 0.3f }
            "Engineering / Tech" -> { science += 0.35f; logic += 0.35f; spatial += 0.2f }
            "Management / CAT" -> { logic += 0.35f; finance += 0.3f }
            else -> { gk += 0.1f; logic += 0.15f }
        }

        profile.interests.forEach { interest ->
            when {
                interest.contains("Logic", true) || interest.contains("Puzzle", true) -> logic += 0.1f
                interest.contains("Science", true) || interest.contains("Tech", true) -> science += 0.1f
                interest.contains("History", true) -> history += 0.1f
                interest.contains("Finance", true) || interest.contains("Economy", true) -> finance += 0.1f
                interest.contains("Visual", true) || interest.contains("Spatial", true) -> spatial += 0.1f
            }
        }

        return KnowledgeProfileVector(
            generalKnowledge = gk.coerceIn(0.1f, 1.0f),
            logicalReasoning = logic.coerceIn(0.1f, 1.0f),
            historyChronology = history.coerceIn(0.1f, 1.0f),
            scienceTech = science.coerceIn(0.1f, 1.0f),
            financeEconomics = finance.coerceIn(0.1f, 1.0f),
            spatialVisual = spatial.coerceIn(0.1f, 1.0f),
            domainStrength = (logic * 0.5f + gk * 0.5f).coerceIn(0.1f, 1.0f),
            regionalContext = profile.state,
            regionalCity = profile.city
        )
    }

    private fun entityToUserProfile(entity: UserProfileEntity): UserProfile {
        val interestsList = try {
            val jsonArr = JSONArray(entity.interestsJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArr.length()) {
                list.add(jsonArr.getString(i))
            }
            list
        } catch (_: Exception) {
            listOf("Logical Reasoning", "General Science", "Puzzles")
        }

        return UserProfile(
            id = entity.userId,
            name = entity.name,
            age = entity.age,
            state = entity.state,
            city = entity.city,
            languageMode = entity.languageMode.uppercase().let { if (it in listOf("HINDI", "ENGLISH", "BILINGUAL")) it else "ENGLISH" },
            hostGender = entity.hostGender,
            upiId = entity.upiId ?: "",
            educationLevel = entity.educationLevel,
            occupation = entity.occupation,
            preparationDomain = entity.preparationDomain,
            studentClass = entity.studentClass,
            isStudentMode = entity.isStudentMode,
            interests = interestsList,
            profileVector = KnowledgeProfileVector(
                generalKnowledge = entity.gkScore,
                logicalReasoning = entity.logicScore,
                historyChronology = entity.historyScore,
                scienceTech = entity.scienceScore,
                financeEconomics = entity.financeScore,
                spatialVisual = entity.spatialScore,
                domainStrength = (entity.logicScore * 0.6f + entity.gkScore * 0.4f),
                regionalContext = entity.state,
                regionalCity = entity.city
            )
        )
    }
}
