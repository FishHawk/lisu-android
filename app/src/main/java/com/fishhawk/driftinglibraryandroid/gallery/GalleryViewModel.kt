package com.fishhawk.driftinglibraryandroid.gallery

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val id: String,
    private val source: String?,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    val detail: LiveData<Result<MangaDetail>> = liveData {
        emit(Result.Loading)
        if (source == null) emit(remoteLibraryRepository.getMangaFromLibrary(id))
        else emit(remoteLibraryRepository.getMangaFromSource(source, id))
    }

    val readingHistory: LiveData<ReadingHistory> = detail.switchMap {
        if (it is Result.Success) readingHistoryRepository.observeReadingHistory(it.data.id)
        else MutableLiveData()
    }

    private val _operationError: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val operationError: LiveData<Event<Throwable?>> = _operationError

    fun download() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.title
            viewModelScope.launch(Dispatchers.Main) {
                when (val result = remoteLibraryRepository.postDownloadTask(source!!, id, title)) {
                    is Result.Success -> _operationError.value = Event(null)
                    is Result.Error -> _operationError.value = Event(result.exception)
                }
            }
        }
    }

    fun subscribe() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.title
            viewModelScope.launch(Dispatchers.Main) {
                when (val result = remoteLibraryRepository.postSubscription(source!!, id, title)) {
                    is Result.Success -> _operationError.value = Event(null)
                    is Result.Error -> _operationError.value = Event(result.exception)
                }
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