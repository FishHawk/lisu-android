package com.fishhawk.lisu.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.database.ServerHistoryRepository
import com.fishhawk.lisu.data.database.model.ServerHistory
import com.fishhawk.lisu.data.datastore.getBlocking
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoreViewModel (
    private val repository: ServerHistoryRepository
) : ViewModel() {
    val address = PR.serverAddress.let {
        it.flow.stateIn(viewModelScope, SharingStarted.Eagerly, it.getBlocking())
    }

    val suggestions = repository.list()
        .map { list -> list.map { it.address } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateAddress(address: String) = viewModelScope.launch {
        PR.serverAddress.set(address)
        repository.update(ServerHistory(address = address))
    }

    fun deleteSuggestion(address: String) = viewModelScope.launch {
        repository.deleteByAddress(address)
    }
}