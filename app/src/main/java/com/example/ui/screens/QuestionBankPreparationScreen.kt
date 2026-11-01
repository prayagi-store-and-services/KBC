package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeepest
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.utils.adaptiveContentWidth
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

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
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .adaptiveContentWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header Animated Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.15f))
                            .border(2.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        when (progress.stage) {
                            PreparationStage.READY -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Ready",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            PreparationStage.ERROR, PreparationStage.RETRY_REQUIRED -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            PreparationStage.CHECKING_NETWORK, PreparationStage.FETCHING_SOURCES -> {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Network Sync",
                                    tint = InfoCyan,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Reasoning Engine",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "तर्कशास्त्र • प्रश्न बैंक निर्माण",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Question Intelligence Pipeline",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = GoldPrimary,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(20.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = progress.detailMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    // Slow Connection Notice
                    if (progress.isSlowConnectionWarning) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarningOrange.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Slow connection",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "धीमी इंटरनेट गति: गेम बैंक तैयार करने में थोड़ा अतिरिक्त समय लग सकता है। (Slow network detected. Preparation may take a little longer.)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarningOrange,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Retry Button on error
                    if (progress.stage == PreparationStage.ERROR || progress.stage == PreparationStage.RETRY_REQUIRED) {
                        Spacer(modifier = Modifier.height(20.dp))
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
