package com.fishhawk.driftinglibraryandroid.repository

import com.fishhawk.driftinglibraryandroid.repository.data.*
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryService

class RemoteLibraryRepository(
    var url: String,
    var service: RemoteLibraryService
) {
    suspend fun searchInLibrary(lastId: String, filter: String): Result<List<MangaOutline>> {
        return try {
            service.searchInLibrary(lastId, filter).let {
                for (s in it) {
                    s.thumb = "${url}library/image/${s.id}/${s.thumb}"
                }
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getManga(id: String, source: String? = null): Result<MangaDetail> {
        return try {
            if (source == null) {
                service.getManga(id).let {
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

    suspend fun deleteManga(id: String): Result<String> = resultWrap {
        service.deleteManga(id)
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

    suspend fun searchInSource(
        source: String,
        keywords: String,
        page: Int
    ): Result<List<MangaOutline>> {
        return try {
            service.searchInSource(source, keywords, page).let {
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


    /*
     * download
     */

    suspend fun getAllDownloadTasks(): Result<List<DownloadTask>> = resultWrap {
        service.getAllDownloadTasks()
    }

    suspend fun startAllDownloadTasks(): Result<List<DownloadTask>> = resultWrap {
        service.startAllDownloadTasks()
    }

    suspend fun pauseAllDownloadTasks(): Result<List<DownloadTask>> = resultWrap {
        service.pauseAllDownloadTasks()
    }

    suspend fun postDownloadTask(
        source: String,
        sourceManga: String,
        targetManga: String
    ): Result<DownloadTask> = resultWrap {
        service.postDownloadTask(source, sourceManga, targetManga)
    }

    suspend fun deleteDownloadTask(id: Int): Result<DownloadTask> = resultWrap {
        service.deleteDownloadTask(id)
    }

    suspend fun startDownloadTask(id: Int): Result<DownloadTask> = resultWrap {
        service.startDownloadTask(id)
    }

    suspend fun pauseDownloadTask(id: Int): Result<DownloadTask> = resultWrap {
        service.pauseDownloadTask(id)
    }

    /*
     * subscription
     */

    suspend fun getAllSubscriptions(): Result<List<Subscription>> = resultWrap {
        service.getAllSubscriptions()
    }

    suspend fun enableAllSubscriptions(): Result<List<Subscription>> = resultWrap {
        service.enableAllSubscriptions()
    }

    suspend fun disableAllSubscriptions(): Result<List<Subscription>> = resultWrap {
        service.disableAllSubscriptions()
    }

    suspend fun postSubscription(
        source: String,
        sourceManga: String,
        targetManga: String
    ): Result<Subscription> = resultWrap {
        service.postSubscription(source, sourceManga, targetManga)
    }

    suspend fun deleteSubscription(id: Int): Result<Subscription> = resultWrap {
        service.deleteSubscription(id)
    }

    suspend fun enableSubscription(id: Int): Result<Subscription> = resultWrap {
        service.enableSubscription(id)
    }

    suspend fun disableSubscription(id: Int): Result<Subscription> = resultWrap {
        service.disableSubscription(id)
    }

    private inline fun <T> resultWrap(func: () -> T): Result<T> {
        return try {
            Result.Success(func())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
