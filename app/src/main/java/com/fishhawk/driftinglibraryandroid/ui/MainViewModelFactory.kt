package com.fishhawk.driftinglibraryandroid.ui

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.download.DownloadViewModel
import com.fishhawk.driftinglibraryandroid.ui.explore.ExploreViewModel
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.ui.globalsearch.GlobalSearchViewModel
import com.fishhawk.driftinglibraryandroid.ui.history.HistoryViewModel
import com.fishhawk.driftinglibraryandroid.ui.library.LibraryViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.search.SearchViewModel
import com.fishhawk.driftinglibraryandroid.ui.server.ServerViewModel
import com.fishhawk.driftinglibraryandroid.ui.subscription.SubscriptionViewModel

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory constructor(
    private val application: MainApplication,
    private val bundle: Bundle = bundleOf()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(application.remoteLibraryRepository)

            isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(application.readingHistoryRepository)

            isAssignableFrom(ExploreViewModel::class.java) ->
                ExploreViewModel(application.remoteProviderRepository)

            isAssignableFrom(GlobalSearchViewModel::class.java) ->
                GlobalSearchViewModel(application.remoteProviderRepository)

            isAssignableFrom(ProviderViewModel::class.java) ->
                ProviderViewModel(
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository
                )

            isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository
                )

            isAssignableFrom(ServerViewModel::class.java) ->
                ServerViewModel(
                    application.readingHistoryRepository,
                    application.serverInfoRepository
                )

            isAssignableFrom(DownloadViewModel::class.java) ->
                DownloadViewModel(application.remoteDownloadRepository)

            isAssignableFrom(SubscriptionViewModel::class.java) ->
                SubscriptionViewModel(application.remoteSubscriptionRepository)

            isAssignableFrom(GalleryViewModel::class.java) ->
                GalleryViewModel(
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository,
                    application.readingHistoryRepository,
                    bundle.getString("id")!!,
                    bundle.getString("providerId")
                )

            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}