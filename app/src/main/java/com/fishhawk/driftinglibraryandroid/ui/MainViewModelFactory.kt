package com.fishhawk.driftinglibraryandroid.ui

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.explore.ExploreViewModel
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.ui.globalsearch.GlobalSearchViewModel
import com.fishhawk.driftinglibraryandroid.ui.history.HistoryViewModel
import com.fishhawk.driftinglibraryandroid.ui.library.LibraryViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.search.SearchViewModel
import com.fishhawk.driftinglibraryandroid.ui.server.ServerViewModel

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory constructor(
    private val application: MainApplication,
    private val bundle: Bundle = bundleOf()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(
                    application.remoteLibraryRepository,
                    bundle.getString("keywords")
                )

            isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(application.readingHistoryRepository)

            isAssignableFrom(ExploreViewModel::class.java) ->
                ExploreViewModel(application.remoteProviderRepository)

            isAssignableFrom(GlobalSearchViewModel::class.java) ->
                GlobalSearchViewModel(application.remoteProviderRepository)

            isAssignableFrom(ProviderViewModel::class.java) ->
                ProviderViewModel(
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    bundle.getString("providerId")!!,
                )

            isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    bundle.getString("providerId")!!,
                    bundle.getString("keywords")!!
                )

            isAssignableFrom(ServerViewModel::class.java) ->
                ServerViewModel(
                    application.readingHistoryRepository,
                    application.serverInfoRepository
                )

            isAssignableFrom(GalleryViewModel::class.java) ->
                GalleryViewModel(
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    application.readingHistoryRepository,
                    bundle.getParcelable<MangaOutline>("outline")!!.id,
                    bundle.getString("providerId")
                )

            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}