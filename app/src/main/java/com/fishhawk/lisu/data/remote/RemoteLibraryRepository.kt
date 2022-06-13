package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaKeyDto
import com.fishhawk.lisu.data.remote.model.MangaMetadataDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow

class RemoteLibraryRepository(clientFlow: Flow<Result<HttpClient>?>) :
    BaseRemoteRepository(clientFlow) {

    suspend fun search(
        page: Int,
        keywords: String = ""
    ): Result<List<MangaDto>> = resultWrap { client ->
        client.get("/library/search") {
            parameter("page", page)
            parameter("keywords", keywords)
        }.let { response ->
            val url = response.request.url
            response.body<List<MangaDto>>()
                .map { it.copy(cover = processCover(url, it.providerId, it.id, it.cover)) }
        }
    }

    suspend fun getRandomManga(): Result<MangaDto> = resultWrap { client ->
        client.get("/library/random-manga").body()
    }

    suspend fun createManga(
        providerId: String,
        mangaId: String
    ): Result<String> = resultWrap { client ->
        client.post("/library/manga/${providerId.path}/${mangaId.path}").body()
    }

    suspend fun deleteManga(
        providerId: String,
        mangaId: String
    ): Result<String> = resultWrap { client ->
        client.delete("/library/manga/${providerId.path}/${mangaId.path}").body()
    }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadataDto
    ): Result<String> = resultWrap { client ->
        client.put("/library/manga/${providerId.path}/${mangaId.path}/metadata") {
            setBody(metadata)
        }.body()
    }

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        cover: ByteArray,
        coverType: String,
    ): Result<String> = resultWrap { client ->
        client.put("/library/manga/${providerId.path}/${mangaId.path}/cover") {
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
    }

    suspend fun deleteMultipleMangas(
        mangas: List<MangaKeyDto>
    ): Result<String> = resultWrap { client ->
        client.post("/library/manga-delete") {
            setBody(mangas)
        }.body()
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
}