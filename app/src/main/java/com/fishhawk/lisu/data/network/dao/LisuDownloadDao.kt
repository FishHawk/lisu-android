package com.fishhawk.lisu.data.network.dao

import com.fishhawk.lisu.data.network.model.MangaDownloadTask
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.resources.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
@Resource("/download")
private class Download {
    @Serializable
    @Resource("/start-all")
    data class StartAll(
        val parent: Download = Download(),
    )

    @Serializable
    @Resource("/start-manga/{providerId}/{mangaId}")
    data class StartManga(
        val parent: Download = Download(),
        val providerId: String,
        val mangaId: String,
    )

    @Serializable
    @Resource("/start-chapter/{providerId}/{mangaId}/{collectionId}/{chapterId}")
    data class StartChapter(
        val parent: Download = Download(),
        val providerId: String,
        val mangaId: String,
        val collectionId: String,
        val chapterId: String,
    )

    @Serializable
    @Resource("/cancel-all")
    data class CancelAll(
        val parent: Download = Download(),
    )

    @Serializable
    @Resource("/cancel-manga/{providerId}/{mangaId}")
    data class CancelManga(
        val parent: Download = Download(),
        val providerId: String,
        val mangaId: String,
    )

    @Serializable
    @Resource("/cancel-chapter/{providerId}/{mangaId}/{collectionId}/{chapterId}")
    data class CancelChapter(
        val parent: Download = Download(),
        val providerId: String,
        val mangaId: String,
        val collectionId: String,
        val chapterId: String,
    )
}

class LisuDownloadDao(private val client: HttpClient) {

    suspend fun listMangaDownloadTask(
    ): Flow<List<MangaDownloadTask>> = flow {
        client.webSocket("/download/list") {
            val url = call.request.url
            incoming
                .receiveAsFlow()
                .filterIsInstance<Frame.Text>()
                .collect { frame ->
                    val list = Json.decodeFromString(
                        ListSerializer(MangaDownloadTask.serializer()),
                        frame.readText()
                    ).map {
                        it.copy(
                            cover = client.processCover(
                                url = url,
                                providerId = it.providerId,
                                mangaId = it.mangaId,
                                imageId = it.cover,
                            )
                        )
                    }
                    emit(list)
                }
        }
    }

    suspend fun startAllTasks(
    ): String = client.post(
        Download.StartAll()
    ).body()

    suspend fun startMangaTask(
        providerId: String,
        mangaId: String,
    ): String = client.post(
        Download.StartManga(
            providerId = providerId,
            mangaId = mangaId,
        )
    ).body()

    suspend fun startChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): String = client.post(
        Download.StartChapter(
            providerId = providerId,
            mangaId = mangaId,
            collectionId = collectionId,
            chapterId = chapterId,
        )
    ).body()

    suspend fun cancelAllTasks(
    ): String = client.post(
        Download.CancelAll()
    ).body()

    suspend fun cancelMangaTask(
        providerId: String,
        mangaId: String,
    ): String = client.post(
        Download.CancelManga(
            providerId = providerId,
            mangaId = mangaId,
        )
    ).body()

    suspend fun cancelChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): String = client.post(
        Download.CancelChapter(
            providerId = providerId,
            mangaId = mangaId,
            collectionId = collectionId,
            chapterId = chapterId,
        )
    ).body()
}