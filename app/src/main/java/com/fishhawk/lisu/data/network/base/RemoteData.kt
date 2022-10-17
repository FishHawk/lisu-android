package com.fishhawk.lisu.data.network.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
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
): Flow<RemoteData<T>> = callbackFlow {
    val dispatcher = Dispatchers.IO.limitedParallelism(1)

    val actionChannel = Channel<RemoteDataAction<T>>()
    var value: Result<T>? = null

    onStart?.invoke(actionChannel)

    suspend fun mySend(newValue: Result<T>?) {
        value = newValue
        send(RemoteData(actionChannel, newValue))
    }

    var job = launch(dispatcher) { mySend(loader()) }

    launch(dispatcher) {
        actionChannel.receiveAsFlow().flowOn(dispatcher).collect { action ->
            when (action) {
                is RemoteDataAction.Mutate -> {
                    value?.onSuccess { mySend(Result.success(action.transformer(it))) }
                }
                is RemoteDataAction.Reload -> {
                    job.cancel()
                    mySend(null)
                    job = launch(dispatcher) { mySend(loader()) }
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