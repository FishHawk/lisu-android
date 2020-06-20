package com.fishhawk.driftinglibraryandroid.repository

import com.fishhawk.driftinglibraryandroid.repository.service.RemoteLibraryService
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail

class RemoteLibraryRepository(
    var url: String,
    var service: RemoteLibraryService
) {
    suspend fun getMangaList(lastId: String, filter: String): Result<List<MangaSummary>> {
        return try {
            service.getMangaList(lastId, filter).let {
                for (s in it) {
                    s.thumb = "${url}image/${s.id}/${s.thumb}"
                }
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getMangaDetail(id: String, source: String? = null): Result<MangaDetail> {
        return try {
            if (source == null) {
                service.getMangaDetail(id).let {
                    it.thumb = "${url}image/${it.id}/${it.thumb}"
                    Result.Success(it)
                }
            } else {
                service.getMangaDetail(source, id).let {
                    Result.Success(it)
                }
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getChapterContent(
        id: String,
        collection: String,
        chapter: String,
        source: String? = null
    ): Result<List<String>> {
        return try {
            if (source == null) {
                service.getChapterContent(id, collection, chapter).let {
                    Result.Success(it.map { "${url}image/$id/$collection/$chapter/$it" })
                }
            } else {
                service.getChapterContent(source, chapter).let {
                    Result.Success(it.map { "${url}$it" })
                }
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun search(
        source: String,
        keywords: String,
        page: Int
    ): Result<List<MangaSummary>> {
        return try {
            service.search(source, keywords, page).let {
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }
}
