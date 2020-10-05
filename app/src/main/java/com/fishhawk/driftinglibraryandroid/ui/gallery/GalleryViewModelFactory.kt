package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication

@Suppress("UNCHECKED_CAST")
class GalleryViewModelFactory constructor(
    private val application: MainApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(GalleryViewModel::class.java) ->
                GalleryViewModel(
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    application.remoteDownloadRepository,
                    application.remoteSubscriptionRepository,
                    application.readingHistoryRepository
                )
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}