package com.fishhawk.driftinglibraryandroid.gallery

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val id: String,
    private val source: String?,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    val mangaDetail: LiveData<Result<MangaDetail>> = liveData {
        emit(Result.Loading)
        emit(remoteLibraryRepository.getManga(id, source))
    }

    val readingHistory: LiveData<ReadingHistory> = mangaDetail.switchMap {
        if (it is Result.Success) readingHistoryRepository.observeReadingHistory(it.data.id)
        else MutableLiveData()
    }

    private val _downloadRequestFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val downloadRequestFinish: LiveData<Event<Throwable?>> = _downloadRequestFinish

    fun download() {
        val id = (mangaDetail.value as Result.Success).data.id
        val title = (mangaDetail.value as Result.Success).data.title
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = remoteLibraryRepository.postDownloadTask(source!!, id, title)) {
                is Result.Success -> _downloadRequestFinish.value = Event(null)
                is Result.Error -> _downloadRequestFinish.value = Event(result.exception)
            }
        }
    }

    fun subscribe() {
        val id = (mangaDetail.value as Result.Success).data.id
        val title = (mangaDetail.value as Result.Success).data.title
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = remoteLibraryRepository.postSubscription(source!!, id, title)) {
                is Result.Success -> _downloadRequestFinish.value = Event(null)
                is Result.Error -> _downloadRequestFinish.value = Event(result.exception)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class GalleryViewModelFactory(
    private val id: String,
    private val source: String?,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(GalleryViewModel::class.java) ->
                GalleryViewModel(id, source, remoteLibraryRepository, readingHistoryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}