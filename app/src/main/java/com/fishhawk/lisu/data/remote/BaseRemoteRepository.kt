package com.fishhawk.lisu.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit

abstract class BaseRemoteRepository<Service>(retrofit: Flow<Result<Retrofit>?>) {
    abstract val serviceType: Class<Service>

    var url: String? = null

    val serviceFlow = retrofit
        .map { result ->
            url = result?.getOrNull()?.baseUrl()?.toUrl()?.toString()
            result?.map { it.create(serviceType) }
        }
        .stateIn(
            CoroutineScope(SupervisorJob() + Dispatchers.Main),
            SharingStarted.Eagerly, null
        )

    protected suspend inline fun <T> resultWrap(crossinline func: suspend (Service) -> T): Result<T> {
        val service = serviceFlow.filterNotNull().first()
        return service.mapCatching { func(it) }
    }
}