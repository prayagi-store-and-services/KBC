package com.example

import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItProfessionalSectionScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.ProfileInstallingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuestionBankPreparationScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SummaryScreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.TarkShastraTheme
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

open class MainActivity : ComponentActivity() {
    private val quizViewModel: QuizViewModel by viewModels()
    private var userLeftViaHome = false

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        com.example.update.UpdateAlert.handle(this, intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try { Brand.applyLauncherName(this) } catch (_: Throwable) { }
        super.onCreate(savedInstanceState)
        if (com.example.geo.GeoGuard.isBlocked(this)) {
            val pad = (24 * resources.displayMetrics.density).toInt()
            val box = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setPadding(pad, pad, pad, pad)
            }
            box.addView(android.widget.TextView(this).apply {
                text = "This app is not available in your region."
                textSize = 20f
                gravity = android.view.Gravity.CENTER
            })
            box.addView(android.widget.Button(this).apply {
                text = "Close"
                setOnClickListener { finishAffinity() }
            })
            setContentView(box)
            return
        }
        // Remove any installer file left from an in-app update (runs in the background).
        Thread { com.example.update.AppUpdater.cleanLeftovers(applicationContext) }.start()
        com.example.update.UpdateAlert.start(this)
        // Anonymous daily usage count (+1 on a public counter, nothing else). The user can turn it off in Settings.
        // Automatic crash reports: a crash saved last time is sent now, in the background. No personal data.
        com.example.stats.CrashReporter.install(this)
        val usageCtx = applicationContext
        Thread { com.example.stats.UsagePing.pingIfDue(usageCtx) }.start()
        enableEdgeToEdge()
        // Ensure FLAG_SECURE is never active so screenshots and screen recordings work normally
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            TarkShastraTheme {
                com.example.update.AppUpdatePrompt()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = NavyBackground,
            topBar = {
                // Standard Netra header: 56 dp, only app name, version and date/time. Everything else scrolls.
                val ctx = androidx.compose.ui.platform.LocalContext.current
                var clockNow by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(java.util.Date()) }
                androidx.compose.runtime.LaunchedEffect(Unit) { while (true) { clockNow = java.util.Date(); kotlinx.coroutines.delay(30_000) } }
                val ownVersion = androidx.compose.runtime.remember { try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (e: Exception) { null } ?: "Unavailable" }
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().background(NavyBackground).statusBarsPadding().height(56.dp).padding(horizontal = 16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    androidx.compose.foundation.layout.Column {
                        Text(text = Brand.name, fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1, color = androidx.compose.ui.graphics.Color.White)
                        Text(text = "v" + ownVersion, fontSize = 12.sp, maxLines = 1, color = androidx.compose.ui.graphics.Color(0xFFB0B8C8))
                    }
                    Text(text = java.text.SimpleDateFormat("EEE d MMM, HH:mm", java.util.Locale.getDefault()).format(clockNow), fontSize = 12.sp, maxLines = 1, color = androidx.compose.ui.graphics.Color(0xFFB0B8C8))
                }
            },
                ) { innerPadding ->
                    TarkAppContent(
                        viewModel = quizViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        userLeftViaHome = true
        quizViewModel.onHomeOrBackgroundExit()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && userLeftViaHome) {
            quizViewModel.onHomeOrBackgroundExit()
        }
    }

    override fun onResume() {
        super.onResume()
        Thread { com.example.update.AppUpdater.cleanStale(applicationContext) }.start()
        userLeftViaHome = false
    }
}

@Composable
fun TarkAppContent(
    viewModel: QuizViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val isGameActive = when (uiState) {
        is QuizUiState.QuestionLoading,
        is QuizUiState.QuestionBankPreparing,
        is QuizUiState.ProfileInstalling,
        is QuizUiState.InGame -> true
        else -> false
    }
    EnsureGameSessionWindowFlags(isGameActive = isGameActive)

    when (val state = uiState) {
        is QuizUiState.HomeScreen -> {
            HomeScreen(viewModel = viewModel, modifier = modifier)
        }
        is QuizUiState.ProfileScreen -> {
            ProfileScreen(viewModel = viewModel, modifier = modifier)
        }
        is QuizUiState.HistoryScreen -> {
            HistoryScreen(viewModel = viewModel, modifier = modifier)
        }
        is QuizUiState.ItProfessionalHubScreen -> {
            ItProfessionalSectionScreen(
                onBack = { viewModel.navigateToHome() }
            )
        }
        is QuizUiState.QuestionLoading -> {
            LoadingScreen(modifier = modifier)
        }
        is QuizUiState.QuestionBankPreparing -> {
            QuestionBankPreparationScreen(
                progress = state.progress,
                onRetry = { viewModel.startNewGame() },
                modifier = modifier
            )
        }
        is QuizUiState.ProfileInstalling -> {
            ProfileInstallingScreen(
                progress = state.progress,
                message = state.message,
                modifier = modifier
            )
        }
        is QuizUiState.InGame -> {
            QuizScreen(state = state, viewModel = viewModel, modifier = modifier)
        }
        is QuizUiState.GameSummary -> {
            SummaryScreen(
                result = state.result,
                lastQuestion = state.lastQuestion,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        is QuizUiState.PermissionRequired -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0F1D))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162038))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔒 Security & Permissions Required",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.navigateToProfile() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text(text = "Go to Profile Settings & Grant Permissions", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.navigateToHome() }
                        ) {
                            Text(text = "Back to Home", color = Color(0xFFA0AEC0))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnsureGameSessionWindowFlags(isGameActive: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, isGameActive) {
        val window = (view.context as? android.app.Activity)?.window
        if (window != null) {
            // Ensure FLAG_SECURE is always cleared so screen sharing / casting never shows black screen
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            if (isGameActive) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
