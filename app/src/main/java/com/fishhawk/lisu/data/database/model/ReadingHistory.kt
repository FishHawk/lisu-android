package com.fishhawk.lisu.data.database.model

import androidx.room.Entity
import com.fishhawk.lisu.data.network.model.MangaState
import java.time.LocalDateTime

@Entity(primaryKeys = ["providerId", "mangaId"])
data class ReadingHistory(
    val state: MangaState,
    val providerId: String,

    val mangaId: String,

    val cover: String?,
    val title: String?,
    val authors: String?,

    var date: LocalDateTime = LocalDateTime.now(),

    var collectionId: String,
    var chapterId: String,
    var chapterName: String,
    var page: Int,
)