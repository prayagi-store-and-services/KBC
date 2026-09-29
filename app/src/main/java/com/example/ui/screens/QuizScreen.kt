package com.example.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.outlined.Lock
import com.example.ui.components.DifficultyLevelIndicator
import com.example.ui.components.ScratchpadDialog
import com.example.ui.viewmodel.QuestionPhase
import com.example.ui.viewmodel.TimerMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BilingualOptionsGrid2x2
import com.example.ui.components.BilingualQuestionCard
import com.example.ui.components.DiagramCanvas
import com.example.ui.components.ExpertAdviceDialog
import com.example.ui.components.FiftyFiftyProofDialog
import com.example.ui.components.LadderDrawer
import com.example.ui.components.LifelineControls
import com.example.ui.components.PadaavCelebrationOverlay
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CheckpointBronze
import com.example.ui.theme.CheckpointGold
import com.example.ui.theme.CheckpointSilver
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
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun QuizScreen(
    state: QuizUiState.InGame,
    viewModel: QuizViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.languageMode.collectAsState()
    val mode = language.uppercase()
    val isHi = mode == "HINDI" || mode == "HI"
    val userProfile by viewModel.userProfile.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceNarrationEnabled.collectAsState()

    val view = LocalView.current
    val context = LocalContext.current
    var isScreenSharingActive by remember { mutableStateOf(false) }

    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            isScreenSharingActive = true
        } else {
            val intent = try {
                Intent(Settings.ACTION_CAST_SETTINGS)
            } catch (_: Exception) {
                Intent(Settings.ACTION_SETTINGS)
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    val question = state.question
    var showQuitConfirmation by remember { mutableStateOf(false) }
    var showScratchpad by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("quiz_screen_container")
    ) {
        val isLargeScreen = maxWidth > 800.dp || isScreenSharingActive
        val widthToHeightRatio = if (maxHeight > 0.dp) maxWidth / maxHeight else 1f
        val isLargeClassroom = isLargeScreen && (widthToHeightRatio > 1.25f || maxWidth > 950.dp)
        val layoutVerification = remember(maxWidth, isScreenSharingActive) {
            com.example.ui.utils.SmartClassLayoutVerifier.verifyLayout(maxWidth, isScreenSharingActive)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            if (isLargeScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoldPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("smart_class_verification_banner")
                ) {
                    Text(
                        text = "✓ Smart Class Verified • Min Font: ${layoutVerification.minFontSizeSp}sp • Contrast: ${layoutVerification.contrastRatio}:1 (Optimal for Distance Viewing)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            // ==========================================
            // TOP HUD (Header)
            // ==========================================
            QuizHeaderHud(
                currentQNumber = state.currentQNumber,
                points = question.points,
                isCheckpoint = question.isCheckpoint,
                checkpointTitle = question.checkpointTitle,
                timeRemaining = state.timeRemainingSeconds,
                timerMode = state.timerMode,
                elapsedThinkingSeconds = state.elapsedThinkingSeconds,
                hasBonusTime = state.hasBonusTime,
                accumulatedBonusSeconds = state.accumulatedBonusSeconds,
                bonusLostNotice = state.bonusLostNotice,
                isVoiceEnabled = isVoiceEnabled,
                userName = userProfile.name,
                onToggleVoice = { viewModel.toggleVoiceNarration() },
                onOpenScratchpad = { showScratchpad = true },
                onOpenLadder = { viewModel.toggleLadderDrawer(true) },
                onQuitClick = { showQuitConfirmation = true },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onScreenShareClick = {
                    try {
                        val projectionIntent = mediaProjectionManager?.createScreenCaptureIntent()
                        if (projectionIntent != null) {
                            screenCaptureLauncher.launch(projectionIntent)
                        } else {
                            isScreenSharingActive = !isScreenSharingActive
                            val intent = try {
                                Intent(Settings.ACTION_CAST_SETTINGS)
                            } catch (_: Exception) {
                                Intent(Settings.ACTION_SETTINGS)
                            }
                            context.startActivity(intent)
                        }
                    } catch (_: Exception) {
                        isScreenSharingActive = !isScreenSharingActive
                    }
                },
                isHi = isHi
            )

            if (isScreenSharingActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoldPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isHi) "🖥️ स्क्रीन शेयर सक्रिय (Smart Class Mode)" else "🖥️ Screen Sharing Active (Smart Class Mode)",
                            color = GoldGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { isScreenSharingActive = false }) {
                            Text(text = if (isHi) "छिपाएं" else "Dismiss", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            // ==========================================
            // 17 LOGIC LEVELS DIFFICULTY LEVEL INDICATOR
            // ==========================================
            DifficultyLevelIndicator(
                currentLevel = state.currentQNumber,
                totalLevels = 17,
                isHindi = isHi,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ==========================================
            // SCROLLABLE QUESTION & CLUES BODY
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Category, Difficulty & Junior Mode Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(NavyCardElevated, RoundedCornerShape(6.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = question.category,
                                color = GoldGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (userProfile.isStudentMode || userProfile.preparationDomain.contains("Student", true)) {
                            Box(
                                modifier = Modifier
                                    .background(InfoCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .border(1.dp, InfoCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = InfoCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = userProfile.studentClass.ifBlank { "Junior" },
                                        color = InfoCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tierMeta = com.example.data.api.GeminiApiClient.getTierDetails(state.currentQNumber)
                        val difficultyColor = when {
                            tierMeta.isCheckpoint -> CheckpointGold
                            state.currentQNumber <= 2 -> InfoCyan
                            state.currentQNumber <= 5 -> SuccessGreen
                            state.currentQNumber <= 9 -> GoldPrimary
                            state.currentQNumber <= 12 -> CheckpointSilver
                            state.currentQNumber <= 15 -> PurpleAccent
                            else -> AlertRed
                        }

                        Box(
                            modifier = Modifier
                                .background(difficultyColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, difficultyColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("difficulty_badge_${state.currentQNumber}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (tierMeta.isCheckpoint) Icons.Default.EmojiEvents else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = difficultyColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Level ${state.currentQNumber}/17 • ${tierMeta.difficultyTitle}",
                                    color = difficultyColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Transparent Camera & Audio Monitoring UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Preview
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(60.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, NavyBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isMonitoringActive) {
                            LiveCameraPreview(modifier = Modifier.fillMaxSize())
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                                    .background(AlertRed, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Videocam, contentDescription = "Camera", tint = TextMuted, modifier = Modifier.size(16.dp))
                                Text("USER", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Status Indicator
                    val statusColor = when {
                        state.disqualificationNotice != null -> AlertRed
                        state.identityWarningCount > 0 -> InfoCyan // Blue warning
                        else -> SuccessGreen
                    }
                    val statusText = when {
                        state.disqualificationNotice != null -> "Disqualified"
                        state.identityWarningCount > 0 -> "Warning ${state.identityWarningCount}/3"
                        else -> "OK"
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Status: $statusText", color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Audio Level Visualization
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Audio Level", color = TextSecondary, fontSize = 9.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(24.dp).padding(vertical = 2.dp)
                        ) {
                            state.audioWaveform.forEach { fraction ->
                                val barColor = if (fraction > 0.8f) AlertRed else if (fraction > 0.4f) InfoCyan else SuccessGreen
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight(fraction)
                                        .background(barColor, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                )
                            }
                        }
                        Text(state.audioState, color = if(state.audioState == "NORMAL") SuccessGreen else InfoCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (state.disqualificationNotice != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF5A1E1E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = state.disqualificationNotice ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (isLargeClassroom) {
                    // Dedicated LargeClassroom Layout State: High-visibility side-by-side split podium format for wide aspect ratio displays
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BilingualQuestionCard(
                            questionHindi = question.questionHindi,
                            questionEnglish = question.questionEnglish,
                            preferredLanguage = language,
                            isLargeScreen = true,
                            modifier = Modifier.weight(0.48f)
                        )

                        AnimatedVisibility(
                            visible = state.isOptionsVisible,
                            enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                            modifier = Modifier.weight(0.52f)
                        ) {
                            BilingualOptionsGrid2x2(
                                optionsHindi = question.optionsHindi,
                                optionsEnglish = question.optionsEnglish,
                                selectedOptionIndex = state.selectedOptionIndex,
                                lockedOptionIndex = state.lockedOptionIndex,
                                discardedIndices = state.discardedOptionIndices,
                                isAnswerRevealed = state.isAnswerRevealed,
                                correctAnswerIndex = question.correctAnswerIndex,
                                isLockedIn = state.isLockedIn,
                                preferredLanguage = language,
                                isEnabled = state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn,
                                isLargeScreen = true,
                                onOptionSelected = { index ->
                                    if (state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn && !state.discardedOptionIndices.contains(index)) {
                                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                        viewModel.selectOption(index)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Primary Bilingual Question Card (Line-by-Line Paired Translation)
                    BilingualQuestionCard(
                        questionHindi = question.questionHindi,
                        questionEnglish = question.questionEnglish,
                        preferredLanguage = language,
                        isLargeScreen = isLargeScreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Answer Options — Directly below the Question Card
                    AnimatedVisibility(
                        visible = state.isOptionsVisible,
                        enter = fadeIn(tween(400)) + expandVertically(tween(400))
                    ) {
                        BilingualOptionsGrid2x2(
                            optionsHindi = question.optionsHindi,
                            optionsEnglish = question.optionsEnglish,
                            selectedOptionIndex = state.selectedOptionIndex,
                            lockedOptionIndex = state.lockedOptionIndex,
                            discardedIndices = state.discardedOptionIndices,
                            isAnswerRevealed = state.isAnswerRevealed,
                            correctAnswerIndex = question.correctAnswerIndex,
                            isLockedIn = state.isLockedIn,
                            preferredLanguage = language,
                            isEnabled = state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn,
                            isLargeScreen = isLargeScreen,
                            onOptionSelected = { index ->
                                if (state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn && !state.discardedOptionIndices.contains(index)) {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.selectOption(index)
                                }
                            }
                        )
                    }
                }
            }

            // ==========================================
            // BOTTOM CONTROLS & LIFELINES
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDeepest)
                    .border(1.dp, NavyBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(8.dp)
            ) {
                // Lifelines Bar
                LifelineControls(
                    lifelineState = state.lifelineState,
                    onUse5050 = { viewModel.use5050() },
                    onUseAskExpert = { viewModel.useAskExpert() },
                    onUseFlipQuestion = { viewModel.useFlipQuestion() },
                    onUsePowerPaplu = { target -> viewModel.usePowerPaplu(target) },
                    onUseHint = { viewModel.useHintLifeline() },
                    isEnabled = state.phase == QuestionPhase.ANSWER_ACTIVE && !state.isLockedIn
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Lock-In Button ("ताला लगाएँ")
                val canLockIn = state.selectedOptionIndex != null && !state.isLockedIn && state.phase == QuestionPhase.ANSWER_ACTIVE
                val isLocked = state.isLockedIn

                Button(
                    onClick = { viewModel.lockInAnswer() },
                    enabled = canLockIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 12.dp)
                        .testTag("lock_in_answer_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocked) GoldDark else GoldPrimary,
                        disabledContainerColor = if (isLocked) GoldDark.copy(alpha = 0.3f) else NavyCard
                    )
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Outlined.Lock,
                        contentDescription = "Lock In",
                        tint = if (canLockIn) NavyDeepest else if (isLocked) GoldGlow else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isLocked -> if (isHi) "🔒 उत्तर लॉक हो चुका है (LOCKED)" else "🔒 ANSWER LOCKED"
                            canLockIn -> if (isHi) "ताला लगाएँ (Lock Answer)" else "LOCK ANSWER"
                            state.phase == QuestionPhase.QUESTION_READING -> if (isHi) "प्रश्न ध्यान से पढ़ें (Read Carefully)" else "READ QUESTION FIRST"
                            else -> if (isHi) "कृपया विकल्प चुनें (Select Option)" else "SELECT AN OPTION"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = if (canLockIn) NavyDeepest else if (isLocked) GoldGlow else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // ==========================================
        // OVERLAY DIALOGS
        // ==========================================

        // 1. Ladder Drawer Sheet
        if (state.isLadderDrawerOpen) {
            LadderDrawer(
                currentQuestionNumber = state.currentQNumber,
                onClose = { viewModel.toggleLadderDrawer(false) }
            )
        }

        // 2. Expert Advice Dialog
        if (state.expertDialogContent != null || state.isExpertLoading) {
            ExpertAdviceDialog(
                advice = state.expertDialogContent ?: "",
                isLoading = state.isExpertLoading,
                language = language,
                onDismiss = { viewModel.dismissExpertDialog() }
            )
        }

        // 3. 50-50 Proof Dialog
        if (state.fiftyFiftyProofDialog != null) {
            FiftyFiftyProofDialog(
                proof = state.fiftyFiftyProofDialog,
                onDismiss = { viewModel.dismiss5050Proof() }
            )
        }

        // 4. Checkpoint Milestone Fanfare Overlay
        if (state.showCheckpointFanfare != null) {
            PadaavCelebrationOverlay(
                currentQNumber = state.currentQNumber,
                points = question.points,
                checkpointTitle = state.showCheckpointFanfare ?: "सुरक्षित पड़ाव",
                isHindi = isHi,
                onDismiss = { viewModel.dismissCheckpointFanfare() }
            )
        }

        // 5. Quit / Exit Session Confirmation Dialog
        if (showQuitConfirmation) {
            AlertDialog(
                onDismissRequest = { showQuitConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Exit Warning",
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = if (isHi) "क्या आप सत्र छोड़ना चाहते हैं? (Exit Quiz Session)" else "Are you sure you want to exit?",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isHi) 
                                "⚠️ चेतावनी: यदि आप अभी इस क्विज सत्र को छोड़ते हैं, तो आपकी वर्तमान प्रगति और जीती गई राशि (${if (state.currentQNumber > 1) state.currentPointsWon else 0} अंक) समाप्त हो सकती है। क्या आपको पूरा यकीन है?"
                            else 
                                "⚠️ Warning: You are about to exit your active quiz session. Exiting now may result in accidental progress loss. Are you sure you want to leave?",
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showQuitConfirmation = false
                            viewModel.quitGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        modifier = Modifier.testTag("confirm_quit_button")
                    ) {
                        Text(if (isHi) "हाँ, बाहर निकलें (Yes, Exit)" else "Yes, Exit Session", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuitConfirmation = false }) {
                        Text(if (isHi) "नहीं, खेलते रहें (Stay)" else "Cancel", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = NavyCardElevated,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 6. Scratchpad Canvas Dialog
        if (showScratchpad) {
            ScratchpadDialog(
                onDismiss = { showScratchpad = false },
                isHi = isHi
            )
        }

        // 7. Hint Dialog (Auto-dismisses in 6s or on manual dismiss)
        if (state.isHintLifelineActive && state.activeHintContent != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissHint() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Logical Hint",
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = if (isHi) "💡 तार्किक संकेत (Hint)" else "💡 Logical Deductive Hint",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldGlow
                        )
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = state.activeHintContent,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        )
                        if (question.diagramType != "none" && question.diagramType.isNotBlank()) {
                            DiagramCanvas(
                                diagramType = question.diagramType,
                                diagramData = question.diagramData,
                                audioPatternType = question.audioPatternType,
                                onPlayAudio = { viewModel.playQuestionAudio() }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissHint() }) {
                        Text(if (isHi) "समझ गया (OK)" else "Got it", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = NavyCardElevated,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun QuizHeaderHud(
    currentQNumber: Int,
    points: Long,
    isCheckpoint: Boolean,
    checkpointTitle: String?,
    timeRemaining: Int?,
    timerMode: TimerMode = TimerMode.TIMED,
    elapsedThinkingSeconds: Int = 0,
    hasBonusTime: Boolean = false,
    accumulatedBonusSeconds: Int = 0,
    bonusLostNotice: Boolean = false,
    isVoiceEnabled: Boolean,
    userName: String = "Challenger",
    onToggleVoice: () -> Unit,
    onOpenScratchpad: () -> Unit,
    onOpenLadder: () -> Unit,
    onQuitClick: () -> Unit,
    onToggleLanguage: () -> Unit,
    onScreenShareClick: () -> Unit,
    isHi: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quiz_header_hud"),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDeepest),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.5f), NavyBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Ladder & Scratchpad & Voice
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenLadder,
                    modifier = Modifier.testTag("open_ladder_drawer_button").size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Ladder",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenScratchpad,
                    modifier = Modifier.testTag("open_scratchpad_button").size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Scratchpad",
                        tint = InfoCyan,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onScreenShareClick,
                    modifier = Modifier.testTag("screen_share_button").size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = if (isHi) "स्क्रीन शेयर" else "Screen Share",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier.testTag("toggle_voice_narration_button").size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isVoiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Voice",
                        tint = if (isVoiceEnabled) GoldGlow else TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // User Name & Question Prize Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldGlow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Q$currentQNumber / 17 • $points POINTS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )

                if (isCheckpoint && checkpointTitle != null) {
                    Text(
                        text = "🔒 $checkpointTitle",
                        fontSize = 10.sp,
                        color = CheckpointGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Timer & Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (timerMode == TimerMode.UNLIMITED_ELAPSED) {
                    val minutes = elapsedThinkingSeconds / 60
                    val seconds = elapsedThinkingSeconds % 60
                    val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

                    Box(
                        modifier = Modifier
                            .background(NavyCard, RoundedCornerShape(10.dp))
                            .border(1.2.dp, PurpleAccent.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formattedTime,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PurpleAccent
                            )
                        }
                    }
                } else if (timeRemaining != null) {
                    val minutes = timeRemaining / 60
                    val seconds = timeRemaining % 60
                    val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

                    // 4-Tier Visual Urgency Classification
                    val isFinalSeconds = timeRemaining <= 5
                    val isCritical = timeRemaining in 6..10
                    val isAttention = timeRemaining in 11..30

                    val containerBg = when {
                        isFinalSeconds -> AlertRed.copy(alpha = 0.38f)
                        isCritical -> AlertRed.copy(alpha = 0.22f)
                        isAttention -> GoldPrimary.copy(alpha = 0.18f)
                        else -> NavyCard
                    }

                    val borderColor = when {
                        isFinalSeconds -> AlertRed
                        isCritical -> AlertRed.copy(alpha = 0.85f)
                        isAttention -> GoldPrimary.copy(alpha = 0.8f)
                        else -> InfoCyan.copy(alpha = 0.6f)
                    }

                    val textColor = when {
                        isFinalSeconds || isCritical -> AlertRed
                        isAttention -> GoldGlow
                        else -> InfoCyan
                    }

                    val borderWidth = if (isFinalSeconds) 2.dp else 1.2.dp

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (hasBonusTime) {
                            Box(
                                modifier = Modifier
                                    .background(GoldPrimary.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 5.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "+${if (accumulatedBonusSeconds > 0) accumulatedBonusSeconds else 5}s Bonus",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldGlow
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(containerBg, RoundedCornerShape(10.dp))
                                .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = timeRemaining.toString(),
                                fontWeight = if (isFinalSeconds || isCritical) FontWeight.Black else FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textColor
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(NavyCard, RoundedCornerShape(8.dp))
                            .border(1.dp, InfoCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isHi) "🏆 असीमित" else "🏆 Untimed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InfoCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Quit Button
                IconButton(
                    onClick = onQuitClick,
                    modifier = Modifier.testTag("quit_game_button").size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Quit",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuizOptionCard(
    optionLetter: String,
    optionText: String,
    isSelected: Boolean,
    isDiscarded: Boolean,
    isAnswerRevealed: Boolean,
    isCorrect: Boolean,
    isLockedIn: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val targetBgColor = when {
        isDiscarded -> NavyDeepest.copy(alpha = 0.4f)
        isAnswerRevealed && isCorrect -> SuccessGreen.copy(alpha = 0.35f)
        isAnswerRevealed && isSelected && !isCorrect -> AlertRed.copy(alpha = 0.35f)
        isSelected && isLockedIn -> GoldDark.copy(alpha = 0.4f)
        isSelected -> GoldPrimary.copy(alpha = 0.2f)
        else -> NavyCard
    }

    val targetBorderColor = when {
        isDiscarded -> Color.Transparent
        isAnswerRevealed && isCorrect -> SuccessGreen
        isAnswerRevealed && isSelected && !isCorrect -> AlertRed
        isSelected -> GoldPrimary
        else -> NavyBorder
    }

    val animatedBg by animateColorAsState(targetValue = targetBgColor, animationSpec = tween(300), label = "bg")
    val animatedBorder by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(300), label = "border")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(enabled = !isDiscarded && !isLockedIn, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(animatedBorder, animatedBorder.copy(alpha = 0.5f))))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        when {
                            isDiscarded -> NavyDeepest
                            isAnswerRevealed && isCorrect -> SuccessGreen
                            isAnswerRevealed && isSelected && !isCorrect -> AlertRed
                            isSelected -> GoldPrimary
                            else -> NavyCardElevated
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLetter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = when {
                        isDiscarded -> TextMuted
                        isSelected || (isAnswerRevealed && isCorrect) -> NavyDeepest
                        else -> TextPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isDiscarded) TextMuted else TextPrimary,
                    fontWeight = if (isSelected || (isAnswerRevealed && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                    textDecoration = if (isDiscarded) TextDecoration.LineThrough else TextDecoration.None
                )
            )
        }
    }
}

@Composable
fun LiveCameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val hasCameraHardware = com.example.util.DeviceCapabilities.hasCamera(context)
    val hasPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
    if (!hasCameraHardware || !hasPermission) {
        Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.VideocamOff, contentDescription = "Camera Unavailable", tint = TextMuted, modifier = Modifier.size(16.dp))
                Text(if (!hasCameraHardware) "NO CAM" else "NO PERM", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch(exc: Exception) {
                    android.util.Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}
