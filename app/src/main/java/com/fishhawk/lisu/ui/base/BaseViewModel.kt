package com.fishhawk.lisu.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface Effect

abstract class BaseViewModel<E : Effect> : ViewModel() {
    private val _effect = Channel<E>()
    val effect = _effect.receiveAsFlow()
    protected suspend fun sendEffect(effect: E) = _effect.send(effect)
    protected fun launchEffect(effect: E) = viewModelScope.launch { _effect.send(effect) }
}
