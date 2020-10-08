package com.fishhawk.driftinglibraryandroid.repository.remote

import retrofit2.Retrofit
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.service.RemoteLibraryService
import okhttp3.MultipartBody
import java.io.File
import okhttp3.RequestBody.Companion.asRequestBody

class RemoteLibraryRepository : BaseRemoteRepository<RemoteLibraryService>() {
    fun connect(url: String?, builder: Retrofit?) {
        this.url = url
        this.service = builder?.create(RemoteLibraryService::class.java)
    }

    suspend fun search(
        lastTime: Long,
        filter: String,
        limit: Int = 20
    ): Result<List<MangaOutline>> =
        resultWrap {
            it.search(lastTime, filter, limit).apply {
                for (outline in this) {
                    outline.thumb = "${url}library/image/${outline.id}/${outline.thumb}"
                }
            }
        }

    suspend fun getManga(mangaId: String): Result<MangaDetail> =
        resultWrap {
            it.getManga(mangaId).apply { thumb = "${url}library/image/${mangaId}/${thumb}" }
        }

    suspend fun deleteManga(mangaId: String): Result<String> =
        resultWrap { it.deleteManga(mangaId) }

    suspend fun updateMangaMetadata(
        mangaId: String,
        metadata: MetadataDetail
    ): Result<MangaDetail> =
        resultWrap { it.patchMangaMetadata(mangaId, metadata) }

    suspend fun updateMangaThumb(
        mangaId: String,
        file: File
    ): Result<MangaDetail> =
        resultWrap { it.patchMangaThumb(mangaId, MultipartBody.Part.create(file.asRequestBody())) }

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