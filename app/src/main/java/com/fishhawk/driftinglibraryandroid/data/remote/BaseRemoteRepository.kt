package com.fishhawk.driftinglibraryandroid.data.remote

open class BaseRemoteRepository<Service> {
    var url: String? = null
    var service: Service? = null

    protected inline fun <T> resultWrap(func: (Service) -> T): Result<T> {
        return service?.let {
            try {
                Result.success(func(service!!))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } ?: Result.failure(IllegalAccessError())
    }
}