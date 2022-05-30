package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Effect
import kotlinx.coroutines.launch

sealed interface ProviderLoginEffect : Effect {
    object LoginSuccess : ProviderLoginEffect
    data class LoginFailure(val exception: Throwable) : ProviderLoginEffect
}

class ProviderLoginViewModel(
    args: Bundle,
    private val remoteProviderRepository: RemoteProviderRepository,
) : BaseViewModel<ProviderLoginEffect>() {

    val provider = args.getParcelable<ProviderDto>("provider")!!

    fun login(cookies: Map<String, String>) = viewModelScope.launch {
        remoteProviderRepository.login(provider.id, cookies)
            .onSuccess { sendEffect(ProviderLoginEffect.LoginSuccess) }
            .onFailure { sendEffect(ProviderLoginEffect.LoginFailure(it)) }
    }
}
