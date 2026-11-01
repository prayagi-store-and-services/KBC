package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameHistoryEntity
import com.example.ui.theme.*
import com.example.ui.utils.adaptiveContentWidth
import com.example.ui.utils.adaptiveFontScaleFor
import androidx.compose.ui.platform.LocalConfiguration
import com.example.ui.viewmodel.QuizViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: QuizViewModel,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.gameHistory.collectAsState()

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val todayStr = dateFormat.format(Date())

    val availableDates = remember(historyList) {
        val dates = historyList.map { dateFormat.format(Date(it.timestamp)) }.distinct()
        if (dates.isNotEmpty()) dates else listOf(todayStr)
    }

    var selectedDate by remember { mutableStateOf(todayStr) }
    if (selectedDate !in availableDates && availableDates.isNotEmpty()) {
        selectedDate = availableDates.first()
    }

    val filteredList = remember(historyList, selectedDate) {
        historyList.filter { dateFormat.format(Date(it.timestamp)) == selectedDate }
    }

    val totalGames = filteredList.size
    val totalCorrect = filteredList.sumOf { it.correctAnswersCount.toLong() }
    val peakPrize = filteredList.maxOfOrNull { it.finalPrize } ?: 0L

    val fontScale = adaptiveFontScaleFor(LocalConfiguration.current.screenWidthDp)
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen_container"),
        color = NavyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .adaptiveContentWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateToHome() },
                    modifier = Modifier.testTag("history_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "तर्क इतिहास / Match History",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * fontScale
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Selector Scrollable Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableDates.forEach { date ->
                    val isSelected = date == selectedDate
                    Surface(
                        modifier = Modifier
                            .clickable { selectedDate = date }
                            .testTag("date_chip_$date"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) GoldPrimary else NavyCardElevated,
                        border = BorderStroke(1.dp, if (isSelected) GoldGlow else NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (isSelected) NavyDeepest else GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (date == todayStr) "Today ($date)" else date,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NavyDeepest else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Stats Card for Selected Date
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(GoldPrimary.copy(alpha = 0.5f), InfoCyan.copy(alpha = 0.3f))
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$totalGames", fontWeight = FontWeight.Black, fontSize = 20.sp, color = GoldGlow)
                        Text(text = "कुल खेल (Matches)", fontSize = 11.sp, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$totalCorrect", fontWeight = FontWeight.Black, fontSize = 20.sp, color = SuccessGreen)
                        Text(text = "सही (Correct)", fontSize = 11.sp, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (peakPrize >= 10000000) "${peakPrize / 1000}k POINTS" else "$peakPrize POINTS",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = GoldPrimary
                        )
                        Text(text = "सर्वोच्च अंक (Peak)", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "इस तिथि पर कोई खेल रिकॉर्ड नहीं मिला।",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { item ->
                        val timeFormatted = try {
                            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            timeFormat.format(Date(item.timestamp))
                        } catch (_: Exception) {
                            ""
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tier Q${item.highestQuestionReached} • ${item.outcomeStatus.replace("_", " ")}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = GoldGlow,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$timeFormatted • Correct: ${item.correctAnswersCount}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Text(
                                    text = if (item.finalPrize >= 10000000) "${item.finalPrize / 1000}k POINTS" else "${item.finalPrize} POINTS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
