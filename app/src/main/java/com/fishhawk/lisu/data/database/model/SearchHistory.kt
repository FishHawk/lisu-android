package com.fishhawk.lisu.data.database.model

import androidx.room.Entity
import java.time.LocalDateTime

@Entity(primaryKeys = ["providerId", "keywords"])
data class SearchHistory(
    val providerId: String,
    val keywords: String,
    val date: LocalDateTime = LocalDateTime.now()
)
