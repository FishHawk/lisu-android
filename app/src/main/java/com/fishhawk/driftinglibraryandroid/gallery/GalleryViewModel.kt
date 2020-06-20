package com.fishhawk.driftinglibraryandroid.gallery

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory

class GalleryViewModel(
    private val id: String,
    private val source: String?,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    val mangaDetail: LiveData<Result<MangaDetail>> = liveData {
        emit(Result.Loading)
        emit(remoteLibraryRepository.getMangaDetail(id, source))
    }

    val readingHistory: LiveData<ReadingHistory> = mangaDetail.switchMap {
        if (it is Result.Success) readingHistoryRepository.observeReadingHistory(it.data.id)
        else MutableLiveData()
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