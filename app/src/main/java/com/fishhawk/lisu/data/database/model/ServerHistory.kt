package com.fishhawk.lisu.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class ServerHistory(
    @PrimaryKey
    val address: String,
    val date: LocalDateTime = LocalDateTime.now()
)
