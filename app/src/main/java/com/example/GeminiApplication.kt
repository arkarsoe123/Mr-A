package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.ChatRepository
import com.example.data.UserPreferences

class GeminiApplication : Application() {
    
    lateinit var database: AppDatabase
        private set
        
    lateinit var repository: ChatRepository
        private set
        
    lateinit var userPreferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "gemini_db"
        ).build()
        repository = ChatRepository(database.messageDao())
        userPreferences = UserPreferences(this)
    }
}
