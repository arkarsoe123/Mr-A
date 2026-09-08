package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.ChatRepository
import com.example.data.UserPreferences

class ChatViewModelFactory(
    private val repository: ChatRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
