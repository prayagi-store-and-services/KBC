package com.example.stats

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val KBC_WEB_URL = "https://prayagi-store-and-services.github.io/KBC/play.html"

/** Profile card: opens KBC Web in the phone's browser (for school PCs and phones without the app). Nothing is sent by this button. */
@Composable
fun WebPlayCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("KBC Web", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Play KBC in a browser on a school PC or any phone, with no install. It has fewer features than the app: no camera or microphone checks, and no Audience poll or Phone a friend.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(KBC_WEB_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    message = null
                } catch (e: ActivityNotFoundException) {
                    message = "Unavailable: no web browser was found on this phone. Address: $KBC_WEB_URL"
                }
            }) { Text("Open KBC Web") }
            message?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}
