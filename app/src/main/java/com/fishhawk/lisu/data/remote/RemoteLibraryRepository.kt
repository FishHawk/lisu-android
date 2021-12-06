package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.service.RemoteLibraryService
import kotlinx.coroutines.flow.Flow
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

    private fun processCover(providerId: String, mangaId: String, cover: String?): String {
        return "${url}provider/${providerId}/manga/${mangaId}/cover?imageId=${cover}"
    }
}