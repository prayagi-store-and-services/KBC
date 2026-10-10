package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameSessionResult
import com.example.data.model.QuestionItem
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CheckpointGold
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeepest
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun SummaryScreen(
    result: GameSessionResult,
    lastQuestion: QuestionItem?,
    viewModel: QuizViewModel,
    modifier: Modifier = Modifier
) {
    val isGrandWin = result.isCompletedWon
    val context = LocalContext.current

    val totalSec = result.totalResponseTimeSec.toInt()
    val totalMin = totalSec / 60
    val totalRemSec = totalSec % 60
    val formattedTotalTime = String.format("%02d:%02d", totalMin, totalRemSec)

    val avgSec = result.averageResponseTimeSec
    val formattedAvgTime = String.format("%.1fs", avgSec)

    val shareText = "🎯 ${com.example.Brand.name} — Reasoning & Achievement Card\n" +
            "👤 Challenger: ${result.userName}\n" +
            "🏆 Highest Q: Q${result.highestQuestionReached} / 17\n" +
            "🏆 Points Won: ${result.totalPointsWon}\n" +
            "✅ Correct: ${result.correctCount} | ❌ Wrong: ${result.wrongCount}\n" +
            "⏱️ Total Response Time: $formattedTotalTime (Avg: $formattedAvgTime)\n" +
            "💡 Hints Used: ${result.hintsUsedCount} | 🛡️ Lifelines Used: ${result.lifelinesUsedCount}\n" +
            "📚 Game Mode: ${result.gameMode} (${result.examContext})\n" +
            "Reason. Solve. Win."

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("summary_screen_container"),
        color = NavyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // User Name Badge
            Box(
                modifier = Modifier
                    .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "👤 ${result.userName} • ${result.gameMode}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = GoldGlow,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trophy / Crown Emblem
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isGrandWin) CheckpointGold else GoldPrimary.copy(alpha = 0.4f),
                                NavyDeepest
                            )
                        )
                    )
                    .border(3.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = GoldGlow,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isGrandWin) "महा-तर्क विजयी! (Grand Champion)" else "खेल समाप्त (Game Summary)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = GoldGlow,
                    fontWeight = FontWeight.Black
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Explicit End Reason Pill
            val (badgeText, badgeBgColor, badgeTextColor) = when {
                result.reasonEnded == "CLEARED_7_CRORE" -> Triple("7 CRORE CHAMPION", SuccessGreen.copy(alpha = 0.25f), SuccessGreen)
                result.reasonEnded == "QUIT" -> Triple("VOLUNTARY QUIT", GoldPrimary.copy(alpha = 0.3f), GoldGlow)
                result.reasonEnded.startsWith("TIMEOUT") -> Triple("TIME EXPIRED / TIMEOUT", AlertRed.copy(alpha = 0.25f), AlertRed)
                result.reasonEnded == "HOME_EXIT" -> Triple("GAME EXIT (BACKGROUND)", AlertRed.copy(alpha = 0.25f), AlertRed)
                else -> Triple("WRONG ANSWER — GAME OVER", AlertRed.copy(alpha = 0.25f), AlertRed)
            }

            Box(
                modifier = Modifier
                    .background(badgeBgColor, RoundedCornerShape(6.dp))
                    .border(1.dp, badgeTextColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (result.reasonEnded) {
                    "CLEARED_7_CRORE" -> "आपने 7 करोड़ पॉइंट्स का महा-तर्क पूर्ण रूप से सिद्ध कर दिया!"
                    "QUIT" -> "आपने स्वेच्छा से खेल छोड़ा और वर्तमान राशि सुरक्षित की।"
                    "TIMEOUT_NO_SELECTION" -> "समय सीमा समाप्त! कोई विकल्प नहीं चुना गया। सुरक्षित पड़ाव राशि देय है।"
                    "TIMEOUT_SELECTED_CORRECT" -> "समय सीमा समाप्त! सही विकल्प चुना था परंतु समय रहते ताला (Lock) नहीं लगाया।"
                    "TIMEOUT_SELECTED_INCORRECT" -> "समय सीमा समाप्त! चुना गया विकल्प गलत था और ताला नहीं लगा।"
                    "TIMEOUT" -> "समय सीमा समाप्त! सुरक्षित पड़ाव राशि सुरक्षित है।"
                    "HOME_EXIT" -> "ऐप से बाहर जाने के कारण खेल समाप्त किया गया।"
                    "WRONG_ANSWER" -> "गलत उत्तर लॉक किया गया! खेल यहीं समाप्त होता है। सुरक्षित पड़ाव राशि देय है।"
                    else -> "खेल समाप्त! सुरक्षित पड़ाव राशि सुरक्षित है।"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Total Points & Comprehensive Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(GoldPrimary, InfoCyan)
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "कुल अर्जित पॉइंट्स (Total Points Won)",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (result.totalPointsWon >= 10000000) "${result.totalPointsWon / 1000}k POINTS" else "${result.totalPointsWon} POINTS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = GoldGlow,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1 Stats: Level, Correct, Wrong, Accuracy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Q${result.highestQuestionReached}/17", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(text = "उच्चतम पड़ाव", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${result.correctCount}", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 14.sp)
                            Text(text = "सही उत्तर", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${result.wrongCount}", fontWeight = FontWeight.Bold, color = com.example.ui.theme.AlertRed, fontSize = 14.sp)
                            Text(text = "गलत उत्तर", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${result.logicAccuracyPercentage}%", fontWeight = FontWeight.Bold, color = InfoCyan, fontSize = 14.sp)
                            Text(text = "सटीकता", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2 Stats: Total Time, Avg Time, Lifelines, Hints
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = formattedTotalTime, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text(text = "कुल समय", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = formattedAvgTime, fontWeight = FontWeight.Bold, color = GoldGlow, fontSize = 13.sp)
                            Text(text = "औसत समय/प्र.", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${result.lifelinesUsedCount}", fontWeight = FontWeight.Bold, color = CheckpointGold, fontSize = 13.sp)
                            Text(text = "लाइफलाइन", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${result.hintsUsedCount}", fontWeight = FontWeight.Bold, color = InfoCyan, fontSize = 13.sp)
                            Text(text = "संकेत", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Step-by-Step Proof of Last Question Review
            if (lastQuestion != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Proof",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "अंतिम सवाल का तार्किक प्रमाण (Logic Proof):",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = lastQuestion.deductionPathHindi,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Share Achievement Card Button
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("share_achievement_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InfoCyan)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = NavyDeepest)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "शेयर करें (Share Achievement)",
                    color = NavyDeepest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Button(
                onClick = { viewModel.startNewGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("play_again_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = NavyDeepest)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "पुनः खेलें (Play Again)",
                    color = NavyDeepest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.navigateToHome() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("summary_home_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("मुख्य पृष्ठ (Home)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
