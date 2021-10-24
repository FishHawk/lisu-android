package com.fishhawk.lisu.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.fishhawk.lisu.data.database.dao.ReadingHistoryDao
import com.fishhawk.lisu.data.database.dao.SearchHistoryDao
import com.fishhawk.lisu.data.database.dao.ServerHistoryDao
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.database.model.ServerHistory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateTimeConverters {
    @TypeConverter
    fun toLocalDateTime(milli: Long): LocalDateTime =
        Instant.ofEpochMilli(milli).atZone(ZoneId.systemDefault()).toLocalDateTime()

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

@Database(
    entities = [
        ReadingHistory::class,
        SearchHistory::class,
        ServerHistory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTimeConverters::class)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun readingHistoryDao(): ReadingHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun serverHistoryDao(): ServerHistoryDao
}