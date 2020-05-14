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

    suspend fun getMangaDetail(id: String): Result<MangaDetail> {
        return try {
            service.getMangaDetail(id).let {
                it.thumb = "${url}image/${it.id}/${it.thumb}"
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getChapterContent(
        id: String,
        collection: String,
        chapter: String
    ): Result<List<String>> {
        return try {
            service.getChapterContent(id, collection, chapter).let {
                Result.Success(it.map { "${url}image/$id/$collection/$chapter/$it" })
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }
}
