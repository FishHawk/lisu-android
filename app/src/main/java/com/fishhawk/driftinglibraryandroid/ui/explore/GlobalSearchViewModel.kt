package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
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
        remoteLibraryRepository.searchInSource(source, keywords, 1)
}