package com.fishhawk.driftinglibraryandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.category.CategoryViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.latest.LatestViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.popular.PopularViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.search.SearchViewModel

@Suppress("UNCHECKED_CAST")
class ProviderViewModelFactory(
    private val providerId: String,
    private val application: MainApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(ProviderViewModel::class.java) ->
                ProviderViewModel(
                    providerId,
                    application.remoteProviderRepository
                )

            isAssignableFrom(PopularViewModel::class.java) ->
                PopularViewModel(
                    providerId,
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository
                )

            isAssignableFrom(LatestViewModel::class.java) ->
                LatestViewModel(
                    providerId,
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository
                )

            isAssignableFrom(CategoryViewModel::class.java) ->
                CategoryViewModel(
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