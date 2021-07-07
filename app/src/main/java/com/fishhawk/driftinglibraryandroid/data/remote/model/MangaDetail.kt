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
data class ChapterCollection(
    val id: String,
    var chapters: List<Chapter>
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
    var cover: String?,
    val updateTime: Long?,
    val source: Source?,
    val metadata: MetadataDetail,
    val collections: List<ChapterCollection>,
    var preview: List<String>?
) : Parcelable {
    val title
        get() = metadata.title ?: id
}
