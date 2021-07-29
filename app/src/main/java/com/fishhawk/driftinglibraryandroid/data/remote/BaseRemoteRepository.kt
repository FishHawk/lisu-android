package com.fishhawk.driftinglibraryandroid.data.remote

import kotlinx.coroutines.flow.StateFlow
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

internal fun createFailure(exception: Throwable): Any = ResultX.Failure(exception)


abstract class BaseRemoteRepository<Service>(private val retrofit: StateFlow<Retrofit?>) {
    val url: String?
        get() = retrofit.value?.baseUrl()?.toUrl()?.toString()

    abstract val serviceFlow: StateFlow<Service?>

    val service
        get() = serviceFlow.value

    protected inline fun <T> resultWrap(func: (Service) -> T): ResultX<T> {
        return service?.let {
            try {
                ResultX.success(func(service!!))
            } catch (e: Exception) {
                ResultX.failure(e)
            }
        } ?: ResultX.failure(IllegalAccessError())
    }
}