package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaKeyDto
import com.fishhawk.lisu.data.remote.model.MangaMetadataDto
import com.fishhawk.lisu.data.remote.service.RemoteLibraryService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit

class RemoteLibraryRepository(retrofit: Flow<Result<Retrofit>?>) :
    BaseRemoteRepository<RemoteLibraryService>(retrofit) {

    override val serviceType = RemoteLibraryService::class.java

    suspend fun search(
        page: Int,
        keywords: String = ""
    ): Result<List<MangaDto>> = resultWrap { server ->
        server.search(page, keywords)
            .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
    }

    suspend fun getRandomManga(): Result<MangaDto> = resultWrap { server ->
        server.getRandomManga()
    }

    suspend fun createManga(
        providerId: String,
        mangaId: String
    ): Result<String> = resultWrap { it.createManga(providerId, mangaId) }

    suspend fun deleteManga(
        providerId: String,
        mangaId: String
    ): Result<String> = resultWrap { it.deleteManga(providerId, mangaId) }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadataDto
    ): Result<String> = resultWrap { it.updateMangaMetadata(providerId, mangaId, metadata) }

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        cover: ByteArray
    ): Result<String> = resultWrap {
        it.updateMangaCover(
            providerId, mangaId,
            cover.toRequestBody("image/png".toMediaType())
        )
    }

    suspend fun deleteMultipleMangas(
        mangas: List<MangaKeyDto>
    ): Result<String> = resultWrap { it.deleteMultipleMangas(mangas) }

    private fun processCover(providerId: String, mangaId: String, cover: String?): String {
        return "${url}provider/${providerId}/manga/${mangaId}/cover?imageId=${cover}"
    }
}