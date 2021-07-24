package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.datastore.HistoryFilter
import com.fishhawk.driftinglibraryandroid.data.datastore.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.get
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ReadingHistoryRepository
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyList =
        combine(
            PR.historyFilter.flow,
            PR.selectedServer.flow.flatMapLatest { repository.list(it) }
        ) { mode, list ->
            when (mode) {
                HistoryFilter.All -> list
                HistoryFilter.FromLibrary -> list.filter { it.providerId == null }
                HistoryFilter.FromProvider -> list.filter { it.providerId != null }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    fun clearReadingHistory() = viewModelScope.launch {
        repository.clear(PR.selectedServer.get())
    }
}
