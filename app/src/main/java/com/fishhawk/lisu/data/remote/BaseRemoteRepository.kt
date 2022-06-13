package com.fishhawk.lisu.data.remote

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

abstract class BaseRemoteRepository(client: Flow<Result<HttpClient>?>) {
    val serviceFlow = client.stateIn(
        CoroutineScope(SupervisorJob() + Dispatchers.Main),
        SharingStarted.Eagerly, null
    )

    protected val String.path
        get() = encodeURLPath()

    protected suspend inline fun <T> resultWrap(crossinline func: suspend (HttpClient) -> T): Result<T> {
        val service = serviceFlow.filterNotNull().first()
        return service.mapCatching { func(it) }
    }
}