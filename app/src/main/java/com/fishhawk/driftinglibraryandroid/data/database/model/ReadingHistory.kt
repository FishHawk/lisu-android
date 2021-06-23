package com.fishhawk.driftinglibraryandroid.data.database.model

import androidx.room.Entity


@Entity(primaryKeys = ["mangaId", "serverId"])
data class ReadingHistory(
    val mangaId: String,
    val serverId: Int,

    var title: String,
    var cover: String,
    var providerId: String?,
    var date: Long,

    var collectionTitle: String,
    var collectionIndex: Int,
    var chapterTitle: String,
    var chapterIndex: Int,
    var pageIndex: Int
)