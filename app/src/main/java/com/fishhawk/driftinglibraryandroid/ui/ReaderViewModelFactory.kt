package com.fishhawk.driftinglibraryandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderViewModel

@Suppress("UNCHECKED_CAST")
class ReaderViewModelFactory(
    private val id: String,
    private val providerId: String?,
    private val collectionIndex: Int,
    private val chapterIndex: Int,
    private val pageIndex: Int,
    private val application: MainApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(ReaderViewModel::class.java) ->
                ReaderViewModel(
                    id,
                    providerId,
                    collectionIndex,
                    chapterIndex,
                    pageIndex,
                    application.remoteLibraryRepository,
                    application.remoteProviderRepository,
                    application.readingHistoryRepository
                )
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}