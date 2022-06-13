package com.fishhawk.lisu.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class MangaKeyDto(
    val providerId: String,
    val id: String,
)

@Serializable
enum class MangaState {
    Local, Remote, RemoteInLibrary
}

@Serializable
@Parcelize
data class MangaDto(
    val state: MangaState = MangaState.Local,

    val providerId: String,
    val id: String,

    var cover: String? = null,
    val updateTime: Long? = null,
    val title: String? = null,
    val authors: List<String> = emptyList(),
    val isFinished: Boolean? = null,
) : Parcelable {
    val key
        get() = MangaKeyDto(providerId, id)

    val titleOrId
        get() = title ?: id
}

@Serializable
@Parcelize
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

    val collections: Map<String, List<ChapterDto>> = emptyMap(),
    val chapters: List<ChapterDto> = emptyList(),
    var preview: List<String> = emptyList()
) : Parcelable {
    val titleOrId
        get() = title ?: id
}

fun MangaDto.toDetail() =
    MangaDetailDto(
        state = state,
        providerId = providerId,
        id = id,
        cover = cover,
        updateTime = updateTime,
        title = title,
        authors = authors,
        isFinished = isFinished,
    )