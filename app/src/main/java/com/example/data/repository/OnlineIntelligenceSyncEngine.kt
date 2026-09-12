package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.api.GeminiApiClient
import com.example.data.db.CurrentAffairEntity
import com.example.data.db.CurrentAffairsDao
import com.example.data.model.CurrentAffairItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Online Intelligence & Current Affairs Engine for TarkShastra.
 * Operates in the background silently at app startup:
 * 1. Checks internet availability without interrupting the user.
 * 2. Fetches and consolidates verified 24-hour national, international, and regional updates.
 * 3. Validates facts and deduplicates multiple reports into canonical events.
 * 4. Calibrates events for Junior (Age 5-17) and Adult player profiles.
 * 5. Caches them in the local Room knowledge store for both online and offline play.
 */
class OnlineIntelligenceSyncEngine(
    private val context: Context,
    private val currentAffairsDao: CurrentAffairsDao,
    private val geminiApiClient: GeminiApiClient = GeminiApiClient()
) {

    fun isInternetAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Silent Background Sync Trigger.
     * ZERO pop-ups, ZERO manual import buttons, ZERO blocking loaders.
     */
    suspend fun syncSilently(userProfile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            // Seed base canonical events if store is empty (guarantees offline resilience)
            val currentCount = currentAffairsDao.getCount()
            if (currentCount == 0) {
                val seedAffairs = CurrentAffairsReasoningGenerator.getAllCanonicalItems().map { itemToEntity(it) }
                currentAffairsDao.insertOrUpdateAffairs(seedAffairs)
            }

            // If offline, silently complete
            if (!isInternetAvailable()) {
                return@withContext
            }

            // Online discovery layer via Gemini / Web intelligence with 3-second circuit breaker
            val onlineUpdates = kotlinx.coroutines.withTimeoutOrNull(3000L) {
                geminiApiClient.fetchRecentCurrentAffairs(userProfile)
            } ?: emptyList()
            if (onlineUpdates.isNotEmpty()) {
                val entities = onlineUpdates.map { itemToEntity(it) }
                currentAffairsDao.insertOrUpdateAffairs(entities)
            }
        } catch (e: Exception) {
            // Fails gracefully and silently, ensuring core game remains 100% playable
            e.printStackTrace()
        }
    }

    private fun itemToEntity(item: CurrentAffairItem): CurrentAffairEntity {
        return CurrentAffairEntity(
            currentAffairId = item.currentAffairId,
            eventId = item.eventId,
            headline = item.headline,
            canonicalSummary = item.canonicalSummary,
            eventDate = item.eventDate,
            firstSeenDate = item.firstSeenDate,
            lastVerifiedDate = item.lastVerifiedDate,
            sourceReferences = item.sourceReferences,
            country = item.country,
            state = item.state,
            districtRegion = item.districtRegion,
            topic = item.topic,
            juniorEligibility = item.juniorEligibility,
            adultEligibility = item.adultEligibility,
            minAge = item.minAge,
            maxAge = item.maxAge,
            examRelevance = item.examRelevance,
            usedQuestionIdsJson = JSONArray(item.usedQuestionIds).toString(),
            isExpired = item.isExpired
        )
    }
}
