package com.fishhawk.lisu.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fishhawk.lisu.data.database.dao.ReadingHistoryDao
import com.fishhawk.lisu.data.database.model.ReadingHistory

@Database(
    entities = [ReadingHistory::class],
    version = 1,
    exportSchema = false
)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun readingHistoryDao(): ReadingHistoryDao
}