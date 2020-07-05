package com.fishhawk.driftinglibraryandroid.explore

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.data.Source

class GlobalSearchViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    var keywords: String = ""

    val sourceList: LiveData<Result<List<Source>>> = liveData {
        emit(Result.Loading)
        emit(remoteLibraryRepository.getSources())
    }

    suspend fun search(source: String, keywords: String): Result<List<MangaOutline>> =
        remoteLibraryRepository.search(source, keywords, 1)
}