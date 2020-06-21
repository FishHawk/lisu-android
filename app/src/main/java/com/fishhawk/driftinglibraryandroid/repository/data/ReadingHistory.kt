package com.fishhawk.driftinglibraryandroid.repository.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReadingHistory(
    @PrimaryKey val id: String,
    var title: String,
    var thumb: String,
    var source: String?,
    var date: Long,
    var collectionIndex: Int,
    var collectionTitle: String,
    var chapterIndex: Int,
    var chapterTitle: String,
    var pageIndex: Int
)