package com.fishhawk.driftinglibraryandroid.ui.main.history

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    private val readingHistoryList: LiveData<List<ReadingHistory>> =
        SettingsHelper.selectedServer.switchMap {
            readingHistoryRepository.observeAllReadingHistoryOfServer(it)
        }

    val filteredReadingHistoryList: MediatorLiveData<List<ReadingHistory>> = MediatorLiveData()

    init {
        filteredReadingHistoryList.addSource(readingHistoryList) { list ->
            val filter = SettingsHelper.historyFilter.getValueDirectly()
            filteredReadingHistoryList.value = filterList(list, filter)
        }
        filteredReadingHistoryList.addSource(SettingsHelper.historyFilter) { filter ->
            val list = readingHistoryList.value
            if (list != null) filteredReadingHistoryList.value = filterList(list, filter)
        }
    }

    fun clearReadingHistory() = viewModelScope.launch {
        readingHistoryRepository.clearReadingHistoryOfServer(
            SettingsHelper.selectedServer.getValueDirectly()
        )
    }

    private fun filterList(
        list: List<ReadingHistory>,
        filter: SettingsHelper.HistoryFilter
    ): List<ReadingHistory> {
        return when (filter) {
            SettingsHelper.HistoryFilter.ALL -> list
            SettingsHelper.HistoryFilter.FROM_LIBRARY -> list.filter { it.providerId == null }
            SettingsHelper.HistoryFilter.FROM_SOURCES -> list.filter { it.providerId != null }
        }
    }
}
