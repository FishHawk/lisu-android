package com.fishhawk.driftinglibraryandroid.data.remote

import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDto
import com.fishhawk.driftinglibraryandroid.data.remote.service.RemoteLibraryService
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import java.net.URLEncoder

class RemoteLibraryRepository(retrofit: Flow<Result<Retrofit>?>) :
    BaseRemoteRepository<RemoteLibraryService>(retrofit) {

    override val serviceType = RemoteLibraryService::class.java

    suspend fun subscribe(
        providerId: String,
        mangaId: String
    ): Result<String> = resultWrap { it.subscribe(providerId, mangaId) }

    suspend fun unsubscribe(
        providerId: String,
        mangaId: String
    ): Result<String> = resultWrap { it.unsubscribe(providerId, mangaId) }

    suspend fun search(
        page: Int,
        keywords: String
    ): Result<List<MangaDto>> = resultWrap { server ->
        server.search(page, keywords)
            .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
    }

    private fun processCover(providerId: String, mangaId: String, cover: String?): String {
        val imageId = cover?.let { URLEncoder.encode(it, "UTF-8") }
        return "${url}provider/${providerId}/manga/${mangaId}/cover?imageId=${imageId}"
    }
}