package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.data.remote.service.RemoteProviderService
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import java.net.URLEncoder

class RemoteProviderRepository(retrofit: Flow<Result<Retrofit>?>) :
    BaseRemoteRepository<RemoteProviderService>(retrofit) {

    override val serviceType = RemoteProviderService::class.java

    private var cachedProviderList: List<ProviderDto>? = null

    suspend fun listProvider(): Result<List<ProviderDto>> = resultWrap { service ->
        cachedProviderList ?: service.listProvider()
            .map { info -> info.copy(icon = "${url}provider/${info.id}/icon") }
            .also { cachedProviderList = it }
    }

    fun getProvider(id: String): ProviderDto = cachedProviderList?.find { it.id == id }!!

    suspend fun getBoard(
        providerId: String,
        boardId: String,
        page: Int,
        filters: Map<String, Int>
    ): Result<List<MangaDto>> = resultWrap { service ->
        service.getBoard(providerId, boardId, page, filters)
            .map { it.copy(cover = processCover(it.providerId, it.id, it.cover)) }
    }

    suspend fun login(
        providerId: String,
        cookies: Map<String, String>,
    ): Result<String> = resultWrap { service ->
        service.login(providerId, cookies)
    }

    suspend fun logout(
        providerId: String,
    ): Result<String> = resultWrap { service ->
        service.logout(providerId)
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
        return "${url}provider/${providerId}/manga/${mangaId}/cover?imageId=${cover}"
    }
}