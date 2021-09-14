package com.fishhawk.driftinglibraryandroid.data.database.model

import androidx.room.Embedded
import androidx.room.Entity
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider

@Entity(primaryKeys = ["mangaId"])
data class ReadingHistory(
    val mangaId: String,
    val cover: String?,
    val title: String?,
    val authors: String?,
    @Embedded val provider: Provider?,
    var date: Long,
    var collection: String,
    var chapterId: String,
    var chapterName: String,
    var page: Int
)