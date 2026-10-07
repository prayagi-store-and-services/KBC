package com.example.data.api

import com.example.data.model.isJuniorPlayer
import com.example.data.model.effectiveClassNumber
import com.example.BuildConfig
import com.example.data.model.CurrentAffairItem
import com.example.data.model.KnowledgeProfileVector
import com.example.data.model.QuestionItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Direct REST client for Gemini API question generation, verification, and expert reasoning hints.
 */
class GeminiApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateDynamicQuestion(
        qNumber: Int,
        userProfile: UserProfile,
        difficultyMultiplier: Float,
        category: String
    ): QuestionItem? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        try {
            val prompt = buildQuestionGenerationPrompt(qNumber, userProfile, difficultyMultiplier, category)
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val responseBody = response.body?.string() ?: return@withContext null
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext null
            val content = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            val text = parts.optJSONObject(0)?.optString("text") ?: return@withContext null

            parseQuestionFromAiResponse(text, qNumber, category)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Silent discovery of rolling 24-hour verified news/events for TarkShastra.
     * Consolidates national, science, governance, economy, sports, and user's state developments.
     */
    suspend fun fetchRecentCurrentAffairs(userProfile: UserProfile): List<CurrentAffairItem> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext emptyList()

        try {
            val userState = userProfile.state.ifBlank { "National" }
            val userCity = userProfile.city.ifBlank { "Local" }
            
            val prompt = """
                Provide a JSON array of 8 verified facts/events for India.
                MANDATORY QUOTAS:
                - At least 2 events MUST be specifically about the city of $userCity (Priority: Recent current affairs. If none, use major historical facts, famous landmarks, or unique culture).
                - At least 3 events MUST be specifically about the state of $userState (Priority: Recent current affairs. If none, use history, administration, geography).
                - 3 events about National/Science/Economy.
                
                For each event, format strictly as a JSON object:
                {
                  "eventId": "UNIQUE_STRING_ID",
                  "headline": "Crisp headline of the verified event or historical fact",
                  "canonicalSummary": "2-sentence factual summary with verified figures/context",
                  "eventDate": "Use 'Recent' for current affairs, 'Historical' for history, or 'Static' for geography/culture",
                  "sourceReferences": "PIB, ISRO, News, or History Book",
                  "country": "India",
                  "state": "$userState",
                  "districtRegion": "$userCity (ONLY if the fact is specifically about the city, otherwise empty)",
                  "topic": "Current Affairs, History, Culture, Geography, Science & Tech, Environment, etc.",
                  "juniorEligibility": true,
                  "adultEligibility": true,
                  "minAge": 6,
                  "maxAge": 99,
                  "examRelevance": "General"
                }
                
                Return strictly the JSON array of objects.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val responseBody = response.body?.string() ?: return@withContext emptyList()
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return@withContext emptyList()
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext emptyList()
            val content = firstCandidate.optJSONObject("content") ?: return@withContext emptyList()
            val parts = content.optJSONArray("parts") ?: return@withContext emptyList()
            val text = parts.optJSONObject(0)?.optString("text") ?: return@withContext emptyList()

            val jsonArray = JSONArray(text)
            val resultList = mutableListOf<CurrentAffairItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                resultList.add(
                    CurrentAffairItem(
                        currentAffairId = obj.optString("eventId", UUID.randomUUID().toString()),
                        eventId = obj.optString("eventId", UUID.randomUUID().toString()),
                        headline = obj.optString("headline"),
                        canonicalSummary = obj.optString("canonicalSummary"),
                        eventDate = obj.optString("eventDate", "Recent"),
                        sourceReferences = obj.optString("sourceReferences", "Verified Source"),
                        country = obj.optString("country", "India"),
                        state = obj.optString("state", userState),
                        districtRegion = obj.optString("districtRegion", ""),
                        topic = obj.optString("topic", "National"),
                        juniorEligibility = obj.optBoolean("juniorEligibility", true),
                        adultEligibility = obj.optBoolean("adultEligibility", true),
                        minAge = obj.optInt("minAge", 6),
                        maxAge = obj.optInt("maxAge", 99),
                        examRelevance = obj.optString("examRelevance", "All")
                    )
                )
            }
            resultList
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Converts a verified current affair into a TarkShastra deductive reasoning question.
     */
    suspend fun generateCurrentAffairsReasoningQuestion(
        affair: CurrentAffairItem,
        qNumber: Int,
        userProfile: UserProfile
    ): QuestionItem? = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null

        try {
            val isStudent = userProfile.isJuniorPlayer()
            val tierMeta = getTierDetails(qNumber)
            val prompt = """
                You are the Chief Current Affairs Reasoning Engine for 'TarkShastra'.
                Transform this verified current event into a pure DEDUCTIVE REASONING CHALLENGE:
                
                Event: ${affair.headline}
                Context: ${affair.canonicalSummary}
                Topic: ${affair.topic}
                State/Region: ${affair.state}
                Target Tier: Q$qNumber (${tierMeta.difficultyTitle})
                Audience: ${if (isStudent) "Junior Student (Age: ${userProfile.age}, Class: ${userProfile.studentClass})" else "Adult Aspirant (Domain: ${userProfile.preparationDomain}, State: ${userProfile.state})"}
                
                MANDATORY RULES:
                1. REASONING OVER GUESSING: Do NOT ask simple recall like "When was X launched?". Instead, embed the verified parameters/rules as clues, and ask for a logical deduction, mathematical consequence, spatial grid coordinate, or process elimination.
                2. BILINGUAL: Full question, clues, options, deduction path, and 50-50 proofs in Hindi & English.
                3. UNAMBIGUOUS: Exactly one mathematically/logically correct option among the 4.
                
                Return JSON with this exact schema:
                {
                  "questionHindi": "string",
                  "questionEnglish": "string",
                  "cluesHindi": ["clue1", "clue2", "clue3"],
                  "cluesEnglish": ["clue1", "clue2", "clue3"],
                  "optionsHindi": ["Option A", "Option B", "Option C", "Option D"],
                  "optionsEnglish": ["Option A", "Option B", "Option C", "Option D"],
                  "correctAnswerIndex": 0,
                  "deductionPathHindi": "string",
                  "deductionPathEnglish": "string",
                  "eliminationReasonsHindi": ["reason0", "reason1", "reason2", "reason3"],
                  "eliminationReasonsEnglish": ["reason0", "reason1", "reason2", "reason3"],
                  "expertAdviceHindi": "string",
                  "expertAdviceEnglish": "string",
                  "fiftyFiftyDiscardIndices": [1, 2],
                  "fiftyFiftyProofHindi": "string",
                  "fiftyFiftyProofEnglish": "string"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.5)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val responseBody = response.body?.string() ?: return@withContext null
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext null
            val content = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            val text = parts.optJSONObject(0)?.optString("text") ?: return@withContext null

            parseQuestionFromAiResponse(text, qNumber, "Current Affairs • ${affair.topic}")
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getLiveExpertClue(
        questionText: String,
        clues: List<String>,
        options: List<String>,
        languageMode: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        try {
            val langInstruction = when (languageMode.uppercase()) {
                "HINDI" -> "Respond in Hindi"
                "ENGLISH" -> "Respond in English"
                else -> "Respond in both Hindi and English (Bilingual)"
            }
            val prompt = """
                You are 'Tark Guru' (The Reasoning Master Expert).
                A user is facing a pure logic deduction question.
                RULE: You are STRICTLY FORBIDDEN from giving the direct answer or revealing which option (A/B/C/D) is correct.
                Instead, give a powerful, structured reasoning coordinate/framework on how the user can logically isolate and deduce the answer from the given clues.
                $langInstruction. Keep your guidance within 2-3 short, crisp sentences.
                
                Question: $questionText
                Clues: ${clues.joinToString(" | ")}
                Options: ${options.joinToString(" | ")}
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val responseBody = response.body?.string() ?: return@withContext null
            val root = JSONObject(responseBody)
            val text = root.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            text?.trim()
        } catch (_: Exception) {
            null
        }
    }

    private fun buildQuestionGenerationPrompt(
        qNumber: Int,
        userProfile: UserProfile,
        difficultyMultiplier: Float,
        category: String
    ): String {
        val tierMeta = getTierDetails(qNumber)
        val isStudent = userProfile.isJuniorPlayer()
        val studentGuidance = if (isStudent) {
            """
            AUDIENCE & CURRICULUM ADAPTATION (KBC JUNIOR / SCHOOL STUDENT MODE):
            - Candidate is a school student currently in ${userProfile.studentClass} (Age: ${userProfile.age} years).
            - MAKE QUESTIONS SLIGHTLY EASIER, ENGAGING, AND INTUITIVE FOR THEIR AGE & GRADE LEVEL.
            - Use relatable school-level concepts: Solar system & planet facts with logic, animal kingdom classification, clock hands & angles, river boat directions, shape folding, simple fraction/balance puzzles, daily calendar dates, and fun word/cipher riddles.
            - Ensure questions spark curiosity while strictly maintaining 100% deductive clarity.
            """.trimIndent()
        } else {
            "AUDIENCE: Competitive aspirant / Adult logician (Domain: ${userProfile.preparationDomain})."
        }

        return """
            You are the Chief Intelligence & Reasoning Engine of 'TarkShastra' - an adaptive KBC-style AI Logic Quiz where EVERY answer MUST be logically derivable from embedded clues, and pure guessing is impossible.
            
            Target Category: $category
            $studentGuidance
            User Profile:
            - Age: ${userProfile.age}, State: ${userProfile.state}, Education: ${userProfile.educationLevel}
            - Domain Preparation: ${userProfile.preparationDomain}, Interests: ${userProfile.interests.joinToString(", ")}
            - Dynamic Difficulty Multiplier: $difficultyMultiplier
            
            MANDATORY SPECIFICATIONS:
            1. PURE LOGIC/REASONING: The question MUST contain sufficient clues, premises, or rules such that the correct answer can be 100% derived mathematically, logically, forensically, or through step-by-step elimination.
            2. UNAMBIGUOUS: There must be EXACTLY ONE mathematically/logically defendable answer. The other 3 options must be demonstrably false based on the premises.
            3. BILINGUAL: Provide full fields in both Hindi and English.
            4. FIFTY-FIFTY: Specify exactly 2 demonstrably incorrect option indices (0, 1, 2, or 3) and provide logical proof why they fail.
            5. DEDUCTION PATH: Detail the step-by-step proof.
            6. DIAGRAM / VISUAL METADATA (Optional): If diagramType is applicable ("shadow_sun", "coordinate_path", "matrix_grid", "clock_angle", "venn_logic"), specify diagramType and diagramData.
            
            Return JSON with strictly this structure:
            {
              "questionHindi": "string",
              "questionEnglish": "string",
              "cluesHindi": ["clue1", "clue2", "clue3"],
              "cluesEnglish": ["clue1", "clue2", "clue3"],
              "optionsHindi": ["Option A", "Option B", "Option C", "Option D"],
              "optionsEnglish": ["Option A", "Option B", "Option C", "Option D"],
              "correctAnswerIndex": 0, // 0, 1, 2, or 3
              "deductionPathHindi": "step-by-step logical proof",
              "deductionPathEnglish": "step-by-step logical proof",
              "eliminationReasonsHindi": ["why opt A fails", "why opt B fails", "why opt C fails", "why opt D fails"],
              "eliminationReasonsEnglish": ["why opt A fails", "why opt B fails", "why opt C fails", "why opt D fails"],
              "expertAdviceHindi": "strategic mental model without giving away the direct answer",
              "expertAdviceEnglish": "strategic mental model without giving away the direct answer",
              "fiftyFiftyDiscardIndices": [1, 3], // two wrong indices
              "fiftyFiftyProofHindi": "proof why these two options fail",
              "fiftyFiftyProofEnglish": "proof why these two options fail",
              "diagramType": "none", // or "shadow_sun", "coordinate_path", "matrix_grid", "clock_angle"
              "diagramData": ""
            }
        """.trimIndent()
    }

    private fun parseQuestionFromAiResponse(jsonText: String, qNumber: Int, category: String): QuestionItem? {
        return try {
            val cleanJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val obj = JSONObject("{$cleanJson}")

            val qHindi = obj.getString("questionHindi")
            val qEnglish = obj.getString("questionEnglish")
            val correctIdx = obj.getInt("correctAnswerIndex").coerceIn(0, 3)

            val cluesHindi = jsonArrayToStringList(obj.optJSONArray("cluesHindi"))
            val cluesEnglish = jsonArrayToStringList(obj.optJSONArray("cluesEnglish"))
            val optionsHindi = jsonArrayToStringList(obj.optJSONArray("optionsHindi"))
            val optionsEnglish = jsonArrayToStringList(obj.optJSONArray("optionsEnglish"))

            if (optionsHindi.size != 4 || optionsEnglish.size != 4) return null

            val deductionHindi = obj.optString("deductionPathHindi", "तार्किक विश्लेषण द्वारा सिद्ध")
            val deductionEnglish = obj.optString("deductionPathEnglish", "Proven through deductive analysis")
            val elimHindi = jsonArrayToStringList(obj.optJSONArray("eliminationReasonsHindi"))
            val elimEnglish = jsonArrayToStringList(obj.optJSONArray("eliminationReasonsEnglish"))
            val expertHindi = obj.optString("expertAdviceHindi", "सुरागों के क्रम और दिशा-निर्देशों पर ध्यान दें।")
            val expertEnglish = obj.optString("expertAdviceEnglish", "Focus on the relative coordinate rules and elimination constraints.")

            val discardsArr = obj.optJSONArray("fiftyFiftyDiscardIndices")
            val discards = mutableListOf<Int>()
            if (discardsArr != null) {
                for (i in 0 until discardsArr.length()) {
                    val idx = discardsArr.getInt(i)
                    if (idx != correctIdx && !discards.contains(idx) && idx in 0..3) {
                        discards.add(idx)
                    }
                }
            }
            while (discards.size < 2) {
                for (cand in 0..3) {
                    if (cand != correctIdx && !discards.contains(cand)) {
                        discards.add(cand)
                        if (discards.size == 2) break
                    }
                }
            }

            val ffProofHindi = obj.optString("fiftyFiftyProofHindi", "दोनों विकल्प प्रत्यक्ष रूप से दिए गए नियमों के विपरीत हैं।")
            val ffProofEnglish = obj.optString("fiftyFiftyProofEnglish", "Both options directly contradict the established boundary rules.")
            val diagramType = obj.optString("diagramType", "none")
            val diagramData = obj.optString("diagramData", "")

            val tierMeta = getTierDetails(qNumber)
            val fingerprint = computeSha256(qEnglish + optionsEnglish.joinToString(","))

            QuestionItem(
                id = UUID.randomUUID().toString(),
                qNumber = qNumber,
                difficultyTitle = tierMeta.difficultyTitle,
                timeLimitSeconds = tierMeta.timeLimitSeconds,
                points = tierMeta.points,
                isCheckpoint = tierMeta.isCheckpoint,
                checkpointTitle = tierMeta.checkpointTitle,
                category = category,
                questionHindi = qHindi,
                questionEnglish = qEnglish,
                cluesHindi = cluesHindi,
                cluesEnglish = cluesEnglish,
                optionsHindi = optionsHindi,
                optionsEnglish = optionsEnglish,
                correctAnswerIndex = correctIdx,
                deductionPathHindi = deductionHindi,
                deductionPathEnglish = deductionEnglish,
                eliminationReasonsHindi = elimHindi,
                eliminationReasonsEnglish = elimEnglish,
                expertAdviceHindi = expertHindi,
                expertAdviceEnglish = expertEnglish,
                fiftyFiftyDiscardIndices = discards,
                fiftyFiftyProofHindi = ffProofHindi,
                fiftyFiftyProofEnglish = ffProofEnglish,
                diagramType = diagramType,
                diagramData = diagramData,
                semanticFingerprint = fingerprint
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i))
        }
        return list
    }

    private fun computeSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        data class TierMeta(
            val difficultyTitle: String,
            val timeLimitSeconds: Int?,
            val points: Long,
            val isCheckpoint: Boolean,
            val checkpointTitle: String? = null
        )

        fun getTierDetails(qNumber: Int): TierMeta {
            return when (qNumber) {
                1 -> TierMeta("Very Easy", 60, 5L, false)
                2 -> TierMeta("Very Easy", 60, 10L, false)
                3 -> TierMeta("Easy", 60, 20L, false)
                4 -> TierMeta("Easy", 60, 30L, false)
                5 -> TierMeta("Easy+", 60, 50L, true, "पहला पड़ाव (1st Checkpoint)")
                6 -> TierMeta("Medium", 120, 75L, false)
                7 -> TierMeta("Medium", 120, 100L, false)
                8 -> TierMeta("Medium+", 120, 150L, false)
                9 -> TierMeta("Medium+", 120, 200L, false)
                10 -> TierMeta("Hard", 120, 300L, true, "दूसरा पड़ाव (2nd Checkpoint)")
                11 -> TierMeta("Hard", null, 400L, false)
                12 -> TierMeta("Hard+", null, 500L, false)
                13 -> TierMeta("Very Hard", null, 600L, false)
                14 -> TierMeta("Very Hard", null, 700L, false)
                15 -> TierMeta("Expert", null, 800L, false)
                16 -> TierMeta("Expert+", null, 900L, true, "तीसरा पड़ाव (3rd Checkpoint)")
                17 -> TierMeta("Extreme Grandmaster", null, 1000L, true, "अंतिम महा-तर्क (Final Crown)")
                else -> TierMeta("Standard", 60, 5L, false)
            }
        }
    }
}
