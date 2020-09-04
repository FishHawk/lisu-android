package com.fishhawk.driftinglibraryandroid.repository.remote.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MetadataOutline(
    val title: String?,
    val authors: List<String>?,
    val status: MangaStatus?
) : Parcelable

@Parcelize
data class MangaOutline(
    val id: String,
    var thumb: String?,
    val updateTime: Long?,
    val metadata: MetadataOutline
) : Parcelable {
    val title
        get() = metadata.title ?: id
    val authors
        get() = metadata.authors?.joinToString(";")
}
