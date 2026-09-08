package com.example.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Message
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SurfaceColor
import com.example.utils.MusicPlayerHelper
import com.example.utils.VoiceHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToDeveloper: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    val voiceHelper = remember {
        VoiceHelper(
            context = context,
            onSpeechResult = { result -> inputText = result; isListening = false },
            onSpeechError = { isListening = false }
        )
    }
    val musicPlayer = remember { MusicPlayerHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
            musicPlayer.stop()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListening = true
            voiceHelper.startListening()
        }
    }

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem((messages.size + if (isGenerating) 1 else 0) - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.mra_logo),
                                contentDescription = "Mr.A",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.width(9.dp))
                        Column {
                            Text("Mr.A", fontWeight = FontWeight.Bold)
                            Text(
                                if (isGenerating) "Thinking…" else "AI Assistant",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDeveloper) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Developer profile")
                    }
                    IconButton(onClick = onNavigateToStats) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = SurfaceColor,
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    voiceHelper.stopListening()
                                    isListening = false
                                } else if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED) {
                                    isListening = true
                                    voiceHelper.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        ) {
                            Icon(
                                if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice input",
                                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask Mr.A…") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            maxLines = 4
                        )

                        if (inputText.isNotBlank() && !isGenerating) {
                            IconButton(
                                onClick = {
                                    val text = inputText.trim()
                                    if (text.isNotEmpty()) {
                                        viewModel.sendMessage(
                                            text = text,
                                            onAiResponse = { response -> voiceHelper.speak(response) },
                                            onAudioResponse = { audio -> musicPlayer.playBase64Audio(audio) }
                                        )
                                        inputText = ""
                                        keyboardController?.hide()
                                        coroutineScope.launch {
                                            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PrimaryBlue)
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (error != null) {
                AssistChip(
                    onClick = { },
                    label = { Text("Connection issue — see the last message for details") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        WelcomeCard(onDeveloper = onNavigateToDeveloper)
                    }
                }
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)
                    Spacer(Modifier.height(12.dp))
                }
                if (isGenerating) {
                    item {
                        ThinkingIndicator()
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(onDeveloper: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.mra_logo),
                    contentDescription = "Mr.A logo",
                    modifier = Modifier.size(92.dp).clip(RoundedCornerShape(24.dp))
                )
                Spacer(Modifier.height(14.dp))
                Text("မင်္ဂလာပါ 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "ကျွန်တော်က Mr.A — သင့်ရဲ့ AI Assistant ပါ။",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    "မေးခွန်း၊ code၊ ဘာသာပြန်၊ အကြံဉာဏ်နဲ့ လိုအပ်သလို Smart UI Card တွေကို ကူညီပေးနိုင်ပါတယ်။",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onDeveloper) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Developer & Support")
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    val transition = rememberInfiniteTransition(label = "thinking")
    val scale by transition.animateFloat(
        0.75f, 1.15f,
        infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).scale(scale).clip(CircleShape).background(PrimaryBlue))
        Spacer(Modifier.width(7.dp))
        Box(Modifier.size(10.dp).scale(scale).clip(CircleShape).background(PrimaryPurple))
        Spacer(Modifier.width(7.dp))
        Box(Modifier.size(10.dp).scale(scale).clip(CircleShape).background(PrimaryBlue))
        Spacer(Modifier.width(10.dp))
        Text("Mr.A is thinking…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.isUser
    val displayText = message.text.replace("\\[(WIDGET|CARD):[^]]*]".toRegex(), "").replace("\\[WIDGET: WEBVIEW [^]]*]".toRegex(), "").trim()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else SurfaceColor,
                contentColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                Text(
                    text = displayText.ifBlank { if (isUser) "" else "" },
                    modifier = Modifier.padding(15.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (!isUser) {
                SmartCards(message.text)
            }
        }
    }
}

@Composable
private fun SmartCards(raw: String) {
    when {
        raw.contains("[CARD: WEATHER]") || raw.contains("[WIDGET: WEATHER]") -> WeatherCard()
        raw.contains("[CARD: CALCULATOR]") || raw.contains("[WIDGET: CALCULATOR]") -> CalculatorCard()
        raw.contains("[CARD: STATS]") || raw.contains("[WIDGET: STATS]") -> StatsCard()
        raw.contains("[CARD: TASK]") -> TaskCard()
        raw.contains("[CARD: REMINDER]") -> ReminderCard()
        raw.contains("[CARD: TRANSLATION]") -> TranslationCard()
        raw.contains("[CARD: CODE]") -> CodeCard(raw)
        raw.contains("[CARD: SUPPORT]") -> SupportCard()
    }
}

@Composable
private fun CardShell(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(top = 8.dp).fillMaxWidth().widthIn(max = 360.dp)
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
private fun WeatherCard() = CardShell {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Cloud, null, tint = PrimaryBlue, modifier = Modifier.size(42.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Weather", fontWeight = FontWeight.Bold)
            Text("Live weather card — data can be connected to a weather service.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalculatorCard() = CardShell {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Calculate, null, tint = PrimaryBlue, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Calculator", fontWeight = FontWeight.Bold)
            Text("A calculation result can be presented here as a clean action card.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatsCard() = CardShell {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.BarChart, null, tint = PrimaryPurple, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Usage Summary", fontWeight = FontWeight.Bold)
            Text("Your Mr.A usage and token statistics can be displayed here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TaskCard() = CardShell {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.TaskAlt, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Task Plan", fontWeight = FontWeight.Bold)
            Text("AI detected a task-oriented request and converted it to an actionable card.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReminderCard() = CardShell {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Reminder", fontWeight = FontWeight.Bold)
            Text("Reminder details can be shown here before adding them to the device calendar/alarm system.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TranslationCard() = CardShell {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Translate, null, tint = PrimaryPurple, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Translation", fontWeight = FontWeight.Bold)
            Text("English ↔ မြန်မာ translation result card.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CodeCard(raw: String) {
    val clipboard = LocalClipboardManager.current
    val code = raw.substringAfter("[CARD: CODE]").substringBefore("[/CARD]").trim().ifBlank { "Code example" }
    CardShell {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Code, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(10.dp))
            Text("Code Assistant", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp)) {
            Text(code, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = { clipboard.setText(AnnotatedString(code)) }) {
            Icon(Icons.Default.ContentCopy, null)
            Spacer(Modifier.width(6.dp))
            Text("Copy code")
        }
    }
}

@Composable
private fun SupportCard() = CardShell {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Support Mr.A", fontWeight = FontWeight.Bold)
            Text("KPay / Wave Money support options are available from Developer & Support.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
