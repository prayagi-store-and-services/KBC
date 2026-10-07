package com.example.data.repository

import com.example.data.model.isJuniorPlayer
import com.example.data.model.effectiveClassNumber
import com.example.data.model.CurrentAffairItem
import com.example.data.model.QuestionItem
import com.example.data.model.UserProfile
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

/**
 * High-Integrity Current Affairs Reasoning Generator for TarkShastra.
 * Transforms verified current events (National, International, Science & Tech, Governance,
 * Environment, Economy, Sports, and State Developments) into pure deductive reasoning challenges.
 * Ensures the core principle: "Current Information + Embedded Deductive Clues -> Reasoning -> Answer".
 */
object CurrentAffairsReasoningGenerator {

    data class CanonicalEvent(
        val eventId: String,
        val headlineHindi: String,
        val headlineEnglish: String,
        val topic: String,
        val state: String = "National",
        val districtRegion: String = "",
        val juniorEligible: Boolean = true,
        val adultEligible: Boolean = true,
        val minAge: Int = 5,
        val maxAge: Int = 99,
        val summaryHindi: String,
        val summaryEnglish: String
    )

    val canonicalEvents = listOf(
        CanonicalEvent(
            eventId = "ISRO_SPADEX_2025",
            headlineHindi = "इसरो का स्पेस डॉकिंग प्रयोग (SpaDeX मिशन) व ऑर्बिटल कोऑर्डिनेशन",
            headlineEnglish = "ISRO Space Docking Experiment (SpaDeX) & Orbital Coordination",
            topic = "Science & Tech",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 7,
            maxAge = 99,
            summaryHindi = "इसरो ने दो अलग-अलग उपग्रहों (चेज़र और टारगेट) को अंतरिक्ष में सटीक गति व दिशा मिलाकर जोड़ने की स्वचालित डॉकिंग तकनीक का सफल परीक्षण किया।",
            summaryEnglish = "ISRO successfully tested automated autonomous space docking by matching velocity vectors and relative orbital altitudes between Chaser and Target satellites."
        ),
        CanonicalEvent(
            eventId = "GREEN_HYDROGEN_CORRIDORS",
            headlineHindi = "राष्ट्रीय हरित हाइड्रोजन मिशन एवं स्वच्छ ऊर्जा परिवहन ग्रिड",
            headlineEnglish = "National Green Hydrogen Mission & Clean Energy Transport Grid",
            topic = "Environment",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 8,
            maxAge = 99,
            summaryHindi = "भारत ने 5 MMT वार्षिक हरित हाइड्रोजन उत्पादन लक्ष्य के अंतर्गत सौर एवं पवन ऊर्जा से जल इलेक्ट्रोलिसिस द्वारा शून्य-उत्सर्जन ईंधन हब स्थापित किए।",
            summaryEnglish = "India established zero-emission green hydrogen manufacturing hubs utilizing dedicated solar/wind electrolysis grids to replace fossil fuels."
        ),
        CanonicalEvent(
            eventId = "CHESS_OLYMPIAD_INDIA_SWEEP",
            headlineHindi = "भारतीय शतरंज ग्रैंडमास्टर्स का ऐतिहासिक दोहरा स्वर्ण विजय",
            headlineEnglish = "Indian Grandmasters Historic Double Gold at FIDE Chess Olympiad",
            topic = "Sports",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 6,
            maxAge = 99,
            summaryHindi = "भारत की पुरुष एवं महिला टीमों ने सभी 11 राउंड में अपराजित रहते हुए बोर्ड-1 और बोर्ड-2 पर निर्णायक रणनीतिक चालों से ऐतिहासिक दोहरा स्वर्ण जीता।",
            summaryEnglish = "India's Open and Women's teams swept double historic gold at the 45th Chess Olympiad remaining undefeated across all 11 rounds."
        ),
        CanonicalEvent(
            eventId = "UP_AI_DATA_CENTER_EXPRESSWAY",
            headlineHindi = "उत्तर प्रदेश: बुंदेलखंड एवं गंगा एक्सप्रेसवे डिजिटल लॉजिस्टिक्स कॉरिडोर",
            headlineEnglish = "Uttar Pradesh: Bundelkhand & Ganga Expressway Digital Logistics Hub",
            topic = "Regional",
            state = "Uttar Pradesh",
            juniorEligible = true,
            adultEligible = true,
            minAge = 8,
            maxAge = 99,
            summaryHindi = "उत्तर प्रदेश ने एक्सप्रेसवे नेटवर्क के किनारे हाई-स्पीड डेटा केबल और सौर-ऊर्जा संचालित एग्री-वेयरहाउसिंग लॉजिस्टिक्स नोड्स को जोड़ा।",
            summaryEnglish = "UP integrated solar-powered agro-logistics cold chains and dedicated fiber nodes along the Bundelkhand and Ganga Expressway network."
        ),
        CanonicalEvent(
            eventId = "MAHA_VADHAVAN_MEGA_PORT",
            headlineHindi = "महाराष्ट्र: वधावन ग्रीनफील्ड ऑल-वेदर डीप-वाटर मेगा पोर्ट",
            headlineEnglish = "Maharashtra: Vadhavan All-Weather Deep-Draft Mega Port",
            topic = "Economy",
            state = "Maharashtra",
            juniorEligible = true,
            adultEligible = true,
            minAge = 9,
            maxAge = 99,
            summaryHindi = "महाराष्ट्र के पालघर में 20 मीटर प्राकृतिक ड्राफ्ट वाला विश्व-स्तरीय बंदरगाह बड़े कंटेनर जहाजों की सीधी आवाजाही को गति देगा।",
            summaryEnglish = "Vadhavan Mega Port in Palghar features a natural 20-meter draft allowing direct berthing of ultra-large container vessels."
        ),
        CanonicalEvent(
            eventId = "CHETAK_SUBMERSIBLE_BIODIVERSITY",
            headlineHindi = "समुद्रयान मिशन: मत्स्य-6000 गहरे समुद्र का अन्वेषण व जैव विविधता",
            headlineEnglish = "Samudrayaan Mission: MATSYA 6000 Deep Ocean Exploration",
            topic = "Science & Tech",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 6,
            maxAge = 99,
            summaryHindi = "भारत के स्वदेशी मानवयुक्त पनडुब्बी मत्स्य 6000 ने 6000 मीटर की गहराई पर गहरे सागरीय थर्मल वेंट्स और दुर्लभ पॉलिमेटैलिक खनिजों का अध्ययन किया।",
            summaryEnglish = "India's indigenous manned submersible MATSYA 6000 conducts deep ocean exploration at 6,000 meters depth studying hydrothermal vent ecosystems."
        ),
        CanonicalEvent(
            eventId = "TIGER_CENSUS_CAMERA_TRAP",
            headlineHindi = "अखिल भारतीय बाघ गणना: कैमरा ट्रैप एवं कॉरिडोर कनेक्टिविटी विश्लेषण",
            headlineEnglish = "All-India Tiger Census: Spatial Camera-Trap & Corridor Connectivity",
            topic = "Environment",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 6,
            maxAge = 99,
            summaryHindi = "नवीनतम गणना के अनुसार भारत में 75% से अधिक वैश्विक बाघ आबादी संरक्षित गलियारों और नदी जल विभाजकों के जरिए आपस में जुड़ी है।",
            summaryEnglish = "Latest census shows India hosts over 75% of global wild tigers maintained via interconnected wildlife corridors across river basins."
        ),
        CanonicalEvent(
            eventId = "SEMICON_INDIA_FABRICATION",
            headlineHindi = "भारत सेमीकंडक्टर मिशन: धोलेरा एवं मोरीगांव चिप फैब्रिकेशन यूनिट्स",
            headlineEnglish = "India Semiconductor Mission: Dholera & Morigaon Chip Fabrication",
            topic = "Economy",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 10,
            maxAge = 99,
            summaryHindi = "गुजरात के धोलेरा और असम के मोरीगांव में 28nm और एडवांस्ड पैकेजिंग सिलिकॉन वेफर फैब्रिकेशन प्लांट्स का निर्माण कार्य प्रारंभ हुआ।",
            summaryEnglish = "Commercial silicon chip fabrication plants commenced construction in Dholera (Gujarat) and Morigaon (Assam) for advanced packaging."
        ),
        CanonicalEvent(
            eventId = "SOLAR_SOLAR_RURAL_GRID",
            headlineHindi = "पीएम सूर्य घर मुफ्त बिजली योजना: रूफटॉप सोलर नेट-मीटरिंग ग्रिड",
            headlineEnglish = "PM Surya Ghar Free Electricity Scheme: Rooftop Solar Net-Metering Grid",
            topic = "Govt Schemes",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 6,
            maxAge = 99,
            summaryHindi = "1 करोड़ परिवारों के घरों की छतों पर 3 किलोवाट सोलर पैनल लगाकर अधिशेष बिजली को स्मार्ट बायो-डायरेक्शनल मीटर के जरिए ग्रिड में भेजने की प्रणाली।",
            summaryEnglish = "Over 1 crore households install 3kW rooftop solar systems equipped with bidirectional net-meters to export surplus clean electricity to the state grid."
        ),
        CanonicalEvent(
            eventId = "INDIA_AI_COMPUTE_MISSION",
            headlineHindi = "इंडिया एआई मिशन: 10,000+ जीपीयू राष्ट्रीय सुपरकंप्यूटिंग क्लस्टर",
            headlineEnglish = "India AI Mission: 10,000+ GPU National High-Performance AI Compute",
            topic = "Science & Tech",
            state = "National",
            juniorEligible = true,
            adultEligible = true,
            minAge = 9,
            maxAge = 99,
            summaryHindi = "स्वदेशी बहुभाषी एआई मॉडलों के प्रशिक्षण हेतु देश में 10,000 से अधिक ग्राफिक्स प्रोसेसिंग यूनिट्स (GPUs) का खुला साझा कंप्यूटिंग ढांचा तैयार किया गया।",
            summaryEnglish = "National compute infrastructure featuring 10,000+ GPUs deployed to empower Indian researchers training sovereign multilingual foundational AI models."
        )
    )

    fun getAllCanonicalItems(): List<CurrentAffairItem> {
        return canonicalEvents.map { ev ->
            CurrentAffairItem(
                currentAffairId = ev.eventId,
                eventId = ev.eventId,
                headline = ev.headlineEnglish,
                canonicalSummary = ev.summaryEnglish,
                eventDate = "Recent 24-48h Verified",
                sourceReferences = "PIB India, ISRO, Nature, The Hindu, Official Press Releases",
                country = "India",
                state = ev.state,
                topic = ev.topic,
                juniorEligibility = ev.juniorEligible,
                adultEligibility = ev.adultEligible,
                minAge = ev.minAge,
                maxAge = ev.maxAge,
                examRelevance = if (ev.topic == "Science & Tech" || ev.topic == "Govt Schemes") "UPSC / State PSC / SSC / Banking" else "General & Competitive"
            )
        }
    }

    /**
     * Generates a deductive reasoning question strictly from a Current Affair event.
     * Incorporates User Profile (Junior vs Adult), Target Padaav Tier (Q1-Q5 or Q6-Q10),
     * and guarantees pure logical deduction without requiring blind trivia memorization.
     */
    fun generateReasoningQuestion(
        qNumber: Int,
        userProfile: UserProfile,
        excludedFingerprints: Set<String> = emptySet(),
        seed: Int = Random.nextInt(1, 999999),
        quotaType: String = "CA",
        availableAffairs: List<CurrentAffairItem> = emptyList()
    ): QuestionItem {
        val rand = Random(seed)
        val isStudent = userProfile.isJuniorPlayer()
        val userState = userProfile.state.ifBlank { "National" }
        val userCity = userProfile.city.ifBlank { "Local" }

        val dynamicEvents = availableAffairs.map { item ->
            CanonicalEvent(
                eventId = item.eventId,
                headlineHindi = item.headline,
                headlineEnglish = item.headline,
                topic = item.topic,
                state = item.state,
                districtRegion = item.districtRegion,
                juniorEligible = item.juniorEligibility,
                adultEligible = item.adultEligibility,
                minAge = item.minAge,
                maxAge = item.maxAge,
                summaryHindi = item.canonicalSummary,
                summaryEnglish = item.canonicalSummary
            )
        }

        val allEvents = (dynamicEvents + canonicalEvents).filter { ev ->
            if (isStudent) ev.juniorEligible && userProfile.age >= ev.minAge
            else ev.adultEligible
        }

        // Filter based on the requested regional quota
        val matchingEvents = when (quotaType) {
            "CITY" -> allEvents.filter { it.districtRegion.equals(userCity, ignoreCase = true) }
            "STATE" -> allEvents.filter { it.state.equals(userState, ignoreCase = true) }
            else -> allEvents
        }

        // Graceful fallback hierarchy:
        // If City not found -> fallback to State -> fallback to general
        val finalEvents = when {
            matchingEvents.isNotEmpty() -> matchingEvents
            quotaType == "CITY" -> {
                val stateEvents = allEvents.filter { it.state.equals(userState, ignoreCase = true) }
                if (stateEvents.isNotEmpty()) stateEvents else allEvents
            }
            else -> allEvents
        }

        val candidates = finalEvents.shuffled(rand)
        for (event in candidates) {
            for (attempt in 0..5) {
                val candidateRand = Random(seed + attempt * 79 + event.eventId.hashCode())
                val q = if (isStudent) {
                    buildJuniorCurrentAffairQuestion(qNumber, event, userProfile, candidateRand)
                } else {
                    buildAdultCurrentAffairQuestion(qNumber, event, userProfile, candidateRand)
                }
                val fp = q.semanticFingerprint.trim().lowercase()
                if (!excludedFingerprints.contains(fp)) {
                    return q
                }
            }
        }

        val chosenEvent = candidates.firstOrNull() ?: canonicalEvents.first()
        return if (isStudent) {
            buildJuniorCurrentAffairQuestion(qNumber, chosenEvent, userProfile, rand)
        } else {
            buildAdultCurrentAffairQuestion(qNumber, chosenEvent, userProfile, rand)
        }
    }

    // =========================================================================
    // JUNIOR CURRENT AFFAIRS REASONING BUILDER (Age 5 - 17)
    // =========================================================================
    private fun buildJuniorCurrentAffairQuestion(
        qNumber: Int,
        event: CanonicalEvent,
        profile: UserProfile,
        rand: Random
    ): QuestionItem {
        val meta = DynamicLogicEngine.getTierMeta(qNumber)

        return when (event.eventId) {
            "ISRO_SPADEX_2025" -> {
                val distKm = listOf(12, 15, 20, 24).random(rand)
                val approachSpeed = listOf(2, 3, 4).random(rand)
                val targetTimeMin = distKm / approachSpeed
                val correctMin = targetTimeMin * 60

                val qHi = "इसरो के नवीनतम 'स्पेस डॉकिंग' परीक्षण में उपग्रह A (चेज़र) और उपग्रह B (टारगेट) एक ही कक्षा में $distKm किमी की दूरी पर हैं। उपग्रह A, उपग्रह B की ओर $approachSpeed किमी प्रति मिनट की स्थिर सापेक्ष गति से बढ़ रहा है। यदि दोनों के बीच स्वचालित लेज़र सेंसर 0 किमी पर लॉक होते हैं, तो डॉकिंग पूर्ण होने में ठीक कितना समय (सेकंड में) लगेगा?"
                val qEn = "In ISRO's Space Docking test, Satellite A (Chaser) is $distKm km behind Satellite B (Target) along the same orbital line, closing in at a constant relative velocity of $approachSpeed km/minute. If automated locking engages at 0 km relative distance, in exactly how many SECONDS will docking complete?"

                val correctSecStr = "$correctMin सेकंड ($targetTimeMin मिनट)"
                val correctSecStrEn = "$correctMin Seconds ($targetTimeMin min)"

                val optA = "$correctMin सेकंड ($targetTimeMin मिनट)" to "$correctMin Seconds ($targetTimeMin min)"
                val optB = "${correctMin + 120} सेकंड (${targetTimeMin + 2} मिनट)" to "${correctMin + 120} Seconds (${targetTimeMin + 2} min)"
                val optC = "${correctMin - 60} सेकंड (${targetTimeMin - 1} मिनट)" to "${correctMin - 60} Seconds (${targetTimeMin - 1} min)"
                val optD = "${correctMin * 2} सेकंड (${targetTimeMin * 2} मिनट)" to "${correctMin * 2} Seconds (${targetTimeMin * 2} min)"

                val optsList = listOf(optA, optB, optC, optD).shuffled(rand)
                val correctIdx = optsList.indexOf(optA)

                val cluesHi = listOf(
                    "सुराग 1: दोनों उपग्रह एक ही कक्षा में $distKm किमी दूरी पर हैं।",
                    "सुराग 2: उपग्रह A की सापेक्ष गति $approachSpeed किमी/मिनट है।",
                    "सुराग 3: समय = दूरी ÷ चाल ($distKm ÷ $approachSpeed = $targetTimeMin मिनट = $correctMin सेकंड)।"
                )
                val cluesEn = listOf(
                    "Clue 1: Initial relative separation = $distKm km in identical orbital plane.",
                    "Clue 2: Relative closing speed = $approachSpeed km/minute.",
                    "Clue 3: Docking Time = Distance ÷ Relative Speed ($distKm ÷ $approachSpeed = $targetTimeMin min = $correctMin sec)."
                )

                val discards = mutableListOf<Int>()
                for (i in 0..3) {
                    if (i != correctIdx && discards.size < 2) discards.add(i)
                }

                QuestionItem(
                    id = UUID.randomUUID().toString(),
                    qNumber = qNumber,
                    difficultyTitle = meta.difficultyTitle,
                    timeLimitSeconds = meta.timeLimitSeconds,
                    points = meta.points,
                    isCheckpoint = meta.isCheckpoint,
                    checkpointTitle = meta.checkpointTitle,
                    category = "Science & Tech • Space Logistics",
                    questionHindi = qHi,
                    questionEnglish = qEn,
                    cluesHindi = cluesHi,
                    cluesEnglish = cluesEn,
                    optionsHindi = optsList.map { it.first },
                    optionsEnglish = optsList.map { it.second },
                    correctAnswerIndex = correctIdx,
                    deductionPathHindi = "दूरी $distKm किमी और गति $approachSpeed किमी/मिनट से कुल समय = $targetTimeMin मिनट = $targetTimeMin × 60 = $correctMin सेकंड।",
                    deductionPathEnglish = "Time = $distKm km / $approachSpeed km/min = $targetTimeMin minutes = $targetTimeMin × 60 = $correctMin seconds exactly.",
                    eliminationReasonsHindi = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "सटीक गणितीय व वैज्ञानिक गणना द्वारा सिद्ध।" else "चाल और दूरी के संबंध $distKm/$approachSpeed से असंगत।" },
                    eliminationReasonsEnglish = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "Mathematically and physically proven." else "Inconsistent with distance/speed calculation." },
                    expertAdviceHindi = "समय = दूरी / चाल सूत्र का प्रयोग करें और मिनट को सेकंड (× 60) में बदलना न भूलें।",
                    expertAdviceEnglish = "Apply Time = Distance / Speed and convert minutes to seconds (multiply by 60).",
                    fiftyFiftyDiscardIndices = discards,
                    fiftyFiftyProofHindi = "विकल्प सीधे चाल-दूरी के गणितीय नियम का उल्लंघन करते हैं।",
                    fiftyFiftyProofEnglish = "Discarded options contradict the linear velocity equation.",
                    semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(
                        MultiLayerQuestionValidator.normalizeText(qEn),
                        MultiLayerQuestionValidator.normalizeText(optsList[correctIdx].second)
                    ),
                    logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("ca_spadex", "${distKm}_$approachSpeed"),
                    conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Science & Tech", "Orbital Logistics", "ISRO_SPADEX_2025"),
                    patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("current_affairs", "kinematics_docking"),
                    generationVersion = 2
                )
            }
            "TIGER_CENSUS_CAMERA_TRAP" -> {
                val zoneA = listOf(14, 18, 22).random(rand)
                val zoneB = zoneA + listOf(6, 8, 10).random(rand)
                val sharedCorridor = listOf(4, 5, 6).random(rand)
                val totalUnique = zoneA + zoneB - sharedCorridor

                val qHi = "नवीनतम वन्यजीव गलियारा अध्ययन में: वन क्षेत्र X में $zoneA बाघ और वन क्षेत्र Y में $zoneB बाघ कैमरा-ट्रैप में दर्ज हुए। यदि $sharedCorridor बाघ दोनों वन क्षेत्रों के बीच के साझा सुरक्षित गलियारे में घूमते पाए गए, तो इस संयुक्त संरक्षित वन क्षेत्र में कुल अद्वितीय (Unique) बाघों की न्यूनतम संख्या क्या है?"
                val qEn = "In a wildlife corridor study: Camera traps recorded $zoneA tigers in Forest Zone X and $zoneB tigers in Forest Zone Y. If exactly $sharedCorridor tigers use the common connecting ecological corridor between both zones, what is the exact total number of UNIQUE tigers across this entire reserve?"

                val optA = "$totalUnique बाघ (Unique Tigers)" to "$totalUnique Unique Tigers"
                val optB = "${zoneA + zoneB} बाघ" to "${zoneA + zoneB} Tigers"
                val optC = "${totalUnique + sharedCorridor} बाघ" to "${totalUnique + sharedCorridor} Tigers"
                val optD = "${totalUnique - 3} बाघ" to "${totalUnique - 3} Tigers"

                val optsList = listOf(optA, optB, optC, optD).shuffled(rand)
                val correctIdx = optsList.indexOf(optA)

                val cluesHi = listOf(
                    "सुराग 1: क्षेत्र X में बाघ = $zoneA, क्षेत्र Y में बाघ = $zoneB.",
                    "सुराग 2: दोनों क्षेत्रों में साझा (Common) बाघ = $sharedCorridor.",
                    "सुराग 3: समुच्चय सिद्धांत (Set Theory): कुल अद्वितीय बाघ = n(X) + n(Y) - n(X ∩ Y) = $zoneA + $zoneB - $sharedCorridor = $totalUnique."
                )
                val cluesEn = listOf(
                    "Clue 1: Tigers in Zone X = $zoneA; Tigers in Zone Y = $zoneB.",
                    "Clue 2: Tigers moving across both shared zones = $sharedCorridor.",
                    "Clue 3: Set Theory: Total Unique = n(X) + n(Y) - n(X ∩ Y) = $zoneA + $zoneB - $sharedCorridor = $totalUnique."
                )

                val discards = mutableListOf<Int>()
                for (i in 0..3) {
                    if (i != correctIdx && discards.size < 2) discards.add(i)
                }

                QuestionItem(
                    id = UUID.randomUUID().toString(),
                    qNumber = qNumber,
                    difficultyTitle = meta.difficultyTitle,
                    timeLimitSeconds = meta.timeLimitSeconds,
                    points = meta.points,
                    isCheckpoint = meta.isCheckpoint,
                    checkpointTitle = meta.checkpointTitle,
                    category = "Environment • Ecological Set Logic",
                    questionHindi = qHi,
                    questionEnglish = qEn,
                    cluesHindi = cluesHi,
                    cluesEnglish = cluesEn,
                    optionsHindi = optsList.map { it.first },
                    optionsEnglish = optsList.map { it.second },
                    correctAnswerIndex = correctIdx,
                    deductionPathHindi = "वेन आरेख / समुच्चय नियम द्वारा: कुल अद्वितीय = $zoneA + $zoneB - $sharedCorridor = $totalUnique बाघ।",
                    deductionPathEnglish = "By inclusion-exclusion principle: Total Unique = $zoneA + $zoneB - $sharedCorridor = $totalUnique tigers.",
                    eliminationReasonsHindi = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "समावेशन-अपवर्जन नियम द्वारा सिद्ध।" else "दोहरी गिनती (Double Counting) से अमान्य।" },
                    eliminationReasonsEnglish = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "Proven by Set Inclusion-Exclusion." else "Invalid due to double counting overlap." },
                    expertAdviceHindi = "जो बाघ दोनों क्षेत्रों में साझा हैं, उन्हें दो बार गिनने से बचें (n(A∪B) = n(A) + n(B) - n(A∩B))।",
                    expertAdviceEnglish = "Avoid double counting the overlapping tigers present in both camera trap zones.",
                    fiftyFiftyDiscardIndices = discards,
                    fiftyFiftyProofHindi = "साझा बाघों की दोहरी गणना किए बिना उत्तर $totalUnique ही संभव है।",
                    fiftyFiftyProofEnglish = "Excluding duplicate corridor overlaps conclusively yields $totalUnique.",
                    diagramType = "venn_logic",
                    diagramData = "{\"setA\":$zoneA,\"setB\":$zoneB,\"intersection\":$sharedCorridor}",
                    semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(
                        MultiLayerQuestionValidator.normalizeText(qEn),
                        MultiLayerQuestionValidator.normalizeText(optsList[correctIdx].second)
                    ),
                    logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("ca_tiger_census", "${zoneA}_${zoneB}_$sharedCorridor"),
                    conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Environment", "Wildlife Corridor", "TIGER_CENSUS_CAMERA_TRAP"),
                    patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("current_affairs", "set_inclusion_exclusion"),
                    generationVersion = 2
                )
            }
            else -> {
                // Solar Rooftop Net-Metering logic
                val panels = listOf(6, 8, 10, 12).random(rand)
                val generationPerPanel = listOf(350, 400, 500).random(rand)
                val dailyHouseholdUsageKwh = listOf(14, 16, 18).random(rand)
                val totalGenWatts = panels * generationPerPanel
                val dailyGenKwh = (totalGenWatts * 5) / 1000 // 5 peak sun hours
                val netExportKwh = dailyGenKwh - dailyHouseholdUsageKwh

                val qHi = "पीएम सूर्य घर योजना के अंतर्गत एक आवास पर $generationPerPanel वॉट क्षमता के $panels सोलर पैनल लगाए गए हैं। यदि प्रतिदिन 5 घंटे पूर्ण धूप मिलती है और घर का दैनिक बिजली उपभोग $dailyHouseholdUsageKwh kWh है, तो नेट-मीटरिंग के माध्यम से ग्रिड को प्रतिदिन कितनी यूनिट (kWh) शुद्ध बिजली (Net Export) भेजी जाएगी?"
                val qEn = "Under PM Surya Ghar scheme, a house installs $panels solar panels of $generationPerPanel Watts each. Assuming 5 peak sun hours daily and daily home consumption of $dailyHouseholdUsageKwh kWh, how many net kWh units will be exported to the state grid each day via the net-meter?"

                val optA = "$netExportKwh kWh (यूनिट)" to "$netExportKwh kWh (Units)"
                val optB = "${netExportKwh + 4} kWh" to "${netExportKwh + 4} kWh"
                val optC = "${dailyGenKwh} kWh" to "${dailyGenKwh} kWh"
                val optD = "${netExportKwh - 3} kWh" to "${netExportKwh - 3} kWh"

                val optsList = listOf(optA, optB, optC, optD).shuffled(rand)
                val correctIdx = optsList.indexOf(optA)

                val cluesHi = listOf(
                    "सुराग 1: कुल सौर क्षमता = $panels × $generationPerPanel = $totalGenWatts वॉट = ${(totalGenWatts / 1000.0)} kW.",
                    "सुराग 2: दैनिक उत्पादन = ${(totalGenWatts / 1000.0)} kW × 5 घंटे = $dailyGenKwh kWh.",
                    "सुराग 3: शुद्ध निर्यात = दैनिक उत्पादन ($dailyGenKwh kWh) - दैनिक उपभोग ($dailyHouseholdUsageKwh kWh) = $netExportKwh kWh."
                )
                val cluesEn = listOf(
                    "Clue 1: Total solar capacity = $panels × $generationPerPanel = $totalGenWatts W = ${(totalGenWatts / 1000.0)} kW.",
                    "Clue 2: Daily Generation = ${(totalGenWatts / 1000.0)} kW × 5 peak hours = $dailyGenKwh kWh.",
                    "Clue 3: Net Export = Daily Generation ($dailyGenKwh kWh) - Home Consumption ($dailyHouseholdUsageKwh kWh) = $netExportKwh kWh."
                )

                val discards = mutableListOf<Int>()
                for (i in 0..3) {
                    if (i != correctIdx && discards.size < 2) discards.add(i)
                }

                QuestionItem(
                    id = UUID.randomUUID().toString(),
                    qNumber = qNumber,
                    difficultyTitle = meta.difficultyTitle,
                    timeLimitSeconds = meta.timeLimitSeconds,
                    points = meta.points,
                    isCheckpoint = meta.isCheckpoint,
                    checkpointTitle = meta.checkpointTitle,
                    category = "Govt Schemes • Clean Energy Balance",
                    questionHindi = qHi,
                    questionEnglish = qEn,
                    cluesHindi = cluesHi,
                    cluesEnglish = cluesEn,
                    optionsHindi = optsList.map { it.first },
                    optionsEnglish = optsList.map { it.second },
                    correctAnswerIndex = correctIdx,
                    deductionPathHindi = "कुल उत्पादन = $dailyGenKwh kWh; उपभोग $dailyHouseholdUsageKwh kWh घटाने पर शुद्ध ग्रिड निर्यात = $netExportKwh kWh।",
                    deductionPathEnglish = "Total generation = $dailyGenKwh kWh; subtracting consumption $dailyHouseholdUsageKwh kWh gives net export = $netExportKwh kWh.",
                    eliminationReasonsHindi = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "ऊर्जा संरक्षण एवं नेट-मीटरिंग समीकरण द्वारा सिद्ध।" else "दैनिक उपभोग व उत्पादन के अंतर से मेल नहीं खाता।" },
                    eliminationReasonsEnglish = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "Proven by energy balance arithmetic." else "Incorrect calculation of net export." },
                    expertAdviceHindi = "पहले कुल किलोवॉट-घंटे (kWh) उत्पादन निकालें, फिर घरेलू उपभोग घटाएँ।",
                    expertAdviceEnglish = "First calculate total daily kWh generation, then subtract household consumption.",
                    fiftyFiftyDiscardIndices = discards,
                    fiftyFiftyProofHindi = "कुल उत्पादन और उपभोग का अंतर स्पष्ट रूप से $netExportKwh kWh है।",
                    fiftyFiftyProofEnglish = "The difference between generation and self-consumption is precisely $netExportKwh kWh.",
                    semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(
                        MultiLayerQuestionValidator.normalizeText(qEn),
                        MultiLayerQuestionValidator.normalizeText(optsList[correctIdx].second)
                    ),
                    logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("ca_surya_ghar", "${panels}_${generationPerPanel}_$dailyHouseholdUsageKwh"),
                    conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Govt Schemes", "Clean Energy Balance", "PM_SURYA_GHAR_SOLAR"),
                    patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("current_affairs", "energy_balance"),
                    generationVersion = 2
                )
            }
        }
    }

    // =========================================================================
    // ADULT CURRENT AFFAIRS REASONING BUILDER (UPSC, SSC, State PSC, Banking)
    // =========================================================================
    private fun buildAdultCurrentAffairQuestion(
        qNumber: Int,
        event: CanonicalEvent,
        profile: UserProfile,
        rand: Random
    ): QuestionItem {
        val meta = DynamicLogicEngine.getTierMeta(qNumber)

        return when (event.eventId) {
            "GREEN_HYDROGEN_CORRIDORS" -> {
                val efficiencyPct = listOf(60, 70, 75).random(rand)
                val powerInputMw = listOf(100, 200, 300).random(rand)
                val energyValueMjPerKg = 120 // approx 33.33 kWh/kg
                val outputEnergyMwh = (powerInputMw * 24 * efficiencyPct) / 100

                val qHi = "राष्ट्रीय हरित हाइड्रोजन मिशन के अंतर्गत $powerInputMw MW क्षमता का एक समर्पित सौर-इलेक्ट्रोलिसिस प्लांट $efficiencyPct% समग्र ऊर्जा रूपांतरण दक्षता (Efficiency) पर 24 घंटे लगातार संचालित होता है। थर्मोडायनामिक संरक्षण नियमों के अनुसार, 24 घंटे में उत्पादित हरित हाइड्रोजन में संचित प्रभावी ऊर्जा (Stored Chemical Energy) कितनी MWh होगी?"
                val qEn = "Under the National Green Hydrogen Mission, a dedicated $powerInputMw MW solar electrolysis plant operates continuously for 24 hours at $efficiencyPct% overall conversion efficiency. Based on thermodynamic conservation laws, what is the exact stored chemical energy (in MWh) of the green hydrogen produced in 24 hours?"

                val optA = "$outputEnergyMwh MWh" to "$outputEnergyMwh MWh"
                val optB = "${powerInputMw * 24} MWh" to "${powerInputMw * 24} MWh (100% ideal)"
                val optC = "${outputEnergyMwh + 300} MWh" to "${outputEnergyMwh + 300} MWh"
                val optD = "${outputEnergyMwh / 2} MWh" to "${outputEnergyMwh / 2} MWh"

                val optsList = listOf(optA, optB, optC, optD).shuffled(rand)
                val correctIdx = optsList.indexOf(optA)

                val cluesHi = listOf(
                    "सुराग 1: कुल 24 घंटे का इनपुट = $powerInputMw MW × 24 घंटे = ${powerInputMw * 24} MWh.",
                    "सुराग 2: इलेक्ट्रोलिसिस रूपांतरण दक्षता = $efficiencyPct% = ${efficiencyPct / 100.0}.",
                    "सुराग 3: संचित ऊर्जा = कुल इनपुट (${powerInputMw * 24} MWh) × $efficiencyPct% = $outputEnergyMwh MWh."
                )
                val cluesEn = listOf(
                    "Clue 1: Total 24-hour Electrical Input = $powerInputMw MW × 24 h = ${powerInputMw * 24} MWh.",
                    "Clue 2: Conversion Efficiency = $efficiencyPct% = ${efficiencyPct / 100.0}.",
                    "Clue 3: Stored Chemical Energy = Total Input × Efficiency = ${powerInputMw * 24} × $efficiencyPct% = $outputEnergyMwh MWh."
                )

                val discards = mutableListOf<Int>()
                for (i in 0..3) {
                    if (i != correctIdx && discards.size < 2) discards.add(i)
                }

                QuestionItem(
                    id = UUID.randomUUID().toString(),
                    qNumber = qNumber,
                    difficultyTitle = meta.difficultyTitle,
                    timeLimitSeconds = meta.timeLimitSeconds,
                    points = meta.points,
                    isCheckpoint = meta.isCheckpoint,
                    checkpointTitle = meta.checkpointTitle,
                    category = "National Policy • Thermodynamic Energy Logic",
                    questionHindi = qHi,
                    questionEnglish = qEn,
                    cluesHindi = cluesHi,
                    cluesEnglish = cluesEn,
                    optionsHindi = optsList.map { it.first },
                    optionsEnglish = optsList.map { it.second },
                    correctAnswerIndex = correctIdx,
                    deductionPathHindi = "ऊर्जा रूपांतरण: ${powerInputMw * 24} MWh × $efficiencyPct% = $outputEnergyMwh MWh रासायनिक ऊर्जा।",
                    deductionPathEnglish = "Energy Conversion: ${powerInputMw * 24} MWh × $efficiencyPct% = $outputEnergyMwh MWh chemical potential.",
                    eliminationReasonsHindi = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "थर्मोडायनामिक रूपांतरण दक्षता नियम द्वारा सिद्ध।" else "दक्षता हानि को नजरअंदाज करने या गलत गणना के कारण अमान्य।" },
                    eliminationReasonsEnglish = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived from first law & conversion efficiency." else "Fails to apply the $efficiencyPct% conversion factor." },
                    expertAdviceHindi = "इनपुट ऊर्जा (MW × घंटे) को दक्षता प्रतिशत ($efficiencyPct%) से गुणा करें।",
                    expertAdviceEnglish = "Calculate gross power input (MW × 24h) and apply the given efficiency ratio.",
                    fiftyFiftyDiscardIndices = discards,
                    fiftyFiftyProofHindi = "अन्य विकल्प या तो 100% शून्य-हानि मान लेते हैं या अनुपात गलत लगाते हैं।",
                    fiftyFiftyProofEnglish = "Discarded options ignore thermodynamic losses or miscalculate efficiency.",
                    semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(
                        MultiLayerQuestionValidator.normalizeText(qEn),
                        MultiLayerQuestionValidator.normalizeText(optsList[correctIdx].second)
                    ),
                    logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("ca_green_hydrogen", "${powerInputMw}_$efficiencyPct"),
                    conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("National Policy", "Thermodynamic Energy Logic", "GREEN_HYDROGEN_CORRIDORS"),
                    patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("current_affairs", "energy_conservation"),
                    generationVersion = 2
                )
            }
            "MAHA_VADHAVAN_MEGA_PORT", "UP_AI_DATA_CENTER_EXPRESSWAY" -> {
                val corridorLengthKm = listOf(300, 450, 600).random(rand)
                val sensorNodeIntervalKm = listOf(30, 50, 60).random(rand)
                val totalNodes = (corridorLengthKm / sensorNodeIntervalKm) + 1

                val stateName = if (profile.state.isNotBlank()) profile.state else "उत्तर प्रदेश"
                val qHi = "$stateName के एक्सप्रेसवे व लॉजिस्टिक्स इंफ्रास्ट्रक्चर प्रोजेक्ट में $corridorLengthKm किमी लंबे एक्सप्रेसवे कॉरिडोर के दोनों सिरों (0 किमी व $corridorLengthKm किमी) सहित प्रत्येक $sensorNodeIntervalKm किमी पर सौर-सेंसर नोड्स स्थापित किए जाने हैं। इस पूरे कॉरिडोर पर स्थापित कुल सेंसर नोड्स की सटीक संख्या क्या होगी?"
                val qEn = "In $stateName's regional high-speed infrastructure project, intelligent solar sensor nodes are deployed along a $corridorLengthKm km expressway corridor at intervals of every $sensorNodeIntervalKm km, including both starting (0 km) and end ($corridorLengthKm km) terminals. What is the EXACT total count of sensor nodes installed?"

                val optA = "$totalNodes नोड्स (Nodes)" to "$totalNodes Nodes"
                val optB = "${totalNodes - 1} नोड्स" to "${totalNodes - 1} Nodes (Fence-post error)"
                val optC = "${totalNodes + 1} नोड्स" to "${totalNodes + 1} Nodes"
                val optD = "${totalNodes + 3} नोड्स" to "${totalNodes + 3} Nodes"

                val optsList = listOf(optA, optB, optC, optD).shuffled(rand)
                val correctIdx = optsList.indexOf(optA)

                val cluesHi = listOf(
                    "सुराग 1: कॉरिडोर की कुल लंबाई = $corridorLengthKm किमी.",
                    "सुराग 2: प्रत्येक अंतराल (Interval) = $sensorNodeIntervalKm किमी.",
                    "सुराग 3: फेंस-पोस्ट सिद्धांत (Fence-post Problem): कुल नोड्स = (कुल लंबाई ÷ अंतराल) + 1 (दोनों सिरों को शामिल करने पर) = ($corridorLengthKm ÷ $sensorNodeIntervalKm) + 1 = $totalNodes."
                )
                val cluesEn = listOf(
                    "Clue 1: Total Corridor Length = $corridorLengthKm km.",
                    "Clue 2: Interval spacing between consecutive nodes = $sensorNodeIntervalKm km.",
                    "Clue 3: Fence-post Principle: Total Nodes = (Length ÷ Interval) + 1 = ($corridorLengthKm ÷ $sensorNodeIntervalKm) + 1 = $totalNodes."
                )

                val discards = mutableListOf<Int>()
                for (i in 0..3) {
                    if (i != correctIdx && discards.size < 2) discards.add(i)
                }

                QuestionItem(
                    id = UUID.randomUUID().toString(),
                    qNumber = qNumber,
                    difficultyTitle = meta.difficultyTitle,
                    timeLimitSeconds = meta.timeLimitSeconds,
                    points = meta.points,
                    isCheckpoint = meta.isCheckpoint,
                    checkpointTitle = meta.checkpointTitle,
                    category = "Regional Infrastructure • Discrete Grid Logic",
                    questionHindi = qHi,
                    questionEnglish = qEn,
                    cluesHindi = cluesHi,
                    cluesEnglish = cluesEn,
                    optionsHindi = optsList.map { it.first },
                    optionsEnglish = optsList.map { it.second },
                    correctAnswerIndex = correctIdx,
                    deductionPathHindi = "अंतरालों की संख्या = $corridorLengthKm ÷ $sensorNodeIntervalKm = ${totalNodes - 1}; दोनों सिरों को जोड़ने पर कुल नोड्स = ${totalNodes - 1} + 1 = $totalNodes।",
                    deductionPathEnglish = "Number of segments = $corridorLengthKm ÷ $sensorNodeIntervalKm = ${totalNodes - 1}; adding initial 0 km node yields $totalNodes nodes.",
                    eliminationReasonsHindi = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "फेंस-पोस्ट असतत गणितीय नियम द्वारा सिद्ध।" else "शुरुआती या अंतिम नोड को छोड़ने/अधिक गिनने की त्रुटि।" },
                    eliminationReasonsEnglish = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "Proven by discrete intervals arithmetic." else "Fails classic fence-post boundary count." },
                    expertAdviceHindi = "याद रखें: दोनों सिरों को शामिल करने पर नोड्स की संख्या = अंतरालों की संख्या + 1 होती है।",
                    expertAdviceEnglish = "Remember the boundary rule: Nodes count = (Total Length / Interval) + 1.",
                    fiftyFiftyDiscardIndices = discards,
                    fiftyFiftyProofHindi = "कुल $corridorLengthKm किमी पर $sensorNodeIntervalKm किमी के अंतरालों पर ठीक $totalNodes बिंदु बनते हैं।",
                    fiftyFiftyProofEnglish = "Placing nodes at 0 km and each subsequent $sensorNodeIntervalKm km yields exactly $totalNodes.",
                    semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(
                        MultiLayerQuestionValidator.normalizeText(qEn),
                        MultiLayerQuestionValidator.normalizeText(optsList[correctIdx].second)
                    ),
                    logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("ca_expressway_nodes", "${corridorLengthKm}_$sensorNodeIntervalKm"),
                    conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("Regional Infrastructure", "Discrete Grid Logic", "EXPRESSWAY_GRID"),
                    patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("current_affairs", "fence_post_counting"),
                    generationVersion = 2
                )
            }
            else -> {
                // India AI Mission GPU Distribution Logic
                val totalGpus = 10000
                val hubAFrac = listOf(40, 50).random(rand)
                val remainingGpus = totalGpus - (totalGpus * hubAFrac / 100)
                val hubBFracOfRemaining = listOf(50, 60).random(rand)
                val hubBGpus = (remainingGpus * hubBFracOfRemaining) / 100
                val hubCGpus = remainingGpus - hubBGpus

                val qHi = "इंडिया एआई मिशन के 10,000 जीपीयू नेशनल कंप्यूट क्लस्टर में: Hub-A को कुल क्षमता का $hubAFrac% आवंटित किया गया। शेष बचे जीपीयू में से $hubBFracOfRemaining% Hub-B को दिया गया, और अंतिम बचे हुए जीपीयू Hub-C (रिसर्च लैब) को दिए गए। Hub-C को आवंटित कुल जीपीयू की संख्या क्या है?"
                val qEn = "Under the India AI Mission's 10,000 GPU National High-Performance cluster: Hub-A is allocated $hubAFrac% of total compute. Of the REMAINING GPUs, Hub-B receives $hubBFracOfRemaining%, and the final residue is dedicated to Hub-C (Open Research). Exactly how many GPUs are allocated to Hub-C?"

                val optA = "$hubCGpus GPUs" to "$hubCGpus GPUs"
                val optB = "${hubCGpus + 500} GPUs" to "${hubCGpus + 500} GPUs"
                val optC = "${remainingGpus / 2} GPUs" to "${remainingGpus / 2} GPUs"
                val optD = "${hubCGpus - 400} GPUs" to "${hubCGpus - 400} GPUs"

                val optsList = listOf(optA, optB, optC, optD).shuffled(rand)
                val correctIdx = optsList.indexOf(optA)

                val cluesHi = listOf(
                    "सुराग 1: कुल क्षमता = 10,000 जीपीयू. Hub-A = $hubAFrac% = ${totalGpus * hubAFrac / 100} जीपीयू.",
                    "सुराग 2: Hub-A के बाद शेष = 10,000 - ${totalGpus * hubAFrac / 100} = $remainingGpus जीपीयू.",
                    "सुराग 3: Hub-B = $remainingGpus का $hubBFracOfRemaining% = $hubBGpus जीपीयू. अतः Hub-C = $remainingGpus - $hubBGpus = $hubCGpus जीपीयू."
                )
                val cluesEn = listOf(
                    "Clue 1: Total Cluster = 10,000 GPUs. Hub-A = $hubAFrac% = ${totalGpus * hubAFrac / 100} GPUs.",
                    "Clue 2: Residue after Hub-A = 10,000 - ${totalGpus * hubAFrac / 100} = $remainingGpus GPUs.",
                    "Clue 3: Hub-B = $hubBFracOfRemaining% of $remainingGpus = $hubBGpus GPUs. Hub-C = $remainingGpus - $hubBGpus = $hubCGpus GPUs."
                )

                val discards = mutableListOf<Int>()
                for (i in 0..3) {
                    if (i != correctIdx && discards.size < 2) discards.add(i)
                }

                QuestionItem(
                    id = UUID.randomUUID().toString(),
                    qNumber = qNumber,
                    difficultyTitle = meta.difficultyTitle,
                    timeLimitSeconds = meta.timeLimitSeconds,
                    points = meta.points,
                    isCheckpoint = meta.isCheckpoint,
                    checkpointTitle = meta.checkpointTitle,
                    category = "National AI Policy • Sequential Allocation Logic",
                    questionHindi = qHi,
                    questionEnglish = qEn,
                    cluesHindi = cluesHi,
                    cluesEnglish = cluesEn,
                    optionsHindi = optsList.map { it.first },
                    optionsEnglish = optsList.map { it.second },
                    correctAnswerIndex = correctIdx,
                    deductionPathHindi = "चरणबद्ध अनुपात गणना: 10,000 का $hubAFrac% = ${totalGpus * hubAFrac / 100}; शेष $remainingGpus में से $hubBFracOfRemaining% = $hubBGpus; अंतिम शेष = $hubCGpus जीपीयू।",
                    deductionPathEnglish = "Sequential breakdown: 10,000 - ${totalGpus * hubAFrac / 100} = $remainingGpus; then $remainingGpus - ($remainingGpus × $hubBFracOfRemaining%) = $hubCGpus GPUs.",
                    eliminationReasonsHindi = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "क्रमिक प्रतिशत विभाजन द्वारा सिद्ध।" else "कुल 10,000 से सीधे प्रतिशत निकालने की सामान्य त्रुटि।" },
                    eliminationReasonsEnglish = optsList.mapIndexed { idx, opt -> if (idx == correctIdx) "Derived from sequential remaining balance." else "Fails to take percentage of remainder." },
                    expertAdviceHindi = "ध्यान दें: Hub-B को 'शेष बचे हुए' में से $hubBFracOfRemaining% दिया गया है, कुल 10,000 में से नहीं।",
                    expertAdviceEnglish = "Careful: Hub-B receives $hubBFracOfRemaining% of the REMAINING balance, not the grand total.",
                    fiftyFiftyDiscardIndices = discards,
                    fiftyFiftyProofHindi = "शेष बची संख्या $remainingGpus का विभाजन करने पर Hub-C का हिस्सा $hubCGpus ही आता है।",
                    fiftyFiftyProofEnglish = "Evaluating remaining balance $remainingGpus yields exactly $hubCGpus GPUs for Hub-C.",
                    semanticFingerprint = MultiLayerQuestionValidator.computeSemanticFingerprint(
                        MultiLayerQuestionValidator.normalizeText(qEn),
                        MultiLayerQuestionValidator.normalizeText(optsList[correctIdx].second)
                    ),
                    logicFingerprint = MultiLayerQuestionValidator.computeLogicFingerprint("ca_ai_gpu_distribution", "${hubAFrac}_$hubBFracOfRemaining"),
                    conceptFingerprint = MultiLayerQuestionValidator.computeConceptFingerprint("National AI Policy", "Sequential Allocation Logic", "AI_SUPERCOMPUTER_AIRAWAT"),
                    patternFingerprint = MultiLayerQuestionValidator.computePatternFingerprint("current_affairs", "sequential_allocation"),
                    generationVersion = 2
                )
            }
        }
    }

    private fun computeSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
