package com.fishhawk.lisu.data.network.dao

import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.MangaKeyDto
import com.fishhawk.lisu.data.network.model.MangaMetadata
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/library")
private class Library {
    @Serializable
    @Resource("/search")
    data class Search(
        val parent: Library = Library(),
        val page: Int,
        val keywords: String,
    )

    @Serializable
    @Resource("/manga-delete")
    data class MangaDelete(
        val parent: Library = Library(),
    )

    @Serializable
    @Resource("/random-manga")
    data class RandomManga(
        val parent: Library = Library(),
    )

    @Serializable
    @Resource("/manga/{providerId}/{mangaId}")
    data class Manga(
        val parent: Library = Library(),
        val providerId: String,
        val mangaId: String,
    )

    @Serializable
    @Resource("/manga/{providerId}/{mangaId}/cover")
    data class Cover(
        val parent: Library = Library(),
        val providerId: String,
        val mangaId: String,
    )

    @Serializable
    @Resource("/manga/{providerId}/{mangaId}/metadata")
    data class Metadata(
        val parent: Library = Library(),
        val providerId: String,
        val mangaId: String,
    )
}

class LisuLibraryDao(private val client: HttpClient) {

    suspend fun search(
        page: Int,
        keywords: String = "",
    ): List<MangaDto> = client.get(
        Library.Search(
            page = page,
            keywords = keywords,
        )
    ).run {
        body<List<MangaDto>>().map {
            it.copy(
                cover = client.processCover(
                    url = request.url,
                    providerId = it.providerId,
                    mangaId = it.id,
                    imageId = it.cover,
                )
            )
        }
    }

    suspend fun removeMultipleMangas(
        mangas: List<MangaKeyDto>,
    ): String = client.post(
        Library.MangaDelete()
    ) {
        contentType(ContentType.Application.Json)
        setBody(mangas)
    }.body()

    suspend fun getRandomManga(): MangaDto = client.get(
        Library.RandomManga()
    ).body()

    suspend fun addManga(
        providerId: String,
        mangaId: String,
    ): String = client.post(
        Library.Manga(
            providerId = providerId,
            mangaId = mangaId,
        )
    ).body()

    suspend fun removeManga(
        providerId: String,
        mangaId: String,
    ): String = client.delete(
        Library.Manga(
            providerId = providerId,
            mangaId = mangaId,
        )
    ).body()

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        cover: ByteArray,
        coverType: String,
    ): String = client.put(
        Library.Cover(
            providerId = providerId,
            mangaId = mangaId,
        )
    ) {
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

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadata,
    ): MangaMetadata = client.put(
        Library.Metadata(
            providerId = providerId,
            mangaId = mangaId,
        )
    ) {
        contentType(ContentType.Application.Json)
        setBody(metadata)
    }.body()
}