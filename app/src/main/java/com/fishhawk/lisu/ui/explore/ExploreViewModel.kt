package com.fishhawk.lisu.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.remote.LisuRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val lisuRepository: LisuRepository,
) : ViewModel() {
    val providersLoadState = lisuRepository.providers
        .filterNotNull()
        .map { it.value?.map { list -> list.groupBy { provider -> provider.lang } } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val lastUsedProvider =
        combine(
            lisuRepository.providers
                .filterNotNull()
                .mapNotNull { it.value?.getOrNull() },
            PR.lastUsedProvider.flow
        ) { list, name -> list.find { it.id == name } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun reload() {
        viewModelScope.launch {
            lisuRepository.providers.value?.reload()
        }
    }

    fun logout(providerId: String) {
        viewModelScope.launch {
            lisuRepository.logout(providerId)
                .onSuccess { }
                .onFailure { }
        }
    }
}
