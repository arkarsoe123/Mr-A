package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.chat.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    val useBuiltInModel by viewModel.useBuiltInModel.collectAsStateWithLifecycle()
    val useGoogleSearch by viewModel.useGoogleSearch.collectAsStateWithLifecycle()
    var apiKeyInput by remember { mutableStateOf("") }
    
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()
    var expandedProvider by remember { mutableStateOf(false) }
    val providers = listOf("Gemini", "ChatGPT", "DeepSeek")
    
    val systemPrompt by viewModel.systemPrompt.collectAsStateWithLifecycle()
    var systemPromptInput by remember { mutableStateOf(systemPrompt) }
    // Update local state when flow changes
    LaunchedEffect(systemPrompt) {
        systemPromptInput = systemPrompt
    }
    
    var expanded by remember { mutableStateOf(false) }
    var selectedOfflineModel by remember { mutableStateOf("Gemma 2B (2.1GB) - Compact") }
    val offlineModels = listOf("Gemma 2B (2.1GB) - Compact", "Gemma 7B (7.4GB) - Pro", "TinyLlama (600MB) - Fast")

    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("AI Model Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle for Built-in offline model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Offline Built-in Model", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("Use a basic built-in model that works offline without an API key.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
                Switch(
                    checked = useBuiltInModel,
                    onCheckedChange = { viewModel.setBuiltInMode(it) }
                )
            }
            
            if (useBuiltInModel) {
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedOfflineModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Offline Model Version") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        offlineModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    selectedOfflineModel = model
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Note: Models must be downloaded before first use. Currently simulated.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (!useBuiltInModel) {
                Text("Cloud Model Configuration", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expandedProvider,
                    onExpandedChange = { expandedProvider = !expandedProvider }
                ) {
                    OutlinedTextField(
                        value = selectedProvider,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("AI Provider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvider) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProvider,
                        onDismissRequest = { expandedProvider = false }
                    ) {
                        providers.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider) },
                                onClick = {
                                    viewModel.setSelectedProvider(provider)
                                    expandedProvider = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("API Key for $selectedProvider", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.saveApiKeyForProvider(selectedProvider, apiKeyInput)
                        apiKeyInput = ""
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save API Key")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Google Search Grounding", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Enhance AI responses with real-time Google Search data.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    }
                    Switch(
                        checked = useGoogleSearch,
                        onCheckedChange = { viewModel.setUseGoogleSearch(it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Persona & Behavior", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("System Prompt", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("Define Mr.A's persona, tone, and default behavior.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = systemPromptInput,
                onValueChange = { systemPromptInput = it },
                label = { Text("System Prompt") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.setSystemPrompt(systemPromptInput) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Persona")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Model Updates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("You can upgrade the Built-in AI model by checking our GitHub repository.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(16.dp))
            
            FilledTonalButton(
                onClick = { 
                    // Open github link
                    uriHandler.openUri("https://github.com/google/generative-ai-android")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check for Updates on GitHub")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.clearHistory() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Chat History")
            }
        }
    }
}
