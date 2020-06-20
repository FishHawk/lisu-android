package com.fishhawk.driftinglibraryandroid.explore

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Source

class ExploreViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    val sourceList: LiveData<Result<List<Source>>> = liveData {
        emit(Result.Loading)
        emit(remoteLibraryRepository.getSources())
    }
}

@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(ExploreViewModel::class.java) ->
                ExploreViewModel(remoteLibraryRepository)
            isAssignableFrom(LatestViewModel::class.java) ->
                LatestViewModel(source, remoteLibraryRepository)
            isAssignableFrom(PopularViewModel::class.java) ->
                PopularViewModel(source, remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}