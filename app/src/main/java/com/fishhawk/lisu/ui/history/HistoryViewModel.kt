package com.fishhawk.lisu.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.database.model.ReadingHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel (
    private val repository: ReadingHistoryRepository
) : ViewModel() {

    val histories = repository.list()
        .map { list -> list.groupBy { it.date.toLocalDate() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    fun deleteHistory(history: ReadingHistory) = viewModelScope.launch {
        repository.delete(history)
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clear()
    }
}
