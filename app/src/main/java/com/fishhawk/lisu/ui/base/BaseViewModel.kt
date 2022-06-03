package com.fishhawk.lisu.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

interface Event

abstract class BaseViewModel<E : Event> : ViewModel() {
    private val _event = Channel<E>()
    val event = _event.receiveAsFlow().shareIn(viewModelScope, SharingStarted.Lazily)
    protected suspend fun sendEvent(event: E) = _event.send(event)
    protected fun sendEventSync(event: E) = viewModelScope.launch { _event.send(event) }
}

@Composable
fun <E : Event> OnEvent(event: Flow<E>, onEvent: (E) -> Unit) {
    LaunchedEffect(Unit) {
        event.collect(onEvent)
    }
}
