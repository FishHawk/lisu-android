package com.fishhawk.lisu.data.network.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface RemoteDataAction<out T> {
    data class Mutate<T>(val transformer: (T) -> T) : RemoteDataAction<T>
    object Reload : RemoteDataAction<Nothing>
}

typealias RemoteDataActionChannel<T> = Channel<RemoteDataAction<T>>

suspend fun <T> RemoteDataActionChannel<T>.mutate(transformer: (T) -> T) {
    send(RemoteDataAction.Mutate(transformer))
}

suspend fun <T> RemoteDataActionChannel<T>.reload() {
    send(RemoteDataAction.Reload)
}

class RemoteData<T>(
    private val actionChannel: RemoteDataActionChannel<T>,
    val value: Result<T>?,
) {
    suspend fun mutate(transformer: (T) -> T) = actionChannel.mutate(transformer)
    suspend fun reload() = actionChannel.reload()
}

fun <T> remoteData(
    connectivity: Connectivity,
    loader: suspend () -> Result<T>,
    onStart: ((actionChannel: RemoteDataActionChannel<T>) -> Unit)? = null,
    onClose: ((actionChannel: RemoteDataActionChannel<T>) -> Unit)? = null,
): Flow<RemoteData<T>> =
    remoteData(
        connectivity = connectivity,
        block = { emit(loader()) },
        onStart = onStart,
        onClose = onClose,
    )

fun <T> remoteDataFromFlow(
    connectivity: Connectivity,
    loader: suspend () -> Result<Flow<T>>,
    onStart: ((actionChannel: RemoteDataActionChannel<T>) -> Unit)? = null,
    onClose: ((actionChannel: RemoteDataActionChannel<T>) -> Unit)? = null,
): Flow<RemoteData<T>> =
    remoteData(
        connectivity = connectivity,
        block = {
            loader()
                .onSuccess {
                    it.catch { emit(Result.failure(it)) }
                        .collect { emit(Result.success(it)) }
                }
                .onFailure {
                    emit(Result.failure(it))
                }
        },
        onStart = onStart,
        onClose = onClose,
    )

private fun <T> remoteData(
    connectivity: Connectivity,
    block: suspend RemoteDataScope<T>.() -> Unit,
    onStart: ((actionChannel: RemoteDataActionChannel<T>) -> Unit)? = null,
    onClose: ((actionChannel: RemoteDataActionChannel<T>) -> Unit)? = null,
): Flow<RemoteData<T>> = callbackFlow {
    val dispatcher = Dispatchers.IO.limitedParallelism(1)

    val actionChannel = Channel<RemoteDataAction<T>>()
    var value: Result<T>? = null

    onStart?.invoke(actionChannel)

    val remoteDataScope = RemoteDataScope { newValue ->
        value = newValue
        send(RemoteData(actionChannel, newValue))
    }

    var job = launch(dispatcher) { remoteDataScope.block() }

    launch(dispatcher) {
        actionChannel.receiveAsFlow().flowOn(dispatcher).collect { action ->
            when (action) {
                is RemoteDataAction.Mutate -> {
                    value?.onSuccess { remoteDataScope.emit(Result.success(action.transformer(it))) }
                }
                is RemoteDataAction.Reload -> {
                    job.cancel()
                    remoteDataScope.emit(null)
                    job = launch(dispatcher) { remoteDataScope.block() }
                }
            }
        }
    }
    launch(dispatcher) {
        connectivity.interfaceName.collect {
            delay(250)
            if (value?.isSuccess != true) {
                actionChannel.reload()
            }
        }
    }
    awaitClose {
        onClose?.invoke(actionChannel)
    }
}

private fun interface RemoteDataScope<in T> {
    suspend fun emit(value: Result<T>?)
}