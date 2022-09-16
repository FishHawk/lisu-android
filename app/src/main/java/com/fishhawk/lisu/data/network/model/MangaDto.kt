package com.fishhawk.lisu.data.network.model

import kotlinx.serialization.SerialName
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

    fun toDetail() = MangaDetailDto(
        state = state,
        providerId = providerId,
        id = id,
        cover = cover,
        updateTime = updateTime,
        title = title,
        authors = authors,
        isFinished = isFinished,
        content = MangaContent.SingleChapter(),
    )
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

    val content: MangaContent,
) {
    val titleOrId
        get() = title ?: id
}


@Serializable
sealed interface MangaContent {
    @Serializable
    @SerialName("Collections")
    data class Collections(
        val collections: Map<String, List<Chapter>> = emptyMap(),
    ) : MangaContent {
        fun isEmpty() = collections.values.all { it.isEmpty() }
        fun firstOrNull(): Pair<String, Chapter>? {
            return collections.entries
                .firstOrNull { it.value.isNotEmpty() }
                ?.let { (collectionId, chapters) ->
                    collectionId to chapters.first()
                }
        }
    }

    @Serializable
    @SerialName("Chapters")
    data class Chapters(val chapters: List<Chapter> = emptyList()) : MangaContent {
        fun isEmpty() = chapters.isEmpty()
    }

    @Serializable
    @SerialName("SingleChapter")
    data class SingleChapter(var preview: List<String> = emptyList()) : MangaContent {
        fun isEmpty() = preview.isEmpty()
    }
}

@Serializable
data class Chapter(
    val id: String,
    val name: String? = null,
    val title: String? = null,
    val updateTime: Long? = null,
    val isLocked: Boolean = false,
)