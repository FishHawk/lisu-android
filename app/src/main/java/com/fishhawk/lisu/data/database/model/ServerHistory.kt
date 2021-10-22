package com.fishhawk.lisu.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.util.*

@Entity
data class ServerHistory(
    @PrimaryKey
    val address: String,
    val date: LocalDateTime = LocalDateTime.now()
)
