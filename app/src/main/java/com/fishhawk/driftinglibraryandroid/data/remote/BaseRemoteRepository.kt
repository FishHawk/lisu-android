package com.fishhawk.driftinglibraryandroid.data.remote

import com.fishhawk.driftinglibraryandroid.data.Result

open class BaseRemoteRepository<Service> {
    var url: String? = null
    var service: Service? = null

    protected inline fun <T> resultWrap(func: (Service) -> T): Result<T> {
        return service?.let {
            try {
                Result.Success(func(service!!))
            } catch (e: Exception) {
                Result.Error(e)
            }
        } ?: Result.Error(IllegalAccessError())
    }
}