package com.fishhawk.driftinglibraryandroid.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fishhawk.driftinglibraryandroid.data.database.dao.ReadingHistoryDao
import com.fishhawk.driftinglibraryandroid.data.database.dao.ServerInfoDao
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo

@Database(entities = [ReadingHistory::class, ServerInfo::class], version = 1, exportSchema = false)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun readingHistoryDao(): ReadingHistoryDao
    abstract fun serverInfoDao(): ServerInfoDao
}