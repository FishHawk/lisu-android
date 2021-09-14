package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

internal fun Long.toLocalDate() =
    Date(this).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

internal fun Long.toLocalTime() =
    Date(this).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ReadingHistoryRepository
) : ViewModel() {
    val historyList = repository.list()
        .map { list -> list.groupBy { it.date.toLocalDate() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    fun clear() = viewModelScope.launch { repository.clear() }
}
