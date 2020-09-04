package com.fishhawk.driftinglibraryandroid.repository.local.model

import androidx.room.Entity


@Entity(primaryKeys = ["mangaId", "serverId"])
data class ReadingHistory(
    val mangaId: String,
    val serverId: Int,

    var title: String,
    var thumb: String,
    var providerId: String?,
    var date: Long,

    var collectionTitle: String,
    var collectionIndex: Int,
    var chapterTitle: String,
    var chapterIndex: Int,
    var pageIndex: Int
)