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

    suspend fun listManga(
        lastTime: Long,
        keywords: String,
        limit: Int = 20
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.listManga(lastTime, keywords, limit).onEach { outline ->
                outline.thumb = "${url}library/mangas/${outline.id}/thumb"
            }
        }

    suspend fun createManga(
        mangaId: String,
        providerId: String,
        sourceMangaId: String,
        shouldDeleteAfterUpdated: Boolean
    ) = resultWrap {
        it.createManga(
            mangaId,
            providerId,
            sourceMangaId,
            shouldDeleteAfterUpdated
        )
    }

    suspend fun getManga(mangaId: String): Result<MangaDetail> =
        resultWrap {
            it.getManga(mangaId).apply {
                thumb = "${url}library/mangas/${mangaId}/thumb"
            }
        }

    suspend fun deleteManga(mangaId: String): Result<String> =
        resultWrap { it.deleteManga(mangaId) }

    suspend fun updateMangaMetadata(
        mangaId: String,
        metadata: MetadataDetail
    ): Result<MangaDetail> =
        resultWrap {
            it.updateMangaMetadata(mangaId, metadata).apply {
                thumb = "${url}library/mangas/${mangaId}/thumb"
            }
        }

    suspend fun createMangaSource(
        mangaId: String,
        providerId: String,
        sourceMangaId: String,
        shouldDeleteAfterUpdated: Boolean
    ) = resultWrap {
        it.createMangaSource(
            mangaId,
            providerId,
            sourceMangaId,
            shouldDeleteAfterUpdated
        )
    }

    suspend fun deleteMangaSource(mangaId: String) = resultWrap {
        it.deleteMangaSource(mangaId)
    }

    suspend fun syncMangaSource(mangaId: String) = resultWrap {
        it.syncMangaSource(mangaId)
    }

    suspend fun updateMangaThumb(
        mangaId: String,
        requestBody: RequestBody
    ): Result<MangaDetail> =
        resultWrap {
            it.updateMangaThumb(mangaId, requestBody).apply {
                thumb = "${url}library/mangas/${mangaId}/thumb"
            }
        }

    suspend fun getChapterContent(
        mangaId: String,
        collectionId: String,
        chapterId: String
    ): Result<List<String>> =
        resultWrap { service ->
            service.getChapter(mangaId, collectionId, chapterId)
                .map { "${url}library/mangas/$mangaId/images/$collectionId/$chapterId/$it" }
        }
}