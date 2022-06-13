package com.fishhawk.lisu.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class MangaMetadataDto(
    val title: String? = null,
    val authors: List<String>? = null,
    val isFinished: Boolean? = null,
    val description: String? = null,
    val tags: Map<String, List<String>> = emptyMap(),

    val collections: Map<String, Map<String, ChapterMetadataDto>>? = null,
    val chapters: Map<String, ChapterMetadataDto>? = null,
) : Parcelable

@Serializable
@Parcelize
data class ChapterMetadataDto(
    val name: String?,
    val title: String?,
) : Parcelable

fun MangaDetailDto.toMetadataDetail(): MangaMetadataDto {
    return MangaMetadataDto(
        title = title,
        authors = authors,
        isFinished = isFinished,
        description = description,
        tags = tags,
    )
}