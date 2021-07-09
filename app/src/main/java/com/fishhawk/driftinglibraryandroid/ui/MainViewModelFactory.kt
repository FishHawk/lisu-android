package com.fishhawk.driftinglibraryandroid.ui

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.explore.ExploreViewModel
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.ui.globalsearch.GlobalSearchViewModel
import com.fishhawk.driftinglibraryandroid.ui.history.HistoryViewModel
import com.fishhawk.driftinglibraryandroid.ui.library.LibraryViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.search.SearchViewModel
import com.fishhawk.driftinglibraryandroid.ui.server.ServerViewModel

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory constructor(fragment: Fragment) : ViewModelProvider.Factory {
    private val arguments = fragment.arguments
    private val application = fragment.requireActivity().application as MainApplication

    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(GlobalSearchViewModel::class.java) ->
                GlobalSearchViewModel(
                    application.remoteProviderRepository,
                    arguments?.getString("keywords")!!
                )

            isAssignableFrom(ProviderViewModel::class.java) ->
                ProviderViewModel(
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    arguments?.getParcelable("provider")!!
                )

            isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    arguments?.getParcelable("provider")!!,
                    arguments.getString("keywords")!!
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
                    arguments?.getParcelable("outline")!!,
                    arguments.getParcelable("provider"),
                )

            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}