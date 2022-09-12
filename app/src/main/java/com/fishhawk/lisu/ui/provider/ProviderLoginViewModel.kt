package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.launch

sealed interface ProviderLoginEffect : Event {
    object LoginSuccess : ProviderLoginEffect
    data class LoginFailure(val exception: Throwable) : ProviderLoginEffect
}

class ProviderLoginViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
) : BaseViewModel<ProviderLoginEffect>() {
    val providerId = args.getString("providerId")!!

    val loginSite =
        lisuRepository.providers.value!!.value!!.getOrThrow()
            .find { provider -> provider.id == providerId }!!.loginSite

    fun login(cookies: Map<String, String>) = viewModelScope.launch {
        lisuRepository.login(providerId, cookies)
            .onSuccess { sendEvent(ProviderLoginEffect.LoginSuccess) }
            .onFailure { sendEvent(ProviderLoginEffect.LoginFailure(it)) }
    }
}
