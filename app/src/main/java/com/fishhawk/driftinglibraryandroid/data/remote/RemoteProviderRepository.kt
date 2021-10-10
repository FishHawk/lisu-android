package com.fishhawk.driftinglibraryandroid.data.remote

import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetailDto
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDto
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataDto
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.data.remote.service.RemoteProviderService
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.Retrofit
import java.net.URLEncoder

class RemoteProviderRepository(retrofit: Flow<Result<Retrofit>?>) :
    BaseRemoteRepository<RemoteProviderService>(retrofit) {

    override val serviceType = RemoteProviderService::class.java

    suspend fun listProvider(): Result<List<Provider>> = resultWrap {
        it.listProvider().map { info ->
            info.copy(icon = "${url}provider/${info.id}/icon")
        }
    }

    suspend fun getBoard(
        providerId: String,
        boardId: String,
        page: Int,
        filters: Map<String, Int>
    ): Result<List<MangaDto>> = resultWrap { service ->
        service.getBoard(providerId, boardId, page, filters)
            .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
    }

    suspend fun search(
        providerId: String,
        page: Int,
        keywords: String,
    ): Result<List<MangaDto>> = resultWrap { service ->
        service.search(providerId, page, keywords)
            .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
    }

    suspend fun getManga(
        providerId: String,
        mangaId: String
    ): Result<MangaDetailDto> = resultWrap { service ->
        service.getManga(providerId, mangaId).let { detail ->
            detail.copy(
                cover = processCover(detail.providerId, detail.id, detail.cover),
                preview = detail.preview?.map {
                    processImage(providerId, mangaId, " ", " ", it)
                }
            )
        }
    }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MetadataDto
    ): Result<String> = resultWrap { it.updateMangaMetadata(providerId, mangaId, metadata) }

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        requestBody: RequestBody
    ): Result<String> = resultWrap { it.updateMangaCover(providerId, mangaId, requestBody) }

    suspend fun getContent(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String
    ): Result<List<String>> = resultWrap { service ->
        service.getContent(providerId, mangaId, collectionId, chapterId)
            .map { processImage(providerId, mangaId, collectionId, chapterId, it) }
    }

    private fun processImage(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
        imageId: String
    ): String {
        val encoded = URLEncoder.encode(imageId, "UTF-8")
        return "${url}provider/${providerId}/manga/${mangaId}/image/${collectionId}/${chapterId}/${encoded}"
    }

    private fun processCover(providerId: String, mangaId: String, cover: String?): String {
        val imageId = cover?.let { URLEncoder.encode(it, "UTF-8") }
        return "${url}provider/${providerId}/manga/${mangaId}/cover?imageId=${imageId}"
    }
}