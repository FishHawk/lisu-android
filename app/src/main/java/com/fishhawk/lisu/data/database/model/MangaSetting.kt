package com.fishhawk.lisu.data.database.model

import androidx.room.Entity
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.ReaderOrientation

@Entity(primaryKeys = ["providerId", "mangaId"])
data class MangaSetting(
    val providerId: String,
    val mangaId: String,
    val title: String?,
    val readerMode: ReaderMode? = null,
    val readerOrientation: ReaderOrientation? = null,
)