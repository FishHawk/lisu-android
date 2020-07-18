package com.fishhawk.driftinglibraryandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.download.DownloadViewModel
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.ui.history.HistoryViewModel
import com.fishhawk.driftinglibraryandroid.ui.library.LibraryViewModel
import com.fishhawk.driftinglibraryandroid.ui.server.ServerViewModel
import com.fishhawk.driftinglibraryandroid.ui.subscription.SubscriptionViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val application: MainApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(application.remoteLibraryRepository)

            isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(application.readingHistoryRepository)

            isAssignableFrom(ServerViewModel::class.java) ->
                ServerViewModel(application.serverInfoRepository)

            isAssignableFrom(DownloadViewModel::class.java) ->
                DownloadViewModel(application.remoteLibraryRepository)

            isAssignableFrom(SubscriptionViewModel::class.java) ->
                SubscriptionViewModel(application.remoteLibraryRepository)

            isAssignableFrom(GalleryViewModel::class.java) ->
                GalleryViewModel(
                    application.remoteLibraryRepository,
                    application.readingHistoryRepository
                )
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}