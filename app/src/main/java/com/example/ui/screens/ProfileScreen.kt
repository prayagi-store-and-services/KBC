package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeepest
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuizViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: QuizViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasMicrophonePermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicrophonePermission = isGranted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // 1. Profile Mode: "Adult" vs "Junior"
    var isJuniorMode by remember(currentProfile) {
        mutableStateOf(currentProfile.isStudentMode || currentProfile.preparationDomain.contains("Student", true) || currentProfile.age < 18)
    }

    var name by remember(currentProfile) { mutableStateOf(currentProfile.name) }
    var selectedState by remember(currentProfile) { mutableStateOf(currentProfile.state) }
    var selectedCity by remember(currentProfile) { mutableStateOf(currentProfile.city) }
    var selectedLanguage by remember(currentProfile) { 
        val lang = currentProfile.languageMode.uppercase()
        mutableStateOf(if (lang == "HINDI") "HINDI" else "ENGLISH")
    }
    var selectedHostGender by remember(currentProfile) {
        val gender = currentProfile.hostGender.uppercase()
        mutableStateOf(if (gender == "MALE") "MALE" else "FEMALE")
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Junior Specific States
    val juniorAges = (5..17).toList()
    var selectedJuniorAge by remember(currentProfile) {
        val initialAge = if (currentProfile.age in 5..17) currentProfile.age else 12
        mutableIntStateOf(initialAge)
    }

    fun ageToClass(age: Int): String {
        return when (age) {
            5, 6 -> "Early Primary (Class 1-2)"
            7, 8 -> "Primary (Class 2-3)"
            9, 10 -> "Upper Primary (Class 4-5)"
            11, 12 -> "Middle School (Class 6-7)"
            13, 14 -> "Secondary (Class 8-9)"
            15, 16 -> "Senior Secondary (Class 10-11)"
            17 -> "Senior Secondary / Transition (Class 12)"
            else -> "Middle School (Class 6-7)"
        }
    }

    var selectedClass by remember(currentProfile, selectedJuniorAge) {
        mutableStateOf(
            if (currentProfile.studentClass.isNotBlank() && currentProfile.isStudentMode) currentProfile.studentClass
            else ageToClass(selectedJuniorAge)
        )
    }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Adult Specific States
    var adultAgeText by remember(currentProfile) {
        val initialAdultAge = if (currentProfile.age >= 18) currentProfile.age.toString() else "24"
        mutableStateOf(initialAdultAge)
    }
    var selectedAdultDomain by remember(currentProfile) {
        val domain = if (currentProfile.preparationDomain.isNotBlank() && !currentProfile.preparationDomain.contains("Student", true)) currentProfile.preparationDomain else "Logic"
        mutableStateOf(domain)
    }
    var selectedEducation by remember(currentProfile) { mutableStateOf(currentProfile.educationLevel.ifBlank { "Graduate" }) }
    var selectedOccupation by remember(currentProfile) { mutableStateOf(currentProfile.occupation.ifBlank { "Aspirant / Student" }) }
    var domainExpanded by remember { mutableStateOf(false) }
    var eduExpanded by remember { mutableStateOf(false) }

    val adultDomains = listOf(
        "Logic",
        "SSC / State Exams",
        "UPSC / Civil Services",
        "Banking / IBPS",
        "Engineering / Tech",
        "Management / CAT",
        "General Puzzles & Logic",
        "IT Professionals & AI (कोडिंग और एआई)"
    )

    val educationLevels = listOf(
        "Undergraduate (स्नातक स्तर)",
        "Graduate (स्नातक)",
        "Post Graduate (स्नातकोत्तर)",
        "Doctorate / Professional (विशेषज्ञ)"
    )

    val juniorInterestsList = listOf(
        "Science Riddles (विज्ञान पहेलियाँ)", "Animal & Nature (प्रकृति व जीव)",
        "Space & Planets (अंतरिक्ष व ग्रह)", "Number Logic (संख्या तर्क)",
        "Shapes & Geometry (आकार व ज्यामिति)", "Detective Puzzles (जासूसी पहेलियाँ)",
        "Clock & Calendar (घड़ी व कैलेंडर)", "Brain Teasers (तार्किक सवाल)"
    )

    val adultInterestsList = listOf(
        "Logical Deductions", "Spatial Vectors", "Forensic Timelines",
        "Syllogisms & Set Theory", "Probability & Risk", "Acoustic Meters",
        "Financial Logic", "Cryptarithms"
    )

    val selectedInterests = remember(currentProfile, isJuniorMode) {
        mutableStateListOf<String>().apply {
            if (currentProfile.interests.isNotEmpty()) {
                addAll(currentProfile.interests)
            } else {
                if (isJuniorMode) addAll(listOf("Science Riddles (विज्ञान पहेलियाँ)", "Number Logic (संख्या तर्क)", "Space & Planets (अंतरिक्ष व ग्रह)"))
                else addAll(listOf("Logical Deductions", "Spatial Vectors", "Syllogisms & Set Theory"))
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_container"),
        color = NavyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ==========================================
            // TOP APP BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateToHome() },
                    modifier = Modifier.testTag("profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "तर्क प्रोफ़ाइल अनुकूलन / User Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldGlow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (isJuniorMode) "🌟 KBC Junior Mode Active" else "🎯 Adult / Aspirant Mode Active",
                        fontSize = 11.sp,
                        color = if (isJuniorMode) InfoCyan else GoldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // ADULT VS JUNIOR SELECTOR TAB
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_type_selector_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(GoldPrimary.copy(alpha = 0.6f), InfoCyan.copy(alpha = 0.6f))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "प्रतियोगिता श्रेणी चुनें (Select Category)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyDeepest, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Junior Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isJuniorMode) Brush.horizontalGradient(listOf(InfoCyan.copy(alpha = 0.8f), InfoCyan))
                                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable {
                                    isJuniorMode = true
                                    selectedClass = ageToClass(selectedJuniorAge)
                                }
                                .padding(vertical = 10.dp)
                                .testTag("select_junior_mode_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isJuniorMode) NavyDeepest else InfoCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Junior (आयु 5-17)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isJuniorMode) NavyDeepest else TextPrimary
                                )
                            }
                        }

                        // Adult Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (!isJuniorMode) Brush.horizontalGradient(listOf(GoldDark, GoldPrimary))
                                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable {
                                    isJuniorMode = false
                                }
                                .padding(vertical = 10.dp)
                                .testTag("select_adult_mode_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (!isJuniorMode) NavyDeepest else GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Adult (आयु 18+)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (!isJuniorMode) NavyDeepest else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // COMMON DETAILS (Name & State)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "मूल विवरण (Basic Details)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isJuniorMode) "विद्यार्थी का नाम (Student Name)" else "प्रतियोगी का नाम (Candidate Name)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isJuniorMode) InfoCyan else GoldPrimary,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = selectedState,
                        onValueChange = { selectedState = it },
                        label = { Text("राज्य / प्रदेश (State / Region)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_state_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isJuniorMode) InfoCyan else GoldPrimary,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = { selectedCity = it },
                        label = { Text("शहर / जिला (City / District)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_city_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isJuniorMode) InfoCyan else GoldPrimary,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "भाषा चयन (Mandatory Language Selection):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("HINDI" to "🇮🇳 Hindi", "ENGLISH" to "🇬🇧 English").forEach { (code, label) ->
                            val isSelected = selectedLanguage.uppercase() == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary else NavyDeepest)
                                    .border(1.dp, if (isSelected) GoldPrimary else NavyBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedLanguage = code }
                                    .padding(vertical = 8.dp)
                                    .testTag("lang_option_$code"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NavyDeepest else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "होस्ट (Host)",
                        color = GoldDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("MALE" to "👨 Male", "FEMALE" to "👩 Female").forEach { (code, label) ->
                            val isSelected = selectedHostGender.uppercase() == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary else NavyDeepest)
                                    .border(1.dp, if (isSelected) GoldPrimary else NavyBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedHostGender = code }
                                    .padding(vertical = 8.dp)
                                    .testTag("host_gender_$code"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NavyDeepest else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // DYNAMIC SECTION: JUNIOR VS ADULT
            // ==========================================
            AnimatedContent(
                targetState = isJuniorMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "profile_mode_switch"
            ) { junior ->
                if (junior) {
                    // ------------------------------------------
                    // JUNIOR MODE REQUIREMENTS
                    // ------------------------------------------
                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(InfoCyan.copy(alpha = 0.6f), SuccessGreen.copy(alpha = 0.4f))
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = InfoCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "KBC Junior: आयु व कक्षा चयन",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = InfoCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "विद्यार्थी की आयु चुनें। कक्षा का निर्धारण व प्रश्नों की तार्किक जटिलता आयु के आधार पर स्वतः अनुकूलित होगी।",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Age Selector (Junior Age Grid Chips)
                                Text(
                                    text = "विद्यार्थी की आयु (Select Age: 5 to 17 Yrs):",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = GoldGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    juniorAges.forEach { age ->
                                        val isSelected = selectedJuniorAge == age
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) InfoCyan else NavyDeepest
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) InfoCyan else NavyBorder,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    selectedJuniorAge = age
                                                    selectedClass = ageToClass(age)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .testTag("junior_age_chip_$age"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$age वर्ष",
                                                color = if (isSelected) NavyDeepest else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Automatic Class Decided Display & Recommendation Confirmation
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(
                                            listOf(SuccessGreen.copy(alpha = 0.6f), InfoCyan.copy(alpha = 0.4f))
                                        )
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "स्वतः निर्धारित कक्षा स्तर (Automatic Band):",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                                Text(
                                                    text = selectedClass,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        HorizontalDivider(color = NavyBorder.copy(alpha = 0.6f))
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "“Based on your age ($selectedJuniorAge), we recommend $selectedClass level.”",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = GoldGlow
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Junior Interests
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "रुचिकर विषय (Favorite Topics for Fun Logic):",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = InfoCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    juniorInterestsList.forEach { interest ->
                                        val isSelected = selectedInterests.contains(interest)
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) InfoCyan.copy(alpha = 0.2f) else NavyCardElevated,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) InfoCyan else NavyBorder,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    if (isSelected) selectedInterests.remove(interest)
                                                    else selectedInterests.add(interest)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = interest,
                                                fontSize = 11.sp,
                                                color = if (isSelected) InfoCyan else TextSecondary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ------------------------------------------
                    // ADULT MODE REQUIREMENTS
                    // ------------------------------------------
                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.6f), PurpleAccent.copy(alpha = 0.4f))
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Work,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "प्रतियोगी परीक्षा व योग्यता विवरण (Adult / Aspirant)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Adult Age
                                OutlinedTextField(
                                    value = adultAgeText,
                                    onValueChange = { adultAgeText = it.filter { char -> char.isDigit() } },
                                    label = { Text("आयु (Adult Age: 18+)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("adult_age_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Exam Domain Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = domainExpanded,
                                    onExpandedChange = { domainExpanded = !domainExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedAdultDomain,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("तैयारी का क्षेत्र (Competitive Exam Domain)") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = domainExpanded) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("adult_domain_selector"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = domainExpanded,
                                        onDismissRequest = { domainExpanded = false }
                                    ) {
                                        adultDomains.forEach { domain ->
                                            DropdownMenuItem(
                                                text = { Text(domain) },
                                                onClick = {
                                                    selectedAdultDomain = domain
                                                    domainExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Education Level Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = eduExpanded,
                                    onExpandedChange = { eduExpanded = !eduExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedEducation,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("शैक्षणिक योग्यता (Highest Education Level)") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eduExpanded) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("adult_education_selector"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = eduExpanded,
                                        onDismissRequest = { eduExpanded = false }
                                    ) {
                                        educationLevels.forEach { level ->
                                            DropdownMenuItem(
                                                text = { Text(level) },
                                                onClick = {
                                                    selectedEducation = level
                                                    eduExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Adult Reasoning Interests
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "तार्किक रुचियां (Reasoning Specialties):",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    adultInterestsList.forEach { interest ->
                                        val isSelected = selectedInterests.contains(interest)
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) GoldPrimary.copy(alpha = 0.25f) else NavyCardElevated,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) GoldPrimary else NavyBorder,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    if (isSelected) selectedInterests.remove(interest)
                                                    else selectedInterests.add(interest)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = interest,
                                                fontSize = 11.sp,
                                                color = if (isSelected) GoldGlow else TextSecondary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            com.example.stats.UsageCountCard()
            Spacer(modifier = Modifier.height(16.dp))
            com.example.update.UpdateCheckCard()
            Spacer(modifier = Modifier.height(16.dp))

            // Permission Request Card (Camera, Microphone & Notifications)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("permission_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "कैमरा, माइक्रोफोन और सूचना अनुमति (Permissions & Security)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Privacy Disclosure Notice (Steps 79 & 80)
                    Text(
                        text = "Camera and microphone access are required for the game's anti-cheating system. During an active game, the camera may be used to verify that the same participant remains in the session, while the microphone may be used to detect possible external verbal assistance. This monitoring is limited to active gameplay. Temporary anti-cheating media/data is deleted when the game/session ends, and it is not retained for unrelated purposes. Notification access is used to provide application notifications.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Camera Permission
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "कैमरा अनुमति (Camera Access)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Required for profile verification and active-game anti-cheating monitoring.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.testTag("grant_camera_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasCameraPermission) SuccessGreen else GoldPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (hasCameraPermission) "Granted ✓" else "Grant",
                                color = NavyDeepest,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Microphone Permission
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "माइक्रोफोन अनुमति (Microphone Access)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Required for anti-cheating audio verification during active gameplay.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier.testTag("grant_microphone_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasMicrophonePermission) SuccessGreen else GoldPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (hasMicrophonePermission) "Granted ✓" else "Grant",
                                color = NavyDeepest,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notification Permission
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "सूचना अनुमति (Notifications)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Notification access is used to show game and application notifications.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            modifier = Modifier.testTag("grant_notification_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasNotificationPermission) SuccessGreen else GoldPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (hasNotificationPermission) "Granted ✓" else "Grant",
                                color = NavyDeepest,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (validationError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF5A1E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = validationError ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // SAVE PROFILE BUTTON
            // ==========================================
            Button(
                onClick = {
                    val finalAge = if (isJuniorMode) selectedJuniorAge else (adultAgeText.toIntOrNull() ?: 0)
                    if (name.isBlank() || selectedState.isBlank() || finalAge <= 0 || selectedLanguage.isBlank()) {
                        validationError = "⚠️ कृपया सभी अनिवार्य फील्ड (नाम, राज्य, आयु, भाषा) भरें।"
                        return@Button
                    }
                    if (selectedInterests.size < 3) {
                        validationError = "⚠️ कम से कम 3 श्रेणियां चुनना अनिवार्य है (Minimum 3 categories required)."
                        return@Button
                    }
                    validationError = null
                    viewModel.setLanguage(selectedLanguage)

                    val finalDomain = if (isJuniorMode) "School Student / KBC Juniors" else selectedAdultDomain
                    val finalClass = if (isJuniorMode) selectedClass else ""
                    val finalEdu = if (isJuniorMode) "School Student" else selectedEducation
                    val finalOcc = if (isJuniorMode) "Junior Student" else selectedOccupation

                    val updated = currentProfile.copy(
                        name = name,
                        age = finalAge,
                        state = selectedState,
                        city = selectedCity,
                        educationLevel = finalEdu,
                        occupation = finalOcc,
                        preparationDomain = finalDomain,
                        studentClass = finalClass,
                        isStudentMode = isJuniorMode,
                        languageMode = selectedLanguage,
                        hostGender = selectedHostGender,
                        upiId = "",
                        interests = selectedInterests.toList()
                    )
                    viewModel.saveProfile(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isJuniorMode) InfoCyan else GoldPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = NavyDeepest
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isJuniorMode) "KBC Junior प्रोफ़ाइल सहेजें (Save Junior Profile)" else "Adult प्रोफ़ाइल सहेजें (Save Adult Profile)",
                    color = NavyDeepest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private val GoldDark = Color(0xFFC69214)
