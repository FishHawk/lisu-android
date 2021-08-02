package com.fishhawk.driftinglibraryandroid.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit

// Hack, see https://youtrack.jetbrains.com/issue/KT-46477#focus=Comments-27-4952485.0-0
class ResultX<out T> internal constructor(internal val value: Any?) {
    val isSuccess: Boolean get() = value !is Failure
    val isFailure: Boolean get() = value is Failure

    fun getOrNull(): T? = when {
        isFailure -> null
        else -> value as T
    }

    fun exceptionOrNull(): Throwable? = when (value) {
        is Failure -> value.exception
        else -> null
    }

    fun <R> fold(
        onSuccess: (value: T) -> R,
        onFailure: (exception: Throwable) -> R
    ): R {
        return when (val exception = exceptionOrNull()) {
            null -> onSuccess(value as T)
            else -> onFailure(exception)
        }
    }

    fun onFailure(action: (exception: Throwable) -> Unit): ResultX<T> {
        exceptionOrNull()?.let { action(it) }
        return this
    }

    fun onSuccess(action: (value: T) -> Unit): ResultX<T> {
        if (isSuccess) action(value as T)
        return this
    }

    override fun toString(): String =
        when (value) {
            is Failure -> value.toString()
            else -> "Success($value)"
        }

    companion object {
        fun <T> success(value: T): ResultX<T> = ResultX(value)
        fun <T> failure(exception: Throwable): ResultX<T> = ResultX(createFailure(exception))
    }

    internal class Failure(val exception: Throwable) {
        override fun equals(other: Any?): Boolean = other is Failure && exception == other.exception
        override fun hashCode(): Int = exception.hashCode()
        override fun toString(): String = "Failure($exception)"
    }
}

suspend fun <T, R> T.runCatching(block: suspend T.() -> R): ResultX<R> {
    return try {
        ResultX.success(block())
    } catch (e: Throwable) {
        ResultX.failure(e)
    }
}

fun <R, T> ResultX<T>.map(transform: (value: T) -> R): ResultX<R> {
    return when {
        isSuccess -> ResultX.success(transform(value as T))
        else -> ResultX(value)
    }
}

suspend fun <R, T> ResultX<T>.mapCatching(transform: suspend (value: T) -> R): ResultX<R> {
    return when {
        isSuccess -> runCatching { transform(value as T) }
        else -> ResultX(value)
    }
}

internal fun createFailure(exception: Throwable): Any = ResultX.Failure(exception)


abstract class BaseRemoteRepository<Service>(retrofit: Flow<ResultX<Retrofit>?>) {
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

    protected suspend inline fun <T> resultWrap(crossinline func: suspend (Service) -> T): ResultX<T> {
        val service = serviceFlow.filterNotNull().first()
        return service.mapCatching { func(it) }
    }
}