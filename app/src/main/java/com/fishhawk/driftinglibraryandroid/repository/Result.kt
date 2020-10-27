package com.fishhawk.driftinglibraryandroid.repository

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }

    fun <T> map(transform: (R) -> T): Result<T> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
            is Loading -> Loading
        }
    }

    fun onSuccess(action: (value: R) -> Unit): Result<R> {
        if (this is Success) action(data)
        return this
    }

    fun onFailure(action: (exception: Throwable) -> Unit): Result<*> {
        if (this is Error) action(exception)
        return this
    }
}
