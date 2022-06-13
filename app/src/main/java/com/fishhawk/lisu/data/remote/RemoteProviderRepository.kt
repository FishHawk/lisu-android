package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.CommentDto
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.ProviderDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow

class RemoteProviderRepository(client: Flow<Result<HttpClient>?>) :
    BaseRemoteRepository(client) {

    private var cachedProviderList: List<ProviderDto>? = null

    suspend fun listProvider(): Result<List<ProviderDto>> = resultWrap { client ->
        cachedProviderList ?: client.get("/provider")
            .let {
                val url = it.request.url
                it.body<List<ProviderDto>>()
                    .map { info -> info.copy(icon = generateProviderIcon(url, info.id)) }
                    .also { cachedProviderList = it }
            }
    }

    fun getProvider(id: String): ProviderDto = cachedProviderList?.find { it.id == id }!!

    suspend fun login(
        providerId: String,
        cookies: Map<String, String>,
    ): Result<String> = resultWrap { client ->
        client.post("/provider/${providerId.path}/login") { setBody(cookies) }.body()
    }

    suspend fun logout(
        providerId: String,
    ): Result<String> = resultWrap { client ->
        client.post("/provider/${providerId.path}/logout").body()
    }

    suspend fun search(
        providerId: String,
        page: Int,
        keywords: String,
    ): Result<List<MangaDto>> = resultWrap { client ->
        client.get("/provider/${providerId.path}/search") {
            parameter("page", page)
            parameter("keywords", keywords)
        }.let { response ->
            val url = response.request.url
            response.body<List<MangaDto>>()
                .map { it.copy(cover = processCover(url, it.providerId, it.id, it.cover)) }
        }
    }

    suspend fun getBoard(
        providerId: String,
        boardId: String,
        page: Int,
        filters: Map<String, Int>
    ): Result<List<MangaDto>> = resultWrap { client ->
        client.get("/provider/${providerId.path}/board/${boardId.path}") {
            parameter("page", page)
            filters.forEach { (key, value) -> parameter(key, value) }
        }.let { response ->
            val url = response.request.url
            response.body<List<MangaDto>>()
                .map { it.copy(cover = processCover(url, it.providerId, it.id, it.cover)) }
        }
    }

    suspend fun getManga(
        providerId: String,
        mangaId: String
    ): Result<MangaDetailDto> = resultWrap { client ->
        client.get("/provider/${providerId.path}/manga/${mangaId.path}")
            .let { response ->
                val url = response.request.url
                val detail = response.body<MangaDetailDto>()
                detail.copy(
                    cover = processCover(url, detail.providerId, detail.id, detail.cover),
                    preview = detail.preview?.map {
                        processImage(url, providerId, mangaId, " ", " ", it)
                    }
                )
            }
    }

    suspend fun getComment(
        providerId: String,
        mangaId: String,
        page: Int,
    ): Result<List<CommentDto>> = resultWrap { client ->
        client.get("/provider/${providerId.path}/manga/${mangaId.path}/comment") {
            parameter("page", page)
        }.body()
    }

    suspend fun getContent(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String
    ): Result<List<String>> = resultWrap { client ->
        client.get("/provider/${providerId.path}/manga/${mangaId.path}/content/${collectionId.path}/${chapterId.path}")
            .let { response ->
                val url = response.request.url
                response.body<List<String>>()
                    .map { processImage(url, providerId, mangaId, collectionId, chapterId, it) }
            }
    }

    private fun generateProviderIcon(
        url: Url,
        providerId: String,
    ): String {
        val builder = URLBuilder(url)
        builder.pathSegments = emptyList()
        builder.parameters.clear()
        builder.appendPathSegments("provider", providerId, "icon")
        return builder.build().toString()
    }

    private fun processCover(
        url: Url,
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
        url: Url,
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