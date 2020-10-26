package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    private val readingHistoryList: LiveData<List<ReadingHistory>> =
        GlobalPreference.selectedServer.switchMap {
            readingHistoryRepository.observeAllReadingHistoryOfServer(it)
        }

    val filteredReadingHistoryList: MediatorLiveData<List<ReadingHistory>> = MediatorLiveData()

    init {
        filteredReadingHistoryList.addSource(readingHistoryList) { list ->
            val filter = GlobalPreference.historyFilter.getValueDirectly()
            filteredReadingHistoryList.value = filterList(list, filter)
        }
        filteredReadingHistoryList.addSource(GlobalPreference.historyFilter) { filter ->
            val list = readingHistoryList.value
            if (list != null) filteredReadingHistoryList.value = filterList(list, filter)
        }
    }

    fun clearReadingHistory() = viewModelScope.launch {
        readingHistoryRepository.clearReadingHistoryOfServer(
            GlobalPreference.selectedServer.getValueDirectly()
        )
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
}
