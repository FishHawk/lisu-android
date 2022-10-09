package com.fishhawk.lisu.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaDownloadTask(
    val providerId: String,
    val mangaId: String,
    val cover: String?,
    val title: String?,
    val chapterTasks: List<ChapterDownloadTask>,
)

@Serializable
data class ChapterDownloadTask(
    val collectionId: String,
    val chapterId: String,
    val name: String? = null,
    val title: String? = null,
    var state: State = State.Waiting,
) {
    @Serializable
    sealed interface State {
        @Serializable
        @SerialName("waiting")
        object Waiting : State

        @Serializable
        @SerialName("downloading")
        data class Downloading(
            val downloadedPageNumber: Int?,
            val totalPageNumber: Int?,
        ) : State

        @Serializable
        @SerialName("failed")
        data class Failed(
            val downloadedPageNumber: Int?,
            val totalPageNumber: Int?,
            val errorMessage: String,
        ) : State
    }
}