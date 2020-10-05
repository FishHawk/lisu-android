package com.fishhawk.driftinglibraryandroid.repository.remote

import retrofit2.Retrofit
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.repository.remote.service.RemoteProviderService
import java.net.URLEncoder

class RemoteProviderRepository : BaseRemoteRepository<RemoteProviderService>() {
    fun connect(url: String?, builder: Retrofit?) {
        this.url = url
        this.service = builder?.create(RemoteProviderService::class.java)
    }

    suspend fun getProvidersInfo(): Result<List<ProviderInfo>> =
        resultWrap { it.getProviders() }

    suspend fun getProvidersDetail(providerId: String): Result<ProviderDetail> =
        resultWrap { it.getProviderDetail(providerId) }

    suspend fun search(
        providerId: String,
        keywords: String,
        page: Int
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.search(providerId, keywords, page)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun getPopularMangaList(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.getPopular(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun getLatestMangaList(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.getLatest(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun getCategoryMangaList(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.getCategory(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun getManga(providerId: String, id: String): Result<MangaDetail> =
        resultWrap { it.getManga(providerId, id) }

    suspend fun getChapterContent(
        providerId: String,
        mangaId: String,
        chapterId: String
    ): Result<List<String>> =
        resultWrap { service ->
            service.getChapterContent(providerId, mangaId, chapterId)
                .map { processImageUrl(providerId, it) }
        }

    private fun processMangaOutline(providerId: String, outline: MangaOutline): MangaOutline {
        outline.thumb = outline.thumb?.let { processImageUrl(providerId, it) }
        return outline
    }

    private fun processImageUrl(providerId: String, imageUrl: String): String {
        val encoded = URLEncoder.encode(imageUrl, "UTF-8")
        return "${url}provider/item/${providerId}/image/${encoded}"
    }
}