package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chapter(
    val id: String,
    val name: String,
    val title: String
) : Parcelable

@Parcelize
data class Collection(
    val id: String,
    val chapters: List<Chapter>
) : Parcelable

@Parcelize
data class TagGroup(
    val key: String,
    val value: List<String>
) : Parcelable

@Parcelize
data class MetadataDetail(
    val title: String?,
    val authors: List<String>?,
    val status: MangaStatus?,
    val description: String?,
    val tags: List<TagGroup>?
) : Parcelable

@Parcelize
data class MangaDetail(
    val provider: ProviderInfo?,
    val id: String,
    var thumb: String?,
    val updateTime: Long?,
    val source: Source?,
    val metadata: MetadataDetail,
    val collections: List<Collection>
) : Parcelable {
    val title
        get() = metadata.title ?: id
}
