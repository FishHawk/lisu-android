package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.preference.P
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ReadingHistoryRepository
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyList =
        combine(
            P.historyFilter.asFlow(),
            P.selectedServer.asFlow().flatMapLatest { repository.list(it) }
        ) { mode, list ->
            when (mode) {
                P.HistoryFilter.ALL -> list
                P.HistoryFilter.FROM_LIBRARY -> list.filter { it.providerId == null }
                P.HistoryFilter.FROM_SOURCES -> list.filter { it.providerId != null }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    fun clearReadingHistory() = viewModelScope.launch {
        repository.clear(P.selectedServer.get())
    }
}
