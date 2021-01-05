package com.fishhawk.driftinglibraryandroid.data.remote

import retrofit2.Retrofit
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.data.remote.service.RemoteLibraryService
import okhttp3.RequestBody

class RemoteLibraryRepository : BaseRemoteRepository<RemoteLibraryService>() {
    fun connect(url: String?, builder: Retrofit?) {
        this.url = url
        this.service = builder?.create(RemoteLibraryService::class.java)
    }

    suspend fun search(
        lastTime: Long,
        keywords: String,
        limit: Int = 20
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.search(lastTime, keywords, limit).apply {
                for (outline in this) {
                    outline.thumb =
                        if (outline.thumb.isNullOrBlank()) null
                        else "${url}library/image/${outline.id}/${outline.thumb}"
                }
            }
        }

    suspend fun getManga(mangaId: String): Result<MangaDetail> =
        resultWrap {
            it.getManga(mangaId).apply {
                thumb =
                    if (thumb.isNullOrBlank()) null
                    else "${url}library/image/${mangaId}/${thumb}"
            }
        }

    suspend fun deleteManga(mangaId: String): Result<String> =
        resultWrap { it.deleteManga(mangaId) }

    suspend fun updateMangaMetadata(
        mangaId: String,
        metadata: MetadataDetail
    ): Result<MangaDetail> =
        resultWrap { it.patchMangaMetadata(mangaId, metadata).apply {
            thumb =
                if (thumb.isNullOrBlank()) null
                else "${url}library/image/${mangaId}/${thumb}"
        } }

    suspend fun updateMangaThumb(
        mangaId: String,
        requestBody: RequestBody
    ): Result<MangaDetail> =
        resultWrap { it.patchMangaThumb(mangaId, requestBody).apply {
            thumb =
                if (thumb.isNullOrBlank()) null
                else "${url}library/image/${mangaId}/${thumb}"
        } }

    suspend fun getChapterContent(
        mangaId: String,
        collection: String,
        chapter: String
    ): Result<List<String>> =
        resultWrap { service ->
            service.getChapterContent(mangaId, collection, chapter)
                .map { "${url}library/image/$mangaId/$collection/$chapter/$it" }
        }
}