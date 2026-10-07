package com.example.data.repository

import com.example.data.model.isJuniorPlayer
import com.example.data.model.effectiveClassNumber
import android.content.Context
import com.example.data.model.QuestionItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Fresh-question packs written by the Netra question generator (public data-only branch "question-pool").
 * The app only downloads public JSON. It never talks to Gemini and holds no key.
 * Which questions were already used is remembered on this device only (question IDs, nothing personal).
 */
class PackRepository(private val context: Context) {

    companion object {
        const val BASE = "https://raw.githubusercontent.com/prayagi-store-and-services/KBC/question-pool/pool/"
        private const val PREFS = "kbc_pack_state"
        private const val REFRESH_MS = 6L * 60L * 60L * 1000L

        /** Pure helpers (unit tested). */
        fun groupFor(profile: UserProfile): String {
            val domain = profile.preparationDomain
            if (profile.isJuniorPlayer()) {
                val n = profile.effectiveClassNumber()
                return when {
                    n <= 5 -> "class-5"
                    n <= 8 -> "class-8"
                    n <= 10 -> "class-10"
                    else -> "class-12"
                }
            }
            return when {
                domain.contains("UPSC", true) -> "upsc"
                domain.contains("SSC", true) -> "ssc"
                domain.contains("Banking", true) || domain.contains("IBPS", true) -> "banking"
                else -> "general"
            }
        }

        fun bandFor(tier: Int): String = when {
            tier <= 5 -> "easy"
            tier <= 11 -> "medium"
            else -> "hard"
        }

        /** Picks one unused question per tier 1..17 from the parsed pool, or null if any tier cannot be filled. */
        fun pickLadder(
            pool: List<PackQuestion>,
            used: Set<String>,
            specialTag: String?,
            random: java.util.Random = java.util.Random()
        ): Map<Int, PackQuestion>? {
            val free = pool.filter { it.id !in used }.toMutableList()
            val chosen = LinkedHashMap<Int, PackQuestion>()
            for (tier in 1..17) {
                val band = bandFor(tier)
                val inBand = free.filter { it.band == band }
                val special = if (specialTag != null) inBand.filter { it.special == specialTag } else emptyList()
                val pick = (special.ifEmpty { inBand }).let { if (it.isEmpty()) null else it[random.nextInt(it.size)] }
                    ?: return null
                chosen[tier] = pick
                free.remove(pick)
            }
            return chosen
        }

        fun specialTagToday(cal: Calendar = Calendar.getInstance()): String? {
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            return when {
                m == 10 && d == 2 -> "Gandhi Jayanti (Mahatma Gandhi)"
                m == 8 && d == 15 -> "Independence Day of India"
                m == 1 && d == 26 -> "Republic Day of India"
                m == 11 && d == 14 -> "Children's Day (Jawaharlal Nehru)"
                m == 9 && d == 5 -> "Teachers' Day (Dr. S. Radhakrishnan)"
                m == 10 && d == 31 -> "National Unity Day (Sardar Vallabhbhai Patel)"
                m == 4 && d == 14 -> "Ambedkar Jayanti (Dr. B. R. Ambedkar)"
                m == 6 && d == 5 -> "World Environment Day"
                else -> null
            }
        }

        fun parsePool(json: String): List<PackQuestion> {
            val arr = JSONArray(json)
            val out = ArrayList<PackQuestion>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val oe = o.optJSONArray("optsEn") ?: continue
                val oh = o.optJSONArray("optsHi") ?: continue
                if (oe.length() != 4 || oh.length() != 4) continue
                val correct = o.optInt("correct", -1)
                if (correct !in 0..3) continue
                out.add(
                    PackQuestion(
                        id = o.optString("id"), band = o.optString("band"),
                        special = o.optString("special").takeIf { it.isNotBlank() && it != "null" },
                        qEn = o.optString("qEn"), qHi = o.optString("qHi"),
                        optsEn = List(4) { oe.getString(it) }, optsHi = List(4) { oh.getString(it) },
                        correct = correct, explainEn = o.optString("explainEn"), sourceHint = o.optString("sourceHint")
                    )
                )
            }
            return out.filter { it.id.isNotBlank() && it.qEn.isNotBlank() }
        }
    }

    data class PackQuestion(
        val id: String, val band: String, val special: String?,
        val qEn: String, val qHi: String, val optsEn: List<String>, val optsHi: List<String>,
        val correct: Int, val explainEn: String, val sourceHint: String
    )

    private val http = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).build()
    private val prefs get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun cacheFile(group: String) = File(context.filesDir, "packs/$group.json")

    fun usedIds(): Set<String> = prefs.getStringSet("used", emptySet()) ?: emptySet()

    /** Remember (on this device) that these questions were played. */
    fun markUsed(ids: Collection<String>) {
        if (ids.isEmpty()) return
        prefs.edit().putStringSet("used", usedIds() + ids).apply()
    }

    fun rememberLadder(sessionId: String, ids: List<String>) {
        prefs.edit().putString("ladder_$sessionId", ids.joinToString(",")).apply()
    }

    fun ladderIds(sessionId: String): List<String> =
        (prefs.getString("ladder_$sessionId", "") ?: "").split(",").filter { it.isNotBlank() }

    /** Download the group pool (at most every 6 hours). Falls back silently to the cached copy offline. */
    suspend fun refresh(group: String) = withContext(Dispatchers.IO) {
        val f = cacheFile(group)
        if (f.exists() && System.currentTimeMillis() - f.lastModified() < REFRESH_MS) return@withContext
        try {
            http.newCall(Request.Builder().url("$BASE$group.json").build()).execute().use { r ->
                if (r.isSuccessful) {
                    val body = r.body?.string().orEmpty()
                    parsePool(body) // validate before saving
                    f.parentFile?.mkdirs()
                    f.writeText(body)
                }
            }
        } catch (_: Exception) {
            // offline or server unavailable: keep using the cached copy
        }
    }

    fun loadPool(group: String): List<PackQuestion> =
        try { cacheFile(group).takeIf { it.exists() }?.readText()?.let { parsePool(it) } ?: emptyList() } catch (_: Exception) { emptyList() }

    /** Builds a fresh 17-question ladder, or null when the pool cannot fill every tier. */
    suspend fun buildLadder(sessionId: String, profile: UserProfile): Map<Int, QuestionItem>? {
        val group = groupFor(profile)
        refresh(group)
        var pool = loadPool(group)
        // Adult "general" questions are never mixed into a junior (class-*) ladder.
        if (group != "general" && !group.startsWith("class-")) { refresh("general"); pool = pool + loadPool("general") }
        if (group.startsWith("class-") && pool.isEmpty()) return null
        val picked = pickLadder(pool, usedIds(), specialTagToday()) ?: return null
        rememberLadder(sessionId, picked.values.map { it.id })
        return picked.mapValues { (tier, q) -> toQuestionItem(tier, q, sessionId) }
    }

    private fun toQuestionItem(tier: Int, q: PackQuestion, sessionId: String): QuestionItem {
        val meta = DynamicLogicEngine.getTierMeta(tier)
        val wrong = (0..3).filter { it != q.correct }
        return QuestionItem(
            id = q.id, qNumber = tier, difficultyTitle = meta.difficultyTitle,
            timeLimitSeconds = meta.timeLimitSeconds, points = meta.points,
            isCheckpoint = meta.isCheckpoint, checkpointTitle = meta.checkpointTitle,
            category = "General Knowledge",
            questionHindi = q.qHi, questionEnglish = q.qEn,
            cluesHindi = emptyList(), cluesEnglish = emptyList(),
            optionsHindi = q.optsHi, optionsEnglish = q.optsEn, correctAnswerIndex = q.correct,
            deductionPathHindi = "", deductionPathEnglish = "",
            eliminationReasonsHindi = emptyList(), eliminationReasonsEnglish = emptyList(),
            expertAdviceHindi = "", expertAdviceEnglish = "",
            fiftyFiftyDiscardIndices = wrong.take(2),
            fiftyFiftyProofHindi = "", fiftyFiftyProofEnglish = "",
            semanticFingerprint = q.id, logicFingerprint = "pack:" + q.id, sessionId = sessionId, generationVersion = 2
        )
    }
}
