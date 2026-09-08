package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    companion object {
        val API_KEY = stringPreferencesKey("api_key") // Gemini Key
        val OPENAI_KEY = stringPreferencesKey("openai_key")
        val DEEPSEEK_KEY = stringPreferencesKey("deepseek_key")
        val SELECTED_PROVIDER = stringPreferencesKey("selected_provider")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val USE_BUILT_IN = booleanPreferencesKey("use_built_in")
        val USE_GOOGLE_SEARCH = booleanPreferencesKey("use_google_search")
    }

    val apiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[API_KEY]
    }
    
    val openAiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[OPENAI_KEY]
    }
    
    val deepSeekKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DEEPSEEK_KEY]
    }
    
    val selectedProvider: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_PROVIDER] ?: "Gemini"
    }

    val systemPrompt: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SYSTEM_PROMPT] ?: "Your name is Mr.A, a helpful AI assistant."
    }

    val useBuiltInModel: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_BUILT_IN] ?: false
    }

    val useGoogleSearch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_GOOGLE_SEARCH] ?: false
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }
    
    suspend fun saveOpenAiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENAI_KEY] = key
        }
    }
    
    suspend fun saveDeepSeekKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[DEEPSEEK_KEY] = key
        }
    }
    
    suspend fun setSelectedProvider(provider: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_PROVIDER] = provider
        }
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[SYSTEM_PROMPT] = prompt
        }
    }

    suspend fun setUseBuiltInModel(useBuiltIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_BUILT_IN] = useBuiltIn
        }
    }

    suspend fun setUseGoogleSearch(useSearch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_GOOGLE_SEARCH] = useSearch
        }
    }
}
