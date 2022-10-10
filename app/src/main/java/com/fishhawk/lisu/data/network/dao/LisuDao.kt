package com.fishhawk.lisu.data.network.dao

import com.fishhawk.lisu.data.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class LisuDao(
    private val client: HttpClient,
    private val url: String,
) {
    suspend fun listProvider(): List<ProviderDto> =
        client.get("$url/provider")
            .let {
                it.body<List<ProviderDto>>()
                    .map { info -> info.copy(icon = generateProviderIcon(info.id)) }
            }

    suspend fun loginByCookies(
        providerId: String,
        cookies: Map<String, String>,
    ): String =
        client.post("$url/provider/${providerId.path}/login-cookies") {
            setBody(cookies)
        }.body()

    suspend fun loginByPassword(
        providerId: String,
        username: String,
        password: String,
    ): String =
        client.post("$url/provider/${providerId.path}/login-password") {
            parameter("username", username)
            parameter("password", password)
        }.body()

    suspend fun logout(
        providerId: String,
    ): String =
        client.post("$url/provider/${providerId.path}/logout").body()

    suspend fun getBoard(
        providerId: String,
        boardId: String,
        page: Int,
        filterValues: BoardFilterValue,
        keywords: String?,
    ): List<MangaDto> =
        client.get("$url/provider/${providerId.path}/board/${boardId.path}") {
            parameter("page", page)
            keywords?.let { parameter("keywords", it) }
            (filterValues.base + filterValues.advance).forEach {
                val value = it.value.value
                val str = if (value is Set<*>) value.joinToString(",") else value.toString()
                parameter(it.key, str)
            }
        }.let { response ->
            response.body<List<MangaDto>>()
                .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
        }

    suspend fun getManga(
        providerId: String,
        mangaId: String,
    ): MangaDetailDto =
        client.get("$url/provider/${providerId.path}/manga/${mangaId.path}")
            .let { response ->
                val detail = response.body<MangaDetailDto>()
                detail.copy(
                    cover = processCover(detail.providerId, detail.id, detail.cover),
                    chapterPreviews = detail.chapterPreviews.map {
                        processImage(providerId, mangaId, " ", " ", it)
                    },
                )
            }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadataDto,
    ): String =
        client.put("$url/library/manga/${providerId.path}/${mangaId.path}/metadata") {
            setBody(metadata)
        }.body()

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        cover: ByteArray,
        coverType: String,
    ): String =
        client.put("$url/library/manga/${providerId.path}/${mangaId.path}/cover") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("cover", cover, Headers.build {
                            append(HttpHeaders.ContentType, coverType)
                        })
                    }
                )
            )
        }.body()

    suspend fun getComment(
        providerId: String,
        mangaId: String,
        page: Int,
    ): List<CommentDto> =
        client.get("$url/provider/${providerId.path}/manga/${mangaId.path}/comment") {
            parameter("page", page)
        }.body()

    suspend fun getContent(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): List<String> =
        client.get("$url/provider/${providerId.path}/manga/${mangaId.path}/content") {
            parameter("collectionId", collectionId)
            parameter("chapterId", chapterId)
        }.body<List<String>>()
            .map { processImage(providerId, mangaId, collectionId, chapterId, it) }

    suspend fun getRandomMangaFromLibrary(): MangaDto =
        client.get("$url/library/random-manga").body()

    suspend fun addMangaToLibrary(
        providerId: String,
        mangaId: String,
    ): String =
        client.post("$url/library/manga/${providerId.path}/${mangaId.path}").body()

    // Library API
    suspend fun searchFromLibrary(
        page: Int,
        keywords: String = "",
    ): List<MangaDto> =
        client.get("$url/library/search") {
            parameter("page", page)
            parameter("keywords", keywords)
        }.let { response ->
            response.body<List<MangaDto>>()
                .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
        }

    suspend fun removeMangaFromLibrary(
        providerId: String,
        mangaId: String,
    ): String =
        client.delete("$url/library/manga/${providerId.path}/${mangaId.path}").body()

    suspend fun removeMultipleMangasFromLibrary(
        mangas: List<MangaKeyDto>,
    ): String =
        client.post("$url/library/manga-delete") {
            setBody(mangas)
        }.body()


    // Download API
    suspend fun listMangaDownloadTask(): Flow<List<MangaDownloadTask>> = flow {
        client.webSocket(
            urlString = "$url/download/list",
            request = { url.protocol = URLProtocol.WS },
        ) {
            incoming
                .receiveAsFlow()
                .filterIsInstance<Frame.Text>()
                .collect { frame ->
                    val list = Json.decodeFromString(
                        ListSerializer(MangaDownloadTask.serializer()),
                        frame.readText()
                    ).map {
                        it.copy(cover = processCover(it.providerId, it.mangaId, it.cover))
                    }
                    emit(list)
                }
        }
    }


    suspend fun startAllTasks(): String =
        client.post("$url/download/start-all").body()

    suspend fun startMangaTask(
        providerId: String,
        mangaId: String,
    ): String =
        client.post("$url/download/start-manga/${providerId.path}/${mangaId.path}").body()

    suspend fun startChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): String =
        client.post("$url/download/start-chapter/${providerId.path}/${mangaId.path}/${collectionId.path}/${chapterId.path}")
            .body()

    suspend fun cancelAllTasks(): String =
        client.post("$url/download/cancel-all").body()

    suspend fun cancelMangaTask(
        providerId: String,
        mangaId: String,
    ): String =
        client.post("$url/download/cancel-manga/${providerId.path}/${mangaId.path}").body()

    suspend fun cancelChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): String =
        client.post("$url/download/cancel-chapter/${providerId.path}/${mangaId.path}/${collectionId.path}/${chapterId.path}")
            .body()


    private val String.path
        get() = encodeURLParameter()

    private fun generateProviderIcon(
        providerId: String,
    ): String {
        val builder = URLBuilder(url)
        builder.pathSegments = emptyList()
        builder.parameters.clear()
        builder.appendPathSegments(
            "provider",
            providerId.path,
            "icon",
        )
        return builder.build().toString()
    }

    private fun processCover(
        providerId: String,
        mangaId: String,
        cover: String?,
    ): String {
        val builder = URLBuilder(url)
        builder.pathSegments = emptyList()
        builder.parameters.clear()
        builder.appendPathSegments(
            "provider",
            providerId.path,
            "manga",
            mangaId.path,
            "cover",
        )
        builder.parameters.append("imageId", cover ?: "")
        return builder.build().toString()
    }

    private fun processImage(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
        imageId: String,
    ): String {
        val builder = URLBuilder(url)
        builder.pathSegments = emptyList()
        builder.parameters.clear()
        builder.appendPathSegments(
            "provider",
            providerId.path,
            "manga",
            mangaId.path,
            "image",
        )
        builder.parameters.append("collectionId", collectionId)
        builder.parameters.append("chapterId", chapterId)
        builder.parameters.append("imageId", imageId)
        return builder.build().toString()
    }
}