package com.fishhawk.lisu.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MangaKeyDto(
    val providerId: String,
    val id: String,
)

enum class MangaState {
    Local, Remote, RemoteInLibrary
}

@Serializable
data class MangaDto(
    val state: MangaState = MangaState.Local,
    val providerId: String,

    val id: String,

    var cover: String? = null,
    val updateTime: Long? = null,
    val title: String? = null,
    val authors: List<String> = emptyList(),
    val isFinished: Boolean? = null,
) {
    val key
        get() = MangaKeyDto(providerId, id)

    val titleOrId
        get() = title ?: id
}

@Serializable
data class MangaDetailDto(
    val state: MangaState = MangaState.Local,
    val providerId: String,

    val id: String,

    var cover: String? = null,
    val updateTime: Long? = null,
    val title: String? = null,
    val authors: List<String> = emptyList(),
    val isFinished: Boolean? = null,

    val description: String? = null,
    val tags: Map<String, List<String>> = emptyMap(),

    val collections: Map<String, List<Chapter>> = emptyMap(),
    val chapterPreviews: List<String> = emptyList(),
)

@Serializable
data class Chapter(
    val id: String,
    val name: String? = null,
    val title: String? = null,
    val updateTime: Long? = null,
    val isLocked: Boolean = false,
)