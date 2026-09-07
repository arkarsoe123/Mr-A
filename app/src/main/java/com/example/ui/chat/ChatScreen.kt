package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import com.example.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Message
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SurfaceColor
import com.example.utils.VoiceHelper
import com.example.utils.MusicPlayerHelper
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    
    val voiceHelper = remember {
        VoiceHelper(
            context = context,
            onSpeechResult = { result ->
                inputText = result
                isListening = false
            },
            onSpeechError = { _ ->
                isListening = false
            }
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
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            voiceHelper.startListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val infiniteTransition = rememberInfiniteTransition()
                    
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = if (isGenerating) 360f else 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                    
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isListening) 1.3f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    val isIdle = !isGenerating && !isListening
                    val floatOffset by infiniteTransition.animateFloat(
                        initialValue = if (isIdle) -4f else 0f,
                        targetValue = if (isIdle) 4f else 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    val iconTint by animateColorAsState(
                        targetValue = when {
                            isGenerating -> PrimaryPurple
                            isListening -> MaterialTheme.colorScheme.error
                            else -> PrimaryBlue
                        },
                        label = "robotTint"
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .offset(y = floatOffset.dp)
                                .scale(pulseScale)
                                .rotate(rotation)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cute_robot_avatar),
                                contentDescription = "Mr.A Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isListening) {
                                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.error.copy(alpha = 0.3f)))
                            } else if (isGenerating) {
                                Box(modifier = Modifier.fillMaxSize().background(PrimaryPurple.copy(alpha = 0.3f)))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mr.A", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStats) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("How can I help you today?", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
                items(messages) { message ->
                    MessageBubble(message = message)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (isGenerating) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryBlue)
                        }
                    }
                }
            }

            // Scroll to bottom when new message arrives
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
            
            // Input Bar
            Surface(
                color = SurfaceColor,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isListening) {
                                voiceHelper.stopListening()
                                isListening = false
                            } else {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    isListening = true
                                    voiceHelper.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask Mr.A...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PrimaryBlue
                        ),
                        maxLines = 4
                    )
                    
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.sendMessage(
                                    text = inputText,
                                    onAiResponse = { response ->
                                        voiceHelper.speak(response)
                                    },
                                    onAudioResponse = { base64Audio ->
                                        musicPlayer.playBase64Audio(base64Audio)
                                    }
                                )
                                inputText = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
    val textColor = if (isUser) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = message.text.replace("\\[WIDGET:.*?\\]".toRegex(), "").trim(),
                    color = textColor,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Generative UI parsing
            if (!isUser) {
                if (message.text.contains("[WIDGET: WEATHER]")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    WeatherWidget()
                }
                if (message.text.contains("[WIDGET: CALCULATOR]")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CalculatorWidget()
                }
                if (message.text.contains("[WIDGET: STATS]")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatsWidget()
                }
                
                val webviewRegex = "\\[WIDGET: WEBVIEW (.*?)\\]".toRegex()
                val webviewMatch = webviewRegex.find(message.text)
                if (webviewMatch != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    WebViewWidget(url = webviewMatch.groupValues[1])
                }
            }
        }
    }
}

@Composable
fun WeatherWidget() {
    Surface(
        color = SurfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(250.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Cloud, contentDescription = "Weather", tint = PrimaryBlue, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Yangon", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("32°C - Partly Cloudy", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun CalculatorWidget() {
    Surface(
        color = SurfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(250.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = PrimaryBlue, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Calculator Applet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Simulated action */ }) {
                Text("Open Calculator")
            }
        }
    }
}

@Composable
fun WebViewWidget(url: String) {
    Surface(
        color = SurfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(300.dp).padding(4.dp)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            },
            update = { webView ->
                webView.loadUrl(url)
            }
        )
    }
}

@Composable
fun StatsWidget() {
    Surface(
        color = SurfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(250.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usage Summary", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Queries Today: 12", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
            Text("Tokens Used: 4,500", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
        }
    }
}
