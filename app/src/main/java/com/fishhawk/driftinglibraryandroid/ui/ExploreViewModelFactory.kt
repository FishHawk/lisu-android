package com.fishhawk.driftinglibraryandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.explore.latest.LatestViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.popular.PopularViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.search.SearchViewModel

@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory(
    private val providerId: String,
    private val application: MainApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(LatestViewModel::class.java) ->
                LatestViewModel(
                    providerId,
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository
                )
            isAssignableFrom(PopularViewModel::class.java) ->
                PopularViewModel(
                    providerId,
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository
                )
            isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(
                    providerId,
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository
                )
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}