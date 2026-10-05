package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldDark
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun HomeScreen(
    viewModel: QuizViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val language by viewModel.languageMode.collectAsState()
    val isSoundMuted by viewModel.isSoundMuted.collectAsState()
    val mode = language.uppercase()
    val isHi = mode == "HINDI" || mode == "HI"

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_container"),
        color = NavyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Switcher
                OutlinedButton(
                    onClick = { viewModel.toggleLanguage() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    modifier = Modifier.testTag("language_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHi) "English" else "हिंदी", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row {
                    // Sound Mute Toggle
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Sound Toggle",
                            tint = if (isSoundMuted) TextMuted else GoldPrimary
                        )
                    }

                    // History Button
                    IconButton(
                        onClick = { viewModel.navigateToHistory() },
                        modifier = Modifier.testTag("nav_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = InfoCyan
                        )
                    }

                    // Profile Button
                    IconButton(
                        onClick = { viewModel.navigateToProfile() },
                        modifier = Modifier.testTag("nav_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = GoldPrimary
                        )
                    }
                }
            }

            com.example.festival.FestivalBannerCard(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            // TarkShastra Emblem & Title
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(GoldPrimary.copy(alpha = 0.4f), NavyDeepest)
                        )
                    )
                    .border(2.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "TarkShastra Logo",
                    tint = GoldGlow,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isHi) "तर्कशास्त्र" else "TARKSHASTRA",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = GoldGlow,
                    letterSpacing = 2.sp
                )
            )

            Text(
                text = if (isHi) "AI Reasoning Hot Seat • Guessing Is Not An Option" else "AI Reasoning Hot Seat • Guessing Is Not An Option",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // User Profile Vector Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card")
                    .clickable { viewModel.navigateToProfile() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(GoldPrimary.copy(alpha = 0.5f), InfoCyan.copy(alpha = 0.3f))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = userProfile.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${userProfile.preparationDomain} • ${userProfile.state}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isHi) "तर्क प्रोफ़ाइल" else "Logic Profile",
                                color = GoldGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Knowledge Profile Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileMetricBadge("तर्क (Logic)", "${(userProfile.profileVector.logicalReasoning * 100).toInt()}%", GoldPrimary)
                        ProfileMetricBadge("विज्ञान (Sci)", "${(userProfile.profileVector.scienceTech * 100).toInt()}%", InfoCyan)
                        ProfileMetricBadge("GK/Context", "${(userProfile.profileVector.generalKnowledge * 100).toInt()}%", PurpleAccent)
                        ProfileMetricBadge("Spatial", "${(userProfile.profileVector.spatialVisual * 100).toInt()}%", SuccessGreen)
                    }

                    if (userProfile.isStudentMode || userProfile.preparationDomain.contains("Student", true)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(InfoCyan.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .border(1.dp, InfoCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = InfoCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isHi) "KBC जूनियर मोड: ${userProfile.studentClass.ifBlank { "Class 8-10" }} (उम्र: ${userProfile.age} वर्ष)"
                                        else "KBC Junior Mode: ${userProfile.studentClass.ifBlank { "Class 8-10" }} (Age: ${userProfile.age})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = InfoCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Daily Reasoning Challenge / Streak Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.startNewGame() }
                    .testTag("daily_challenge_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(SuccessGreen.copy(alpha = 0.5f), GoldPrimary.copy(alpha = 0.3f))
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Daily",
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHi) "दैनिक तर्क चुनौती (Daily Challenge)" else "Daily Reasoning Challenge",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isHi) "🔥 स्ट्रीक: 3 दिन सक्रिय • आज का तर्क परखें" else "🔥 Streak: 3 Days Active • Test Logic Today",
                                fontSize = 11.sp,
                                color = SuccessGreen
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // IT Professionals & AI Hub Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateToItHub() }
                    .testTag("it_hub_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(InfoCyan.copy(alpha = 0.6f), GoldPrimary.copy(alpha = 0.4f))
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(InfoCyan.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = InfoCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "IT Professionals & AI Hub 💻🤖",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Algorithms, GPU limits, Telemetry & Concurrency Q&A",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Open",
                        tint = GoldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Rules Briefing Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHi) "महा-तर्क के 4 मुख्य स्तम्भ" else "Core Rules of TarkShastra",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RuleRow("17 Tiers", if (isHi) "1,000 से 7,00,00,000 पॉइंट्स तक का सफर" else "Progressive ladder from 1K to 70M Points")
                    RuleRow("Safe Padaav", if (isHi) "Q5 (10K), Q10 (3.2L), Q16 (30M) सुरक्षित पड़ाव" else "Checkpoints secure earned points at Q5, Q10, Q16")
                    RuleRow("100% Logic", if (isHi) "हर सवाल में सुराग मौजूद हैं; कोई अनुमान नहीं" else "Every question is logically solvable from clues")
                    RuleRow("4 Lifelines", if (isHi) "50-50, तर्क गुरु 🤖, Flip 🔄, और Power Paplu ⚡" else "50-50, AI Expert, Flip Question, Power Paplu")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Game CTA Button
            Button(
                onClick = { viewModel.startNewGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_quiz_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Quiz",
                    tint = NavyDeepest,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHi) "महा-तर्क हॉट सीट शुरू करें (Start Quiz)" else "START REASONING HOT SEAT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = NavyDeepest,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileMetricBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun RuleRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .background(GoldPrimary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp))
        }
    }
}
