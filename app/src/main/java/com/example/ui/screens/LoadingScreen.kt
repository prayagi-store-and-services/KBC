package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeepest
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.utils.adaptiveContentWidth
import com.example.ui.theme.TextSecondary

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("loading_screen_container"),
        color = NavyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .adaptiveContentWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(NavyDeepest)
                    .border(3.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Thinking",
                    tint = GoldGlow,
                    modifier = Modifier
                        .size(54.dp)
                        .rotate(rotation)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "तर्क इंजन सक्रिय हो रहा है...",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = GoldGlow,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "सुराग सत्यापन • निष्पक्षता जांच • अनूठापन परीक्षण",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    PipelineStage("1. Generator AI (तार्किक प्रश्न सृजन)", SuccessGreen)
                    PipelineStage("2. Critic & Adversarial Logic Validator", InfoCyan)
                    PipelineStage("3. Clue Adequacy & Single-Answer Proof", GoldPrimary)
                    PipelineStage("4. Uniqueness & Anti-Repeat Registry", PurpleAccent)
                }
            }
        }
    }
}

@Composable
fun PipelineStage(title: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
