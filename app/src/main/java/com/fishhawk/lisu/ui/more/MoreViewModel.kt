package com.fishhawk.lisu.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.ServerHistoryRepository
import com.fishhawk.lisu.data.database.model.ServerHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val repository: ServerHistoryRepository
) : ViewModel() {

    val suggestedAddresses = repository.list()
        .map { list -> list.map { it.address } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun update(address: String) = viewModelScope.launch {
        repository.update(ServerHistory(address = address))
    }

    fun clear() = viewModelScope.launch { repository.clear() }
}
