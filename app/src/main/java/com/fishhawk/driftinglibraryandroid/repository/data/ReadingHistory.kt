package com.fishhawk.driftinglibraryandroid.repository.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReadingHistory(
    @PrimaryKey val id: String,
    var title: String,
    var thumb: String,
    var date: Long,
    var collectionIndex: Int,
    var chapterIndex: Int,
    var pageIndex: Int
)