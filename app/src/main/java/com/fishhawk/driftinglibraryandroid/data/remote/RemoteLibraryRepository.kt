package com.fishhawk.driftinglibraryandroid.data.remote

import retrofit2.Retrofit
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
    ): ResultX<List<MangaOutline>> =
        resultWrap {
            it.listManga(lastTime, keywords, limit).onEach { outline ->
                outline.cover = "${url}library/mangas/${outline.id}/cover"
            }
        }

    suspend fun createManga(
        mangaId: String,
        providerId: String,
        sourceMangaId: String,
        keepAfterCompleted: Boolean
    ) = resultWrap {
        it.createManga(
            RemoteLibraryService.CreateMangaBody(
                mangaId,
                providerId,
                sourceMangaId,
                keepAfterCompleted
            )
        )
    }

    suspend fun getManga(mangaId: String): ResultX<MangaDetail> =
        resultWrap {
            it.getManga(mangaId).apply {
                val collectionId = collections.firstOrNull()?.id
                val chapterId = collections.firstOrNull()?.chapters?.firstOrNull()?.id
                cover = "${url}library/mangas/${mangaId}/cover"
                preview = preview?.map { imageId ->
                    makeImageUrl(mangaId, collectionId, chapterId, imageId)
                }
            }
        }

    suspend fun deleteManga(mangaId: String): ResultX<String> =
        resultWrap { it.deleteManga(mangaId) }

    suspend fun updateMangaMetadata(
        mangaId: String,
        metadata: MetadataDetail
    ): ResultX<MangaDetail> =
        resultWrap {
            it.updateMangaMetadata(mangaId, metadata).apply {
                cover = "${url}library/mangas/${mangaId}/cover"
            }
        }

    suspend fun createMangaSource(
        mangaId: String,
        providerId: String,
        sourceMangaId: String,
        keepAfterCompleted: Boolean
    ) = resultWrap {
        it.createMangaSource(
            mangaId,
            RemoteLibraryService.CreateMangaSourceBody(
                providerId,
                sourceMangaId,
                keepAfterCompleted
            )
        )
    }

    suspend fun deleteMangaSource(mangaId: String) = resultWrap {
        it.deleteMangaSource(mangaId)
    }

    suspend fun syncMangaSource(mangaId: String) = resultWrap {
        it.syncMangaSource(mangaId)
    }

    suspend fun updateMangaCover(
        mangaId: String,
        requestBody: RequestBody
    ): ResultX<MangaDetail> =
        resultWrap {
            it.updateMangaCover(mangaId, requestBody).apply {
                cover = "${url}library/mangas/${mangaId}/cover"
            }
        }

    suspend fun getChapterContent(
        mangaId: String,
        collectionId: String,
        chapterId: String
    ): ResultX<List<String>> =
        resultWrap { service ->
            val content =
                if (collectionId.isBlank())
                    if (chapterId.isBlank()) service.getChapter(mangaId)
                    else service.getChapter(mangaId, chapterId)
                else service.getChapter(mangaId, collectionId, chapterId)
            content.map { makeImageUrl(mangaId, collectionId, chapterId, it) }
        }

    private fun makeImageUrl(
        mangaId: String,
        collectionId: String?,
        chapterId: String?,
        imageId: String
    ): String {
        val collectionPath = if (collectionId.isNullOrBlank()) "" else "$collectionId/"
        val chapterPath = if (chapterId.isNullOrBlank()) "" else "$chapterId/"
        return "${url}library/mangas/$mangaId/images/$collectionPath$chapterPath$imageId"
    }
}