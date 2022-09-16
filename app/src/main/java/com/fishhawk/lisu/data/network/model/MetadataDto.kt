package com.fishhawk.lisu.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MangaMetadataDto(
    val title: String? = null,
    val authors: List<String>? = null,
    val isFinished: Boolean? = null,
    val description: String? = null,
    val tags: Map<String, List<String>> = emptyMap(),

    val collections: Map<String, Map<String, ChapterMetadataDto>>? = null,
    val chapters: Map<String, ChapterMetadataDto>? = null,
)

@Serializable
data class ChapterMetadataDto(
    val name: String?,
    val title: String?,
)

fun MangaDetailDto.toMetadataDetail(): MangaMetadataDto {
    return MangaMetadataDto(
        title = title,
        authors = authors,
        isFinished = isFinished,
        description = description,
        tags = tags,
    )
}