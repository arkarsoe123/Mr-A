package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.ChatRepository
import com.example.data.Message
import com.example.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val messages: StateFlow<List<Message>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val selectedProvider = userPreferences.selectedProvider.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Gemini"
    )

    val useGoogleSearch = userPreferences.useGoogleSearch.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val systemPrompt = userPreferences.systemPrompt.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Your name is Mr.A, a helpful AI assistant."
    )

    fun sendMessage(text: String, onAiResponse: ((String) -> Unit)? = null, onAudioResponse: ((String) -> Unit)? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _error.value = null
            repository.insertMessage(Message(text = text, isUser = true))
            
            _isGenerating.value = true
            
            val isOffline = useBuiltInModel.first()
            if (isOffline) {
                // Simulate built-in offline model
                val responseText = simulateOfflineModel(text)
                repository.insertMessage(Message(text = responseText, isUser = false))
                onAiResponse?.invoke(responseText)
            } else {
                val provider = selectedProvider.first()
                val apiKey = when (provider) {
                    "ChatGPT" -> userPreferences.openAiKey.first()
                    "DeepSeek" -> userPreferences.deepSeekKey.first()
                    else -> userPreferences.apiKey.first()
                }

                if (apiKey.isNullOrEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    val msg = "Please configure your API Key for $provider in Settings to use the Cloud Model."
                    repository.insertMessage(Message(text = msg, isUser = false))
                    onAiResponse?.invoke(msg)
                } else {
                    try {
                        val isMusicRequest = text.lowercase().contains("music") || text.lowercase().contains("song")
                        val isSearchEnabled = useGoogleSearch.first()

                        var responseText = ""
                        var base64Audio: String? = null

                        if (provider == "Gemini") {
                            val tools = if (isSearchEnabled && !isMusicRequest) {
                                listOf(com.example.api.Tool(googleSearch = emptyMap()))
                            } else null

                            val generationConfig = if (isMusicRequest) {
                                com.example.api.GenerationConfig(responseModalities = listOf("AUDIO"))
                            } else null

                            val basePrompt = systemPrompt.first()
                            val fullPrompt = "$basePrompt\n\n(System Note: Mr.A should answer naturally and only show a Smart UI card when it helps. Supported markers are [CARD: WEATHER], [CARD: CALCULATOR], [CARD: STATS], [CARD: TASK], [CARD: REMINDER], [CARD: TRANSLATION], [CARD: CODE], [CARD: SUPPORT]. Put the marker at the end of the response. Do not show the marker as normal user-facing text. Keep Myanmar answers in Myanmar and English answers in English.)"

                            val systemInstruction = Content(
                                role = "user",
                                parts = listOf(Part(text = fullPrompt))
                            )

                            val request = GenerateContentRequest(
                                contents = buildChatHistory(text),
                                systemInstruction = systemInstruction,
                                tools = tools,
                                generationConfig = generationConfig
                            )
                            val response = RetrofitClient.service.generateContent(apiKey, request)
                            
                            response.candidates?.firstOrNull()?.content?.parts?.forEach { part ->
                                if (part.text != null) {
                                    responseText += part.text
                                }
                                if (part.inlineData != null && part.inlineData.mimeType.startsWith("audio/")) {
                                    base64Audio = part.inlineData.data
                                }
                                if (part.executableCode != null) {
                                    responseText += "\n[Executed Code: ${part.executableCode["code"]}]"
                                }
                            }
                        } else {
                            // ChatGPT or DeepSeek
                            val modelName = if (provider == "DeepSeek") "deepseek-chat" else "gpt-3.5-turbo"
                            val baseUrl = if (provider == "DeepSeek") "https://api.deepseek.com/v1/chat/completions" else "https://api.openai.com/v1/chat/completions"
                            
                            val previousMessages = messages.value.map { msg ->
                                com.example.api.OpenAiMessage(
                                    role = if (msg.isUser) "user" else "assistant",
                                    content = msg.text
                                )
                            }.toMutableList()
                            
                            val basePrompt = systemPrompt.first()
                            val fullPrompt = "$basePrompt\n\n(System Note: Mr.A should answer naturally and only show a Smart UI card when it helps. Supported markers are [CARD: WEATHER], [CARD: CALCULATOR], [CARD: STATS], [CARD: TASK], [CARD: REMINDER], [CARD: TRANSLATION], [CARD: CODE], [CARD: SUPPORT]. Put the marker at the end of the response. Do not show the marker as normal user-facing text. Keep Myanmar answers in Myanmar and English answers in English.)"
                            previousMessages.add(0, com.example.api.OpenAiMessage(role = "system", content = fullPrompt))
                            previousMessages.add(com.example.api.OpenAiMessage(role = "user", content = text))
                            
                            val request = com.example.api.OpenAiRequest(
                                model = modelName,
                                messages = previousMessages
                            )
                            
                            val response = com.example.api.GenericAiClient.service.generateContent(
                                url = baseUrl,
                                authHeader = "Bearer $apiKey",
                                request = request
                            )
                            
                            responseText = response.choices?.firstOrNull()?.message?.content ?: ""
                        }
                        
                        val inferredCard = inferCardMarker(text)
                        if (inferredCard != null && !responseText.contains("[CARD:")) {
                            responseText = responseText.trimEnd() + "\n[CARD: $inferredCard]"
                        }

                        if (responseText.isBlank() && base64Audio != null) {
                            responseText = "Here is your generated audio track."
                        } else if (responseText.isBlank()) {
                            responseText = "I'm sorry, I couldn't generate a response."
                        }
                        
                        repository.insertMessage(Message(text = responseText, isUser = false))
                        onAiResponse?.invoke(responseText)
                        
                        if (base64Audio != null) {
                            onAudioResponse?.invoke(base64Audio!!)
                        }
                    } catch (e: Exception) {
                        _error.value = e.message ?: "Failed to connect to cloud model"
                        val msg = "Error: ${_error.value}. Try turning on 'Offline Built-in Model' in Settings if you have no internet."
                        repository.insertMessage(Message(text = msg, isUser = false))
                        onAiResponse?.invoke(msg)
                    }
                }
            }
            _isGenerating.value = false
        }
    }

    private fun inferCardMarker(prompt: String): String? {
        val p = prompt.lowercase()
        return when {
            listOf("weather", "မိုး", "ရာသီဥတု").any { p.contains(it) } -> "WEATHER"
            listOf("calculator", "calculate", "တွက်", "ပေါင်း", "မြှောက်", "စား", "နုတ်").any { p.contains(it) } -> "CALCULATOR"
            listOf("translate", "translation", "ဘာသာပြန်", "ဘာသာပြန်ပေး").any { p.contains(it) } -> "TRANSLATION"
            listOf("code", "python", "kotlin", "javascript", "programming").any { p.contains(it) } -> "CODE"
            listOf("remind", "reminder", "သတိပေး", "အချိန်မှတ်").any { p.contains(it) } -> "REMINDER"
            listOf("todo", "task", "လုပ်စရာ", "လုပ်ရန်စာရင်း").any { p.contains(it) } -> "TASK"
            listOf("support", "donate", "ထောက်ပံ့", "လှူ").any { p.contains(it) } -> "SUPPORT"
            listOf("stats", "statistics", "အသုံးပြုမှု", "စာရင်း").any { p.contains(it) } -> "STATS"
            else -> null
        }
    }

    private fun simulateOfflineModel(prompt: String): String {
        // A mock implementation of a local/offline model
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") -> "Hello! I am Mr.A, your offline built-in AI. How can I assist you today?"
            lower.contains("name") -> "I am Mr.A, running on the built-in offline model."
            lower.contains("weather") -> "I am currently offline and cannot check live weather. [CARD: WEATHER]"
            lower.contains("calculator") || lower.contains("calc") -> "Here is a calculator for you. [CARD: CALCULATOR]"
            lower.contains("stats") || lower.contains("statistics") -> "Here is your stats summary. [CARD: STATS]"
            lower.contains("help") -> "I can answer basic questions offline. For more complex queries, please connect to the internet and use the Cloud API."
            else -> "I understand you said: '$prompt'. This is a response from Mr.A (built-in offline model). I'm operating locally on your device."
        }
    }

    private fun buildChatHistory(newPrompt: String): List<Content> {
        val history = mutableListOf<Content>()
        // In a real app we would load a subset of history. Let's just pass the last few.
        val recentMessages = messages.value.takeLast(10)
        for (msg in recentMessages) {
            history.add(
                Content(
                    parts = listOf(Part(text = msg.text)),
                    role = if (msg.isUser) "user" else "model"
                )
            )
        }
        history.add(
            Content(
                parts = listOf(Part(text = newPrompt)),
                role = "user"
            )
        )
        return history
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    val useBuiltInModel = userPreferences.useBuiltInModel.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun saveApiKeyForProvider(provider: String, key: String) {
        viewModelScope.launch {
            when (provider) {
                "ChatGPT" -> userPreferences.saveOpenAiKey(key)
                "DeepSeek" -> userPreferences.saveDeepSeekKey(key)
                else -> userPreferences.saveApiKey(key)
            }
        }
    }
    
    fun setSelectedProvider(provider: String) {
        viewModelScope.launch {
            userPreferences.setSelectedProvider(provider)
        }
    }

    fun setSystemPrompt(prompt: String) {
        viewModelScope.launch {
            userPreferences.setSystemPrompt(prompt)
        }
    }

    fun setBuiltInMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setUseBuiltInModel(enabled)
        }
    }

    fun setUseGoogleSearch(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setUseGoogleSearch(enabled)
        }
    }
}
