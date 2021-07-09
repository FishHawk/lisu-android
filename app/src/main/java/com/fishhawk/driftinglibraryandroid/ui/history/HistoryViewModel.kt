package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    private val historyList: LiveData<List<ReadingHistory>> =
        GlobalPreference.selectedServer.asFlow().asLiveData().switchMap {
            readingHistoryRepository.observeAllReadingHistoryOfServer(it)
        }

    private val _filteredHistoryList: MediatorLiveData<List<ReadingHistory>> = MediatorLiveData()
    val filteredHistoryList: LiveData<List<ReadingHistory>> = _filteredHistoryList

    init {
        _filteredHistoryList.addSource(historyList) { list ->
            val filter = GlobalPreference.historyFilter.get()
            _filteredHistoryList.value = filterList(list, filter)
        }
        _filteredHistoryList.addSource(
            GlobalPreference.historyFilter.asFlow().asLiveData()
        ) { filter ->
            val list = historyList.value
            if (list != null) _filteredHistoryList.value = filterList(list, filter)
        }
    }

    private fun filterList(
        list: List<ReadingHistory>,
        filter: GlobalPreference.HistoryFilter
    ): List<ReadingHistory> {
        return when (filter) {
            GlobalPreference.HistoryFilter.ALL -> list
            GlobalPreference.HistoryFilter.FROM_LIBRARY -> list.filter { it.providerId == null }
            GlobalPreference.HistoryFilter.FROM_SOURCES -> list.filter { it.providerId != null }
        }
    }

    fun clearReadingHistory() = viewModelScope.launch {
        readingHistoryRepository.clearReadingHistoryOfServer(
            GlobalPreference.selectedServer.get()
        )
    }
}
