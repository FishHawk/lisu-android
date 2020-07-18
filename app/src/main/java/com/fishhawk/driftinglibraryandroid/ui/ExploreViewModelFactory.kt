package com.fishhawk.driftinglibraryandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.explore.latest.LatestViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.popular.PopularViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.search.SearchViewModel


@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory(
    private val source: String,
    private val application: MainApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(LatestViewModel::class.java) ->
                LatestViewModel(source, application.remoteLibraryRepository)
            isAssignableFrom(PopularViewModel::class.java) ->
                PopularViewModel(source, application.remoteLibraryRepository)
            isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(source, application.remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}