package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.*
import com.example.ui.utils.adaptiveContentWidth
import com.example.ui.utils.adaptiveFontScaleFor
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.delay

data class ItQaItem(
    val id: String,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val technicalImpact: String
)

@Composable
fun ItProfessionalSectionScreen(
    onBack: () -> Unit
) {
    // Randomized question pool for freshness
    val initialQaList = remember {
        listOf(
            ItQaItem(
                id = "algo_1",
                category = "Algorithm & Complexity",
                question = "What is the worst-case time complexity of Dijkstra's algorithm implemented with an adjacency list and a Fibonacci heap vs a standard binary min-heap?",
                options = listOf(
                    "O(V log V + E) vs O((V + E) log V)",
                    "O(V^2) vs O(E log V)",
                    "O(V + E) vs O(V log V)",
                    "O(E log V) vs O(V^2)"
                ),
                correctIndex = 0,
                explanation = "A Fibonacci heap supports decrease-key in O(1) amortized time, yielding O(V log V + E) overall complexity, whereas a binary heap takes O(log V) per decrease-key, leading to O((V + E) log V).",
                technicalImpact = "Critical for high-throughput routing protocols, graph neural network message passing, and real-time pathfinding at scale."
            ),
            ItQaItem(
                id = "gpu_1",
                category = "GPU Limitations & VRAM",
                question = "During large-scale LLM inference, what primary hardware bottleneck causes sudden latency spikes known as the 'KV Cache Memory Bandwidth Saturation'?",
                options = listOf(
                    "Saturating HBM / VRAM memory bandwidth during attention matrix key-value tensor lookups",
                    "CPU thermal throttling during matrix multiplication scheduling",
                    "PCIe bus lane misconfiguration dropping from PCIe Gen5 to Gen3",
                    "L1 cache miss in CPU vector registers"
                ),
                correctIndex = 0,
                explanation = "Attention mechanisms require fetching massive KV cache tensors from VRAM for every generated token. Since memory bandwidth doesn't scale as fast as compute (FLOPs), memory bandwidth becomes the primary bottleneck.",
                technicalImpact = "Limits maximum concurrent token generation throughput (tokens/sec/user) in production LLM serving clusters."
            ),
            ItQaItem(
                id = "telemetry_1",
                category = "Telemetry & Distributed Tracing",
                question = "In OpenTelemetry distributed tracing across asynchronous Kafka message queues, what is the consequence of failing to inject traceparent headers into message metadata?",
                options = listOf(
                    "Broken trace trees resulting in disconnected child spans and orphan traces in APM dashboards",
                    "Deadlock in consumer thread pool due to unreleased locks",
                    "Out-of-memory error caused by accumulating uncollected spans",
                    "Kafka broker schema validation failure"
                ),
                correctIndex = 0,
                explanation = "Without traceparent context propagation across message boundaries, APM backends cannot link producer spans to consumer spans, breaking end-to-end transaction visibility.",
                technicalImpact = "Obscures root cause analysis during microservice latency degradation and increases Mean Time to Resolution (MTTR)."
            ),
            ItQaItem(
                id = "code_1",
                category = "Coding Series & Concurrency",
                question = "In a multi-threaded Kotlin Coroutine application, what concurrency bug occurs when two coroutines concurrently mutate a shared standard MutableList without confinement or Mutex?",
                options = listOf(
                    "ConcurrentModificationException or silent data corruption due to non-atomic index updates",
                    "StackOverflowError in coroutine dispatcher queue",
                    "Immediate JVM process termination with SIGSEGV",
                    "Deadlock across dispatcher threads"
                ),
                correctIndex = 0,
                explanation = "Standard lists are not thread-safe. Concurrent additions/mutations lead to internal array resizing races, resulting in lost updates, out-of-bounds exceptions, or corrupted internal states.",
                technicalImpact = "Causes intermittent production crashes and difficult-to-reproduce state inconsistencies in state management layers."
            ),
            ItQaItem(
                id = "algo_2",
                category = "Algorithm & Complexity",
                question = "In Dynamic Programming with overlapping subproblems (e.g., Edit Distance / Levenshtein), why does top-down memoization occasionally perform worse than bottom-up tabulation?",
                options = listOf(
                    "Function call overhead and stack frame allocation overhead in recursive calls",
                    "Garbage collector pressure from temporary string allocations",
                    "Lack of branch prediction on modern CPU architectures",
                    "Hash collision overhead in memoization map"
                ),
                correctIndex = 0,
                explanation = "Recursive top-down memoization incurs deep stack frame allocations and function call overhead, whereas bottom-up tabulation iterates sequentially through pre-allocated 2D tables with optimal cache locality.",
                technicalImpact = "Affects latency profiles in sequence alignment, bioinformatics, and NLP tokenization pipelines."
            ),
            ItQaItem(
                id = "gpu_2",
                category = "GPU Limitations & VRAM",
                question = "What is the primary architectural purpose of Quantization (e.g., INT8 / FP4) in deep learning model deployment?",
                options = listOf(
                    "Reducing memory footprint and accelerating memory-bound bandwidth operations during inference",
                    "Increasing model parameter count to improve zero-shot reasoning",
                    "Eliminating the need for GPU accelerators entirely",
                    "Converting convolutional layers into recurrent neural networks"
                ),
                correctIndex = 0,
                explanation = "Quantization reduces precision from FP16/FP32 to INT8/INT4, drastically shrinking VRAM requirements and accelerating memory-bandwidth-bound inference.",
                technicalImpact = "Enables deploying frontier models on edge devices and reduces cloud GPU operational costs by 50%+"
            ),
            ItQaItem(
                id = "telemetry_2",
                category = "Telemetry & Distributed Tracing",
                question = "When designing high-throughput APM metric collection, why is tail-based sampling preferred over head-based sampling for capturing rare system errors?",
                options = listOf(
                    "Tail-based sampling evaluates the entire trace before deciding to retain it, ensuring rare error traces are never dropped",
                    "Head-based sampling consumes zero CPU cycles",
                    "Tail-based sampling eliminates network latency entirely",
                    "Head-based sampling requires no memory buffer"
                ),
                correctIndex = 0,
                explanation = "Head-based sampling decides whether to keep a trace at the root span before knowing if an error occurs downstream. Tail-based sampling inspects the completed trace, capturing 100% of error traces while dropping routine healthy traces.",
                technicalImpact = "Crucial for debugging intermittent production exceptions without incurring prohibitive telemetry storage costs."
            ),
            ItQaItem(
                id = "code_2",
                category = "Coding Series & Concurrency",
                question = "What is the primary cause of memory leaks in Android ViewModel or long-lived coroutine scopes when referencing Activity Context?",
                options = listOf(
                    "Holding a strong reference to a destroyed Activity instance after configuration change or finish()",
                    "Using Kotlin Flow instead of LiveData",
                    "Calling Dispatchers.IO from the main thread",
                    "Overusing Jetpack Compose Recomposition"
                ),
                correctIndex = 0,
                explanation = "Activities are garbage-collected upon destruction unless retained by a longer-lived component (like a ViewModel or static reference). Holding Activity context causes the entire View hierarchy to leak.",
                technicalImpact = "Leads to OutOfMemoryError (OOM) crashes and severe memory bloat during prolonged user navigation sessions."
            )
        ).shuffled()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerLocked by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }
    var timeRemainingSeconds by remember { mutableIntStateOf(60) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    val currentQuestion = if (currentIndex < initialQaList.size) initialQaList[currentIndex] else null

    // Timer effect per question
    LaunchedEffect(currentIndex, isAnswerLocked, isQuizCompleted) {
        if (isQuizCompleted || currentQuestion == null) return@LaunchedEffect
        timeRemainingSeconds = 60
        while (timeRemainingSeconds > 0 && !isAnswerLocked && !isQuizCompleted) {
            delay(1000L)
            timeRemainingSeconds--
        }
        // Time expired auto-lock if not answered
        if (timeRemainingSeconds == 0 && !isAnswerLocked && !isQuizCompleted) {
            isAnswerLocked = true
        }
    }

    val fontScale = adaptiveFontScaleFor(LocalConfiguration.current.screenWidthDp)
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground),
        color = NavyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .adaptiveContentWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("it_section_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "IT Professionals & AI Hub",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize * fontScale
                            )
                        )
                        Text(
                            text = if (!isQuizCompleted && currentQuestion != null) "Question ${currentIndex + 1} of ${initialQaList.size}" else "Quiz Completed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }

                if (!isQuizCompleted) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (timeRemainingSeconds <= 10) Color(0xFF5A1E1E) else NavyCard,
                        border = BorderStroke(1.dp, if (timeRemainingSeconds <= 10) Color.Red else GoldPrimary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (timeRemainingSeconds <= 10) Color.Red else GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${timeRemainingSeconds}s",
                                color = if (timeRemainingSeconds <= 10) Color.Red else GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isQuizCompleted || currentQuestion == null) {
                // Summary Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "IT & AI Professional Assessment Completed!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Score: $correctAnswersCount / ${initialQaList.size} Correct",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    currentIndex = 0
                                    correctAnswersCount = 0
                                    selectedOptionIndex = null
                                    isAnswerLocked = false
                                    isQuizCompleted = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Restart IT Quiz",
                                    color = NavyDeepest,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onBack,
                                border = BorderStroke(1.dp, GoldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Return to Home",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // Active Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("it_qa_active_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Category Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = InfoCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = currentQuestion.category,
                                color = InfoCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options Column
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentQuestion.options.forEachIndexed { index, optionText ->
                                val isSelected = selectedOptionIndex == index
                                val isCorrect = index == currentQuestion.correctIndex
                                val containerColor = when {
                                    !isAnswerLocked -> NavyDeepest
                                    isCorrect -> SuccessGreen.copy(alpha = 0.25f)
                                    isSelected -> Color(0xFF5A1E1E)
                                    else -> NavyDeepest
                                }
                                val borderColor = when {
                                    !isAnswerLocked -> NavyBorder
                                    isCorrect -> SuccessGreen
                                    isSelected -> Color.Red
                                    else -> NavyBorder
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isAnswerLocked) {
                                            selectedOptionIndex = index
                                            isAnswerLocked = true
                                            if (index == currentQuestion.correctIndex) {
                                                correctAnswersCount++
                                            }
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = containerColor,
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${('A' + index)}. ",
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = optionText,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        if (isAnswerLocked) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = NavyBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 Technical Explanation:",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentQuestion.explanation,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚙️ Production Impact:",
                                color = InfoCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentQuestion.technicalImpact,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (currentIndex + 1 < initialQaList.size) {
                                        currentIndex++
                                        selectedOptionIndex = null
                                        isAnswerLocked = false
                                    } else {
                                        isQuizCompleted = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (currentIndex + 1 < initialQaList.size) "Next Question ➔" else "View Final Results 🏆",
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
}
