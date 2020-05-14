package com.fishhawk.driftinglibraryandroid

import android.app.Application
import androidx.room.Room
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.ApplicationDatabase

class MainApplication : Application() {
    private lateinit var database: ApplicationDatabase
    lateinit var readingHistoryRepository: ReadingHistoryRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            ApplicationDatabase::class.java, "Test.db"
        ).build()
        readingHistoryRepository = ReadingHistoryRepository(database.readingHistoryDao())
    }
}