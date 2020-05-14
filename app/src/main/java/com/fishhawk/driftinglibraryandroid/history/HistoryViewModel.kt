package com.fishhawk.driftinglibraryandroid.history

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory

class HistoryViewModel(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    val readingHistoryList: LiveData<List<ReadingHistory>> =
        readingHistoryRepository.observeAllReadingHistory()
}

@Suppress("UNCHECKED_CAST")
class HistoryViewModelFactory(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(readingHistoryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
