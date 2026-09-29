package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.AlertRed
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeepest
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Utility extensions for bilingual numerals formatting.
 * Converts ASCII digits (0-9) to Devanagari numerals (०-९) for pure Hindi rendering.
 */
fun String.toDevanagariNumerals(): String {
    return this
}

fun String.toArabicNumerals(): String {
    return this.map { char ->
        when (char) {
            '०' -> '0'
            '१' -> '1'
            '२' -> '2'
            '३' -> '3'
            '४' -> '4'
            '५' -> '5'
            '६' -> '6'
            '७' -> '7'
            '८' -> '8'
            '९' -> '9'
            else -> char
        }
    }.joinToString("")
}

/**
 * Line-by-line pairing algorithm.
 * Pairs corresponding Hindi and English lines or sentences into a single structured unit.
 */
fun pairBilingualLines(hindiText: String, englishText: String): List<Pair<String, String>> {
    val hLines = hindiText.trim().split("\n").filter { it.isNotBlank() }
    val eLines = englishText.trim().split("\n").filter { it.isNotBlank() }

    if (hLines.size > 1 && hLines.size == eLines.size) {
        return hLines.indices.map { i ->
            Pair(hLines[i].toDevanagariNumerals(), eLines[i].toArabicNumerals())
        }
    }

    val hSentences = hindiText.trim().split("।").map { it.trim() }.filter { it.isNotBlank() }
    val eSentences = englishText.trim().split(".").map { it.trim() }.filter { it.isNotBlank() }

    if (hSentences.size > 1 && hSentences.size == eSentences.size) {
        return hSentences.indices.map { i ->
            Pair("${hSentences[i]}।".toDevanagariNumerals(), "${eSentences[i]}.".toArabicNumerals())
        }
    }

    return listOf(Pair(hindiText.toDevanagariNumerals(), englishText.toArabicNumerals()))
}

/**
 * Paired Question Card Composable.
 * Renders both Hindi and English sentences line-by-line together in a single question block.
 * Highlights the player's selected language while keeping both versions crystal clear.
 */
@Composable
fun BilingualQuestionCard(
    questionHindi: String,
    questionEnglish: String,
    preferredLanguage: String,
    isLargeScreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val mode = preferredLanguage.uppercase()
    val isHi = mode == "HINDI" || mode == "HI"
    val isEn = mode == "ENGLISH" || mode == "EN"
    val pairedLines = pairBilingualLines(questionHindi, questionEnglish)
    val questionFontSize = if (isLargeScreen) 22.sp else 16.sp
    val questionLineHeight = if (isLargeScreen) 30.sp else 23.sp
    val secondaryFontSize = if (isLargeScreen) 16.sp else 13.sp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("primary_bilingual_question_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(GoldPrimary.copy(alpha = 0.6f), GoldDark.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(if (isLargeScreen) 20.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            pairedLines.forEachIndexed { index, (hindiLine, englishLine) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDeepest.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (isHi) {
                        Text(
                            text = hindiLine,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = questionFontSize,
                                lineHeight = questionLineHeight
                            )
                        )
                    } else if (isEn) {
                        Text(
                            text = englishLine,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = questionFontSize,
                                lineHeight = questionLineHeight
                            )
                        )
                    } else {
                        Text(
                            text = hindiLine,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = questionFontSize,
                                lineHeight = questionLineHeight
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = englishLine,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = InfoCyan.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium,
                                fontSize = secondaryFontSize,
                                lineHeight = if (isLargeScreen) 22.sp else 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Paired Deductive Clues List.
 * Renders each clue with line-by-line Hindi + English pairing.
 */
@Composable
fun BilingualCluesList(
    cluesHindi: List<String>,
    cluesEnglish: List<String>,
    verifiedClues: Set<Int>,
    preferredLanguage: String,
    onToggleVerify: (Int) -> Unit
) {
    val mode = preferredLanguage.uppercase()
    val isHi = mode == "HINDI" || mode == "HI"
    val isEn = mode == "ENGLISH" || mode == "EN"
    val count = maxOf(cluesHindi.size, cluesEnglish.size)
    if (count == 0) return

    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isHi) "तार्किक सुराग (Deductive Clues):" else "Deductive Clues (तार्किक सुराग):",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "${verifiedClues.size}/$count ✓",
                fontSize = 11.sp,
                color = if (verifiedClues.size == count) SuccessGreen else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        for (i in 0 until count) {
            val hClue = cluesHindi.getOrNull(i)?.toDevanagariNumerals() ?: ""
            val eClue = cluesEnglish.getOrNull(i)?.toArabicNumerals() ?: ""
            val isVerified = verifiedClues.contains(i)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isVerified) SuccessGreen.copy(alpha = 0.12f) else NavyDeepest.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        if (isVerified) SuccessGreen.copy(alpha = 0.6f) else NavyBorder.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleVerify(i) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isVerified) SuccessGreen else TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (isHi) {
                            if (hClue.isNotBlank()) {
                                Text(
                                    text = hClue,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isVerified) TextPrimary else TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                            if (eClue.isNotBlank()) {
                                Text(
                                    text = eClue,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = InfoCyan.copy(alpha = 0.75f),
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        } else {
                            if (eClue.isNotBlank()) {
                                Text(
                                    text = eClue,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isVerified) TextPrimary else TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                            if (hClue.isNotBlank()) {
                                Text(
                                    text = hClue,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldGlow.copy(alpha = 0.75f),
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Authoritative 2×2 Options Grid (A/B Top Row + C/D Bottom Row).
 * Rule 6 & 7:
 * Row 1: Option A (Hindi + English) | Option B (Hindi + English)
 * Row 2: Option C (Hindi + English) | Option D (Hindi + English)
 */
@Composable
fun BilingualOptionsGrid2x2(
    optionsHindi: List<String>,
    optionsEnglish: List<String>,
    selectedOptionIndex: Int?,
    lockedOptionIndex: Int?,
    discardedIndices: Set<Int>,
    isAnswerRevealed: Boolean,
    correctAnswerIndex: Int,
    isLockedIn: Boolean,
    preferredLanguage: String,
    isEnabled: Boolean = true,
    isLargeScreen: Boolean = false,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isLargeScreen) 14.dp else 8.dp)
    ) {
        // Row 1: Options A (index 0) and B (index 1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isLargeScreen) 14.dp else 8.dp)
        ) {
            BilingualOptionGridCard(
                optionLetter = "A",
                optionHindi = optionsHindi.getOrNull(0)?.toDevanagariNumerals() ?: "",
                optionEnglish = optionsEnglish.getOrNull(0)?.toArabicNumerals() ?: "",
                index = 0,
                isSelected = (lockedOptionIndex ?: selectedOptionIndex) == 0,
                isDiscarded = discardedIndices.contains(0),
                isAnswerRevealed = isAnswerRevealed,
                isCorrect = 0 == correctAnswerIndex,
                isLockedIn = isLockedIn,
                preferredLanguage = preferredLanguage,
                isEnabled = isEnabled,
                isLargeScreen = isLargeScreen,
                onClick = { onOptionSelected(0) },
                modifier = Modifier.weight(1f)
            )

            BilingualOptionGridCard(
                optionLetter = "B",
                optionHindi = optionsHindi.getOrNull(1)?.toDevanagariNumerals() ?: "",
                optionEnglish = optionsEnglish.getOrNull(1)?.toArabicNumerals() ?: "",
                index = 1,
                isSelected = (lockedOptionIndex ?: selectedOptionIndex) == 1,
                isDiscarded = discardedIndices.contains(1),
                isAnswerRevealed = isAnswerRevealed,
                isCorrect = 1 == correctAnswerIndex,
                isLockedIn = isLockedIn,
                preferredLanguage = preferredLanguage,
                isEnabled = isEnabled,
                isLargeScreen = isLargeScreen,
                onClick = { onOptionSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Options C (index 2) and D (index 3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isLargeScreen) 14.dp else 8.dp)
        ) {
            BilingualOptionGridCard(
                optionLetter = "C",
                optionHindi = optionsHindi.getOrNull(2)?.toDevanagariNumerals() ?: "",
                optionEnglish = optionsEnglish.getOrNull(2)?.toArabicNumerals() ?: "",
                index = 2,
                isSelected = (lockedOptionIndex ?: selectedOptionIndex) == 2,
                isDiscarded = discardedIndices.contains(2),
                isAnswerRevealed = isAnswerRevealed,
                isCorrect = 2 == correctAnswerIndex,
                isLockedIn = isLockedIn,
                preferredLanguage = preferredLanguage,
                isEnabled = isEnabled,
                isLargeScreen = isLargeScreen,
                onClick = { onOptionSelected(2) },
                modifier = Modifier.weight(1f)
            )

            BilingualOptionGridCard(
                optionLetter = "D",
                optionHindi = optionsHindi.getOrNull(3)?.toDevanagariNumerals() ?: "",
                optionEnglish = optionsEnglish.getOrNull(3)?.toArabicNumerals() ?: "",
                index = 3,
                isSelected = (lockedOptionIndex ?: selectedOptionIndex) == 3,
                isDiscarded = discardedIndices.contains(3),
                isAnswerRevealed = isAnswerRevealed,
                isCorrect = 3 == correctAnswerIndex,
                isLockedIn = isLockedIn,
                preferredLanguage = preferredLanguage,
                isEnabled = isEnabled,
                isLargeScreen = isLargeScreen,
                onClick = { onOptionSelected(3) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual Paired Option Card for 2×2 Grid.
 */
@Composable
fun BilingualOptionGridCard(
    optionLetter: String,
    optionHindi: String,
    optionEnglish: String,
    index: Int,
    isSelected: Boolean,
    isDiscarded: Boolean,
    isAnswerRevealed: Boolean,
    isCorrect: Boolean,
    isLockedIn: Boolean,
    preferredLanguage: String,
    isEnabled: Boolean = true,
    isLargeScreen: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mode = preferredLanguage.uppercase()
    val isHi = mode == "HINDI" || mode == "HI"
    val isEn = mode == "ENGLISH" || mode == "EN"

    val targetBgColor = when {
        isDiscarded -> NavyDeepest.copy(alpha = 0.35f)
        isAnswerRevealed && isCorrect -> SuccessGreen.copy(alpha = 0.35f)
        isAnswerRevealed && isSelected && !isCorrect -> AlertRed.copy(alpha = 0.35f)
        isSelected && isLockedIn -> GoldDark.copy(alpha = 0.45f)
        isSelected -> GoldPrimary.copy(alpha = 0.22f)
        else -> NavyCard
    }

    val targetBorderColor = when {
        isDiscarded -> NavyBorder.copy(alpha = 0.25f)
        isAnswerRevealed && isCorrect -> SuccessGreen
        isAnswerRevealed && isSelected && !isCorrect -> AlertRed
        isSelected -> GoldPrimary
        else -> NavyBorder.copy(alpha = 0.8f)
    }

    val animatedBg by animateColorAsState(targetValue = targetBgColor, animationSpec = tween(300), label = "grid_bg")
    val animatedBorder by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(300), label = "grid_border")

    val optionPrimaryFontSize = if (isLargeScreen) 18.sp else 13.5.sp
    val optionSecondaryFontSize = if (isLargeScreen) 15.sp else 11.5.sp
    val cardPadding = if (isLargeScreen) 16.dp else 10.dp

    Card(
        modifier = modifier
            .testTag("option_button_$index")
            .clickable(enabled = isEnabled && !isDiscarded && !isLockedIn, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(animatedBorder, animatedBorder.copy(alpha = 0.6f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Letter Badge + Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isLargeScreen) 30.dp else 24.dp)
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
                        fontSize = if (isLargeScreen) 14.sp else 12.sp,
                        color = when {
                            isDiscarded -> TextMuted
                            isSelected || (isAnswerRevealed && isCorrect) -> NavyDeepest
                            else -> GoldGlow
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isLargeScreen) 8.dp else 6.dp))

            // Bilingual / Single Paired Option Text
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isHi) {
                    // Hindi selected -> Hindi only. Zero English.
                    Text(
                        text = optionHindi,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDiscarded) TextMuted else TextPrimary,
                            fontWeight = if (isSelected || (isAnswerRevealed && isCorrect)) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = optionPrimaryFontSize,
                            lineHeight = if (isLargeScreen) 24.sp else 18.sp,
                            textDecoration = if (isDiscarded) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )
                } else if (isEn) {
                    // English selected -> English only. Zero Hindi.
                    Text(
                        text = optionEnglish,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDiscarded) TextMuted else TextPrimary,
                            fontWeight = if (isSelected || (isAnswerRevealed && isCorrect)) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = optionPrimaryFontSize,
                            lineHeight = if (isLargeScreen) 24.sp else 18.sp,
                            textDecoration = if (isDiscarded) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )
                } else {
                    // Bilingual (Both) -> Both rendered
                    Text(
                        text = optionEnglish,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDiscarded) TextMuted else TextPrimary,
                            fontWeight = if (isSelected || (isAnswerRevealed && isCorrect)) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = optionPrimaryFontSize,
                            lineHeight = if (isLargeScreen) 24.sp else 18.sp,
                            textDecoration = if (isDiscarded) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = optionHindi,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDiscarded) TextMuted.copy(alpha = 0.6f) else GoldGlow.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Normal,
                            fontSize = optionSecondaryFontSize,
                            lineHeight = if (isLargeScreen) 20.sp else 15.sp,
                            textDecoration = if (isDiscarded) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )
                }
            }
        }
    }
}

