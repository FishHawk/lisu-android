package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.remote.LisuRepository
import com.fishhawk.lisu.data.remote.model.ProviderDto
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

    val provider = args.getParcelable<ProviderDto>("provider")!!

    fun login(cookies: Map<String, String>) = viewModelScope.launch {
        lisuRepository.login(provider.id, cookies)
            .onSuccess { sendEvent(ProviderLoginEffect.LoginSuccess) }
            .onFailure { sendEvent(ProviderLoginEffect.LoginFailure(it)) }
    }
}
