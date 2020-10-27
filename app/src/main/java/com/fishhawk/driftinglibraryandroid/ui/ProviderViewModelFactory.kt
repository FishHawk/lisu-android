package com.fishhawk.driftinglibraryandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.search.SearchViewModel

@Suppress("UNCHECKED_CAST")
class ProviderViewModelFactory(
    private val providerId: String,
    private val application: MainApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {

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