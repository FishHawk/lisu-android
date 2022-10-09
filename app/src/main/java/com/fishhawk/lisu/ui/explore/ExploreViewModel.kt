package com.fishhawk.lisu.ui.explore

import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ExploreEvent : Event {
    object LoginSuccess : ExploreEvent
    data class LoginFailure(val exception: Throwable) : ExploreEvent
}

class ExploreViewModel(
    private val lisuRepository: LisuRepository,
) : BaseViewModel<ExploreEvent>() {
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

    fun loginByCookies(providerId: String, cookies: Map<String, String>) {
        viewModelScope.launch {
            lisuRepository.loginByCookies(providerId, cookies)
                .onSuccess { sendEvent(ExploreEvent.LoginSuccess) }
                .onFailure { sendEvent(ExploreEvent.LoginFailure(it)) }
        }
    }

    fun loginByPassword(providerId: String, username: String, password: String) {
        viewModelScope.launch {
            lisuRepository.loginByPassword(providerId, username, password)
                .onSuccess { sendEvent(ExploreEvent.LoginSuccess) }
                .onFailure { sendEvent(ExploreEvent.LoginFailure(it)) }
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
