package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetadataOutline(
    val title: String?,
    val authors: List<String>?,
    val status: MangaStatus?
) : Parcelable

@Parcelize
data class MangaOutline(
    val id: String,
    var cover: String?,
    val updateTime: Long?,
    val source: Source?,
    val metadata: MetadataOutline,
    var hasNewMark: Boolean?
) : Parcelable {
    val title
        get() = metadata.title ?: id
}
