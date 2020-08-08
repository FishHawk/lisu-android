package com.fishhawk.driftinglibraryandroid.repository

import com.fishhawk.driftinglibraryandroid.repository.data.*
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryService

class RemoteLibraryRepository {
    var url: String? = null
    var service: RemoteLibraryService? = null

    private inline fun <T> resultWrap(func: (RemoteLibraryService) -> T): Result<T> {
        return service?.let {
            try {
                Result.Success(func(service!!))
            } catch (e: Exception) {
                Result.Error(e)
            }
        } ?: Result.Error(IllegalAccessError())
    }

    suspend fun searchInLibrary(lastTime: Long?, filter: String): Result<List<MangaOutline>> =
        resultWrap {
            it.searchInLibrary(lastTime, filter).apply {
                for (outline in this) {
                    outline.thumb = "${url}library/image/${outline.id}/${outline.thumb}"
                }
            }
        }

    suspend fun getMangaFromLibrary(id: String): Result<MangaDetail> =
        resultWrap {
            it.getMangaFromLibrary(id).apply { thumb = "${url}library/image/${id}/${thumb}" }
        }

    suspend fun deleteMangaFromLibrary(id: String): Result<String> =
        resultWrap { it.deleteMangaFromLibrary(id) }

    suspend fun getChapterContentFromLibrary(
        id: String,
        collection: String,
        chapter: String
    ): Result<List<String>> =
        resultWrap { service ->
            service.getChapterContentFromLibrary(id, collection, chapter)
                .map { "${url}library/image/$id/$collection/$chapter/$it" }
        }

    /*
     * source
     */

    suspend fun getSources(): Result<List<Source>> =
        resultWrap { it.getSources() }

    suspend fun searchInSource(
        source: String,
        keywords: String,
        page: Int
    ): Result<List<MangaOutline>> =
        resultWrap { it.searchInSource(source, keywords, page) }

    suspend fun getPopularMangaList(source: String, page: Int): Result<List<MangaOutline>> =
        resultWrap { it.getPopular(source, page) }

    suspend fun getLatestMangaList(source: String, page: Int): Result<List<MangaOutline>> =
        resultWrap { it.getLatest(source, page) }

    suspend fun getMangaFromSource(source: String, id: String): Result<MangaDetail> =
        resultWrap { it.getMangaFromSource(source, id) }

    suspend fun getChapterContentFromSource(source: String, chapter: String): Result<List<String>> =
        resultWrap { service ->
            service.getChapterContentFromSource(source, chapter).map { "${url}$it" }
        }

    /*
     * download
     */

    suspend fun getAllDownloadTasks(): Result<List<DownloadTask>> =
        resultWrap { it.getAllDownloadTasks() }

    suspend fun startAllDownloadTasks(): Result<List<DownloadTask>> =
        resultWrap { it.startAllDownloadTasks() }

    suspend fun pauseAllDownloadTasks(): Result<List<DownloadTask>> =
        resultWrap { it.pauseAllDownloadTasks() }

    suspend fun postDownloadTask(
        source: String,
        sourceManga: String,
        targetManga: String
    ): Result<DownloadTask> =
        resultWrap { it.postDownloadTask(source, sourceManga, targetManga) }

    suspend fun deleteDownloadTask(id: Int): Result<DownloadTask> =
        resultWrap { it.deleteDownloadTask(id) }

    suspend fun startDownloadTask(id: Int): Result<DownloadTask> =
        resultWrap { it.startDownloadTask(id) }

    suspend fun pauseDownloadTask(id: Int): Result<DownloadTask> =
        resultWrap { it.pauseDownloadTask(id) }

    /*
     * subscription
     */

    suspend fun getAllSubscriptions(): Result<List<Subscription>> =
        resultWrap { it.getAllSubscriptions() }

    suspend fun enableAllSubscriptions(): Result<List<Subscription>> =
        resultWrap { it.enableAllSubscriptions() }

    suspend fun disableAllSubscriptions(): Result<List<Subscription>> =
        resultWrap { it.disableAllSubscriptions() }

    suspend fun postSubscription(
        source: String,
        sourceManga: String,
        targetManga: String
    ): Result<Subscription> =
        resultWrap { it.postSubscription(source, sourceManga, targetManga) }

    suspend fun deleteSubscription(id: Int): Result<Subscription> =
        resultWrap { it.deleteSubscription(id) }

    suspend fun enableSubscription(id: Int): Result<Subscription> =
        resultWrap { it.enableSubscription(id) }

    suspend fun disableSubscription(id: Int): Result<Subscription> =
        resultWrap { it.disableSubscription(id) }
}
