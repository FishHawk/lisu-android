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
    val title: String? = null,
    val authors: List<String>? = null,
    val status: MangaStatus? = null,
    val description: String? = null,
    val tags: List<TagGroup>? = null
) : Parcelable {

    constructor(outline: MetadataOutline) : this(
        title = outline.title,
        authors = outline.authors,
        status = outline.status,
    )
}

@Parcelize
data class MangaDetail(
    val provider: Provider? = null,
    val id: String,
    var cover: String? = null,
    val updateTime: Long? = null,
    val source: Source? = null,
    val metadata: MetadataDetail = MetadataDetail(),
    val collections: List<ChapterCollection> = emptyList(),
    var preview: List<String>? = null
) : Parcelable {
    val title
        get() = metadata.title ?: id

    constructor(outline: MangaOutline) : this(
        id = outline.id,
        cover = outline.cover,
        updateTime = outline.updateTime,
        source = outline.source,
        metadata = MetadataDetail(outline.metadata)
    )
}
