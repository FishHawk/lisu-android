package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Source
import com.fishhawk.driftinglibraryandroid.ui.explore.globalsearch.GlobalSearchViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.latest.LatestViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.popular.PopularViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.search.SearchViewModel

class ExploreViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    val sourceList: LiveData<Result<List<Source>>> = liveData {
        emit(Result.Loading)
        emit(remoteLibraryRepository.getSources())
    }
}
