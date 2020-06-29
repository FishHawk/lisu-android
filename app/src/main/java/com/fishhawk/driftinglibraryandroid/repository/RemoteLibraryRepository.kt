package com.fishhawk.driftinglibraryandroid.repository

import com.fishhawk.driftinglibraryandroid.repository.data.*
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryService

class RemoteLibraryRepository(
    var url: String,
    var service: RemoteLibraryService
) {
    suspend fun getMangaList(lastId: String, filter: String): Result<List<MangaOutline>> {
        return try {
            service.getMangaList(lastId, filter).let {
                for (s in it) {
                    s.thumb = "${url}library/image/${s.id}/${s.thumb}"
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
                    it.thumb = "${url}library/image/${it.id}/${it.thumb}"
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
                    Result.Success(it.map { "${url}library/image/$id/$collection/$chapter/$it" })
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

    suspend fun getSources(): Result<List<Source>> {
        return try {
            service.getSources().let {
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun search(
        source: String,
        keywords: String,
        page: Int
    ): Result<List<MangaOutline>> {
        return try {
            service.search(source, keywords, page).let {
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getPopularMangaList(
        source: String,
        page: Int
    ): Result<List<MangaOutline>> {
        return try {
            service.getPopular(source, page).let {
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getLatestMangaList(
        source: String,
        page: Int
    ): Result<List<MangaOutline>> {
        return try {
            service.getLatest(source, page).let {
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getSubscriptions(): Result<List<Subscription>> {
        return try {
            service.getSubscriptions().let { Result.Success(it) }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun postSubscription(
        source: String,
        sourceManga: String,
        targetManga: String
    ): Result<Subscription> {
        return try {
            service.postSubscription(source, sourceManga, targetManga).let { Result.Success(it) }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun deleteSubscription(id: Int): Result<Subscription> {
        return try {
            service.deleteSubscription(id).let { Result.Success(it) }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }
}
