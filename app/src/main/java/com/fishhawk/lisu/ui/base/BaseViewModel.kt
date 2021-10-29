package com.fishhawk.lisu.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

interface Effect

abstract class BaseViewModel<E : Effect> : ViewModel() {
    private val _effect = Channel<E>()
    val effect = _effect.receiveAsFlow()
    protected suspend fun sendEffect(effect: E) = _effect.send(effect)
}
