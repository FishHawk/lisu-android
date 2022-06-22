package com.fishhawk.lisu.data.remote.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface RemoteListAction<out T> {
    data class Mutate<T>(val transformer: (MutableList<T>) -> MutableList<T>) : RemoteListAction<T>
    object Reload : RemoteListAction<Nothing>
    object RequestNextPage : RemoteListAction<Nothing>
}

typealias RemoteListActionChannel<T> = Channel<RemoteListAction<T>>

suspend fun <T> RemoteListActionChannel<T>.mutate(transformer: (MutableList<T>) -> MutableList<T>) {
    send(RemoteListAction.Mutate(transformer))
}

suspend fun <T> RemoteListActionChannel<T>.reload() {
    send(RemoteListAction.Reload)
}

suspend fun <T> RemoteListActionChannel<T>.requestNextPage() {
    send(RemoteListAction.RequestNextPage)
}

class RemoteList<T>(
    private val actionChannel: RemoteListActionChannel<T>,
    val value: Result<PagedList<T>>?,
) {
    suspend fun mutate(transformer: (MutableList<T>) -> MutableList<T>) =
        actionChannel.mutate(transformer)

    suspend fun reload() = actionChannel.reload()
    suspend fun requestNextPage() = actionChannel.requestNextPage()
}

data class PagedList<T>(
    val list: List<T>,
    val appendState: Result<Unit>?,
)

data class Page<Key : Any, T>(
    val data: List<T>,
    val nextKey: Key?,
)

fun <Key : Any, T> remotePagingList(
    startKey: Key,
    loader: suspend (Key) -> Result<Page<Key, T>>,
    onStart: ((actionChannel: RemoteListActionChannel<T>) -> Unit)? = null,
    onClose: ((actionChannel: RemoteListActionChannel<T>) -> Unit)? = null,
): Flow<RemoteList<T>> = callbackFlow {
    val dispatcher = Dispatchers.IO.limitedParallelism(1)

    val actionChannel = Channel<RemoteListAction<T>>()

    var listState: Result<Unit>? = null
    var appendState: Result<Unit>? = null
    var value: MutableList<T> = mutableListOf()
    var nextKey: Key? = startKey

    onStart?.invoke(actionChannel)

    suspend fun mySend() {
        send(
            RemoteList(
                actionChannel = actionChannel,
                value = listState?.map {
                    PagedList(
                        appendState = appendState,
                        list = value.toList(),
                    )
                },
            )
        )
    }

    fun requestNextPage() = launch(dispatcher) {
        nextKey?.let { key ->
            appendState = null
            mySend()
            loader(key)
                .onSuccess {
                    value.addAll(it.data)
                    nextKey = it.nextKey
                    listState = Result.success(Unit)
                    appendState = Result.success(Unit)
                    mySend()
                }
                .onFailure {
                    if (listState?.isSuccess != true)
                        listState = Result.failure(it)
                    appendState = Result.failure(it)
                    mySend()
                }
        }
    }

    var job = requestNextPage()

    launch(dispatcher) {
        actionChannel.receiveAsFlow().flowOn(dispatcher).collect { action ->
            when (action) {
                is RemoteListAction.Mutate -> {
                    value = action.transformer(value)
                    mySend()
                }
                is RemoteListAction.Reload -> {
                    job.cancel()
                    listState = null
                    appendState = null
                    value.clear()
                    nextKey = startKey
                    mySend()
                    job = requestNextPage()
                }
                is RemoteListAction.RequestNextPage -> {
                    if (!job.isActive) job = requestNextPage()
                }
            }
        }
    }
    launch(dispatcher) {
        Connectivity.instance?.interfaceName?.collect {
            if (job.isActive) {
                job.cancel()
                job = requestNextPage()
            }
        }
    }
    awaitClose {
        onClose?.invoke(actionChannel)
    }
}
