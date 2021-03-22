package com.fishhawk.driftinglibraryandroid.data.remote

import retrofit2.Retrofit
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.data.remote.service.RemoteProviderService
import java.net.URLEncoder

class RemoteProviderRepository : BaseRemoteRepository<RemoteProviderService>() {
    fun connect(url: String?, builder: Retrofit?) {
        this.url = url
        this.service = builder?.create(RemoteProviderService::class.java)
    }

    suspend fun listProvider(): Result<List<ProviderInfo>> =
        resultWrap {
            it.listProvider().onEach { info ->
                info.icon = "${url}providers/${info.id}/icon"
            }
        }

    suspend fun getProvider(providerId: String): Result<ProviderDetail> =
        resultWrap { it.getProvider(providerId) }

    suspend fun listPopularManga(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.listPopularManga(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun listLatestManga(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.listLatestManga(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun listCategoryManga(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.listCategoryManga(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun listManga(
        providerId: String,
        keywords: String,
        page: Int
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.listManga(providerId, keywords, page)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun getManga(providerId: String, mangaId: String): Result<MangaDetail> =
        resultWrap { service ->
            service.getManga(providerId, mangaId)
                .apply { thumb = thumb?.let { processImageUrl(providerId, it) } }
        }

    suspend fun getChapterContent(
        providerId: String,
        mangaId: String,
        chapterId: String
    ): Result<List<String>> =
        resultWrap { service ->
            service.getChapter(providerId, mangaId, chapterId)
                .map { processImageUrl(providerId, it) }
        }

    private fun processMangaOutline(providerId: String, outline: MangaOutline): MangaOutline {
        outline.thumb = outline.thumb?.let { processImageUrl(providerId, it) }
        return outline
    }

    private fun processImageUrl(providerId: String, imageUrl: String): String {
        val encoded = URLEncoder.encode(imageUrl, "UTF-8")
        return "${url}providers/${providerId}/images/${encoded}"
    }
}