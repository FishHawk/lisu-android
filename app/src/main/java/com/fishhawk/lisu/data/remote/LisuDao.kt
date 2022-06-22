package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class LisuDao(
    private val client: HttpClient,
    private val url: String
) {
    suspend fun listProvider(): List<ProviderDto> =
        client.get("$url/provider")
            .let {
                it.body<List<ProviderDto>>()
                    .map { info -> info.copy(icon = generateProviderIcon(info.id)) }
            }

    suspend fun login(
        providerId: String,
        cookies: Map<String, String>,
    ): String =
        client.post("$url/provider/${providerId.path}/login") { setBody(cookies) }.body()

    suspend fun logout(
        providerId: String,
    ): String =
        client.post("$url/provider/${providerId.path}/logout").body()

    suspend fun search(
        providerId: String,
        page: Int,
        keywords: String,
    ): List<MangaDto> =
        client.get("$url/provider/${providerId.path}/search") {
            parameter("page", page)
            parameter("keywords", keywords)
        }.let { response ->
            response.body<List<MangaDto>>()
                .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
        }

    suspend fun getBoard(
        providerId: String,
        boardId: String,
        page: Int,
        filters: Map<String, Int>
    ): List<MangaDto> =
        client.get("$url/provider/${providerId.path}/board/${boardId.path}") {
            parameter("page", page)
            filters.forEach { (key, value) -> parameter(key, value) }
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
                    preview = detail.preview.map {
                        processImage(providerId, mangaId, " ", " ", it)
                    }
                )
            }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadataDto
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
        chapterId: String
    ): List<String> =
        client.get("$url/provider/${providerId.path}/manga/${mangaId.path}/content/${collectionId.path}/${chapterId.path}")
            .let { response ->
                response.body<List<String>>()
                    .map { processImage(providerId, mangaId, collectionId, chapterId, it) }
            }

    suspend fun getRandomMangaFromLibrary(): MangaDto =
        client.get("$url/library/random-manga").body()

    suspend fun addMangaToLibrary(
        providerId: String,
        mangaId: String
    ): String =
        client.post("$url/library/manga/${providerId.path}/${mangaId.path}").body()

    // Library API
    suspend fun searchFromLibrary(
        page: Int,
        keywords: String = ""
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
        mangaId: String
    ): String =
        client.delete("$url/library/manga/${providerId.path}/${mangaId.path}").body()

    suspend fun removeMultipleMangasFromLibrary(
        mangas: List<MangaKeyDto>
    ): String =
        client.post("$url/library/manga-delete") {
            setBody(mangas)
        }.body()


    private val String.path
        get() = encodeURLPath()

    private fun generateProviderIcon(
        providerId: String,
    ): String {
        val builder = URLBuilder(url)
        builder.pathSegments = emptyList()
        builder.parameters.clear()
        builder.appendPathSegments("provider", providerId, "icon")
        return builder.build().toString()
    }

    private fun processCover(
        providerId: String,
        mangaId: String,
        cover: String?
    ): String {
        val builder = URLBuilder(url)
        builder.pathSegments = emptyList()
        builder.parameters.clear()
        builder.appendPathSegments("provider", providerId, "manga", mangaId, "cover")
        builder.parameters.append("imageId", cover ?: "")
        return builder.build().toString()
    }

    private fun processImage(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
        imageId: String
    ): String {
        val builder = URLBuilder(url)
        builder.pathSegments = emptyList()
        builder.parameters.clear()
        builder.appendPathSegments(
            "provider",
            providerId,
            "manga",
            mangaId,
            "image",
            collectionId,
            chapterId,
            imageId
        )
        builder.parameters.append("imageId", imageId)
        return builder.build().toString()
    }
}
