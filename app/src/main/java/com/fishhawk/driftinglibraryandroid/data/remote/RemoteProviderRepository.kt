package com.fishhawk.driftinglibraryandroid.data.remote

import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.data.remote.service.RemoteProviderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import retrofit2.Retrofit
import java.net.URLEncoder

class RemoteProviderRepository(retrofit: StateFlow<Retrofit?>) :
    BaseRemoteRepository<RemoteProviderService>(retrofit) {

    override val serviceFlow = retrofit.map {
        it?.create(RemoteProviderService::class.java)
    }.stateIn(
        CoroutineScope(SupervisorJob() + Dispatchers.Main),
        SharingStarted.Eagerly, null
    )

    suspend fun listProvider(): ResultX<List<ProviderInfo>> =
        resultWrap {
            it.listProvider().onEach { info ->
                info.icon = "${url}providers/${info.id}/icon"
            }
        }

    suspend fun getProvider(providerId: String): ResultX<ProviderDetail> =
        resultWrap { it.getProvider(providerId) }

    suspend fun listPopularManga(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): ResultX<List<MangaOutline>> =
        resultWrap {
            it.listPopularManga(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun listLatestManga(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): ResultX<List<MangaOutline>> =
        resultWrap {
            it.listLatestManga(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun listCategoryManga(
        providerId: String,
        page: Int,
        option: Map<String, Int>
    ): ResultX<List<MangaOutline>> =
        resultWrap {
            it.listCategoryManga(providerId, page, option)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun listManga(
        providerId: String,
        keywords: String,
        page: Int
    ): ResultX<List<MangaOutline>> =
        resultWrap {
            it.listManga(providerId, keywords, page)
                .map { outline -> processMangaOutline(providerId, outline) }
        }

    suspend fun getManga(providerId: String, mangaId: String): ResultX<MangaDetail> =
        resultWrap { service ->
            service.getManga(providerId, mangaId)
                .apply { cover = cover?.let { processImageUrl(providerId, it) } }
        }

    suspend fun getChapterContent(
        providerId: String,
        mangaId: String,
        chapterId: String
    ): ResultX<List<String>> =
        resultWrap { service ->
            service.getChapter(providerId, mangaId, chapterId)
                .map { processImageUrl(providerId, it) }
        }

    private fun processMangaOutline(providerId: String, outline: MangaOutline): MangaOutline {
        outline.cover = outline.cover?.let { processImageUrl(providerId, it) }
        return outline
    }

    private fun processImageUrl(providerId: String, imageUrl: String): String {
        val encoded = URLEncoder.encode(imageUrl, "UTF-8")
        return "${url}providers/${providerId}/images/${encoded}"
    }
}