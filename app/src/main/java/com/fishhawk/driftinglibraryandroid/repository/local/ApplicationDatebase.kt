package com.fishhawk.driftinglibraryandroid.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fishhawk.driftinglibraryandroid.repository.local.dao.ReadingHistoryDao
import com.fishhawk.driftinglibraryandroid.repository.local.dao.ServerInfoDao
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo

@Database(entities = [ReadingHistory::class, ServerInfo::class], version = 1, exportSchema = false)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun readingHistoryDao(): ReadingHistoryDao
    abstract fun serverInfoDao(): ServerInfoDao
}