package com.fishhawk.driftinglibraryandroid.data.database.model

import androidx.room.Entity

@Entity(primaryKeys = ["providerId", "mangaId"])
data class ReadingHistory(
    val providerId: String,
    val mangaId: String,
    val cover: String?,
    val title: String?,
    val authors: String?,
    var date: Long,
    var collectionId: String,
    var chapterId: String,
    var chapterName: String,
    var page: Int
)