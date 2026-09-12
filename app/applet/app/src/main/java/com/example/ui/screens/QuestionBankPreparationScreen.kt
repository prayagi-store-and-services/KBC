package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.PreparationProgress
import com.example.data.repository.PreparationStage
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun QuestionBankPreparationScreen(
    progress: PreparationProgress,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "question_prep_progress"
    )

    // 120-second countdown window timer (ticks down from 120 to 0 or until READY)
    var secondsRemaining by remember { mutableStateOf(120) }
    LaunchedEffect(progress.stage) {
        if (progress.stage == PreparationStage.READY) {
            secondsRemaining = 0
            return@LaunchedEffect
        }
        while (secondsRemaining > 0 && progress.stage != PreparationStage.READY) {
            delay(1000L)
            if (secondsRemaining > 1 && progress.stage != PreparationStage.READY) {
                secondsRemaining--
            }
        }
    }
    val circularTimeProgress = (secondsRemaining.toFloat() / 120f).coerceIn(0f, 1f)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = NavyBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(NavyDeepest, NavyBackground, Color(0xFF0A111E))
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .border(1.dp, NavyBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Circular 120-Second Countdown & Header Icon
                    Box(
                        modifier = Modifier
                            .size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { circularTimeProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = GoldPrimary,
                            trackColor = Color(0xFF1E293B),
                            strokeWidth = 6.dp
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (progress.stage == PreparationStage.READY) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Ready",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Text(
                                    text = "$secondsRemaining",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                                Text(
                                    text = "SEC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "तर्कशास्त्र • स्मार्ट गेम प्रेपरेशन",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Smart Game Preparation Engine (Max 120s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoldPrimary,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}% Completed",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "Tier ${progress.questionsPreparedCount}/${progress.totalQuestions}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stage Title
                    Text(
                        text = progress.stageTitleHindi,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = progress.stageTitleEnglish,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InfoCyan,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = progress.detailMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stage Checklist (Real preparation stages)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PreparationStageItem(
                            title = "Player Profile & Regional Context",
                            isDone = progress.stage.ordinal >= PreparationStage.FETCHING_SOURCES.ordinal,
                            isCurrent = progress.stage == PreparationStage.CHECKING_NETWORK || progress.stage == PreparationStage.FETCHING_SOURCES
                        )
                        PreparationStageItem(
                            title = "Question Synthesis & Current Affairs",
                            isDone = progress.stage.ordinal >= PreparationStage.VALIDATING_QUESTIONS.ordinal,
                            isCurrent = progress.stage == PreparationStage.GENERATING_QUESTIONS
                        )
                        PreparationStageItem(
                            title = "Multi-Layer Validation & Deduplication",
                            isDone = progress.stage.ordinal >= PreparationStage.FINALIZING_BANK.ordinal,
                            isCurrent = progress.stage == PreparationStage.VALIDATING_QUESTIONS || progress.stage == PreparationStage.REMOVING_DUPLICATES
                        )
                        PreparationStageItem(
                            title = "Difficulty Calibration (1–17 Tiers)",
                            isDone = progress.stage == PreparationStage.READY,
                            isCurrent = progress.stage == PreparationStage.FINALIZING_BANK
                        )
                    }

                    // Slow Connection Notice
                    if (progress.isSlowConnectionWarning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarningOrange.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Slow connection",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "धीमी इंटरनेट गति: गेम बैंक तैयार करने में थोड़ा अतिरिक्त समय लग सकता है। (Slow network detected. Preparing challenge...)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarningOrange,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Retry Button on error
                    if (progress.stage == PreparationStage.ERROR || progress.stage == PreparationStage.RETRY_REQUIRED) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "पुनः प्रयास करें (Retry)",
                                color = NavyDeepest,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreparationStageItem(
    title: String,
    isDone: Boolean,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = if (isDone) "✓" else if (isCurrent) "⟳" else "○"
            val color = if (isDone) SuccessGreen else if (isCurrent) GoldPrimary else TextSecondary
            Text(text = icon, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDone || isCurrent) TextPrimary else TextSecondary,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        if (isCurrent) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 2.dp,
                color = GoldPrimary
            )
        }
    }
}
