package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM question_registry WHERE questionFingerprint = :fingerprint LIMIT 1")
    suspend fun getQuestionByFingerprint(fingerprint: String): QuestionRegistryEntity?

    @Query("SELECT questionFingerprint FROM question_registry")
    suspend fun getAllServedFingerprints(): List<String>

    @Query("SELECT logicFingerprint FROM question_registry")
    suspend fun getAllServedLogicFingerprints(): List<String>

    @Query("SELECT semanticFingerprint FROM question_registry WHERE semanticFingerprint != ''")
    suspend fun getAllServedSemanticFingerprints(): List<String>

    @Query("SELECT conceptFingerprint FROM question_registry WHERE conceptFingerprint != ''")
    suspend fun getAllServedConceptFingerprints(): List<String>

    @Query("SELECT conceptFingerprint FROM question_registry WHERE conceptFingerprint != '' ORDER BY usedAt DESC LIMIT 17")
    suspend fun getRecentConceptFingerprints(): List<String>

    @Query("SELECT normalizedQuestionText FROM question_registry WHERE normalizedQuestionText != ''")
    suspend fun getAllServedNormalizedTexts(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registerQuestion(entity: QuestionRegistryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registerQuestions(entities: List<QuestionRegistryEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM question_registry")
    fun getRegisteredQuestionsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_registry WHERE questionFingerprint IN (:fingerprints)")
    suspend fun countExistingFingerprints(fingerprints: List<String>): Int
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile_table WHERE userId = :userId LIMIT 1")
    fun getUserProfileFlow(userId: String = "primary_user"): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile_table WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String = "primary_user"): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(entity: UserProfileEntity)
}

@Dao
interface GameHistoryDao {
    @Query("SELECT * FROM game_history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<GameHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistory(entity: GameHistoryEntity)

    @Query("SELECT MAX(finalPrize) FROM game_history_table")
    suspend fun getHighestPrizeWon(): Long?

    @Query("SELECT MAX(highestQuestionReached) FROM game_history_table")
    suspend fun getHighestQuestionReached(): Int?
}

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions_table WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: String): GameSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(entity: GameSessionEntity)

    @Query("UPDATE game_sessions_table SET status = :status, endedAtMillis = :endedAt, finalQuestionReached = :qReached, finalPrize = :prize WHERE sessionId = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: String, endedAt: Long, qReached: Int, prize: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(entity: GameSessionEventEntity)

    @Query("SELECT * FROM game_session_events_table WHERE sessionId = :sessionId ORDER BY timestampMillis ASC")
    suspend fun getEventsForSession(sessionId: String): List<GameSessionEventEntity>
}

@Dao
interface AppMetadataDao {
    @Query("SELECT * FROM app_metadata_table ORDER BY versionCode DESC LIMIT 1")
    suspend fun getLatestAppMetadata(): AppMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppMetadata(entity: AppMetadataEntity)
}

@Dao
interface CurrentAffairsDao {
    @Query("SELECT * FROM current_affairs_store WHERE isExpired = 0 ORDER BY lastVerifiedDate DESC")
    fun getAllActiveCurrentAffairsFlow(): Flow<List<CurrentAffairEntity>>

    @Query("SELECT * FROM current_affairs_store WHERE isExpired = 0 ORDER BY lastVerifiedDate DESC")
    suspend fun getAllActiveCurrentAffairs(): List<CurrentAffairEntity>

    @Query("SELECT * FROM current_affairs_store WHERE juniorEligibility = 1 AND isExpired = 0 AND minAge <= :age AND maxAge >= :age ORDER BY lastVerifiedDate DESC")
    suspend fun getJuniorEligibleAffairs(age: Int): List<CurrentAffairEntity>

    @Query("SELECT * FROM current_affairs_store WHERE adultEligibility = 1 AND isExpired = 0 ORDER BY lastVerifiedDate DESC")
    suspend fun getAdultEligibleAffairs(): List<CurrentAffairEntity>

    @Query("SELECT * FROM current_affairs_store WHERE (state = :state OR state = 'National' OR state = 'India') AND isExpired = 0 ORDER BY lastVerifiedDate DESC")
    suspend fun getAffairsByStateOrNational(state: String): List<CurrentAffairEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAffairs(affairs: List<CurrentAffairEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAffair(affair: CurrentAffairEntity)

    @Query("SELECT COUNT(*) FROM current_affairs_store")
    suspend fun getCount(): Int
}

@Dao
interface SessionBankCacheDao {
    @Query("SELECT * FROM session_question_bank_cache WHERE sessionId = :sessionId AND isInvalidated = 0 LIMIT 1")
    suspend fun getCachedSessionBank(sessionId: String): SessionQuestionBankCacheEntity?

    @Query("SELECT * FROM session_question_bank_cache WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionBankRaw(sessionId: String): SessionQuestionBankCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSessionBank(entity: SessionQuestionBankCacheEntity)

    @Query("UPDATE session_question_bank_cache SET status = :status WHERE sessionId = :sessionId")
    suspend fun updateSessionBankStatus(sessionId: String, status: String)

    @Query("UPDATE session_question_bank_cache SET status = 'INVALIDATED', isInvalidated = 1 WHERE sessionId = :sessionId")
    suspend fun invalidateSessionBank(sessionId: String)

    @Query("UPDATE session_question_bank_cache SET status = 'INVALIDATED', isInvalidated = 1 WHERE sessionId != :currentSessionId")
    suspend fun invalidateAllOlderSessionBanks(currentSessionId: String)

    @Query("UPDATE session_question_bank_cache SET status = 'INVALIDATED', isInvalidated = 1")
    suspend fun invalidateAllSessionBanks()

    @Query("DELETE FROM session_question_bank_cache WHERE sessionId = :sessionId")
    suspend fun deleteSessionBank(sessionId: String)

    @Query("DELETE FROM session_question_bank_cache WHERE createdAt < :cutoffTimeMillis OR isInvalidated = 1 OR status IN ('COMPLETED', 'FAILED', 'INVALIDATED')")
    suspend fun pruneOldSessionBanks(cutoffTimeMillis: Long): Int
}
