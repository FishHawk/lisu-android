package com.fishhawk.lisu.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MangaMetadata(
    val title: String? = null,
    val authors: List<String> = emptyList(),
    val isFinished: Boolean? = null,
    val description: String? = null,
    val tags: Map<String, List<String>> = emptyMap(),
)

fun MangaDetailDto.toMetadataDetail(): MangaMetadata {
    return MangaMetadata(
        title = title,
        authors = authors,
        isFinished = isFinished,
        description = description,
        tags = tags,
    )
}