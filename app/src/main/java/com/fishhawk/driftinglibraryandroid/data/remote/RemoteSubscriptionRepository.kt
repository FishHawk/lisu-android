package com.fishhawk.driftinglibraryandroid.data.remote

import retrofit2.Retrofit
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.model.Subscription
import com.fishhawk.driftinglibraryandroid.data.remote.service.RemoteSubscriptionService

class RemoteSubscriptionRepository : BaseRemoteRepository<RemoteSubscriptionService>() {
    fun connect(url: String?, builder: Retrofit?) {
        this.url = url
        this.service = builder?.create(RemoteSubscriptionService::class.java)
    }

    suspend fun getAllSubscriptions(): Result<List<Subscription>> =
        resultWrap { it.getAllSubscriptions() }

    suspend fun enableAllSubscriptions(): Result<List<Subscription>> =
        resultWrap { it.enableAllSubscriptions() }

    suspend fun disableAllSubscriptions(): Result<List<Subscription>> =
        resultWrap { it.disableAllSubscriptions() }

    suspend fun postSubscription(
        providerId: String,
        sourceManga: String,
        targetManga: String
    ): Result<Subscription> =
        resultWrap { it.postSubscription(providerId, sourceManga, targetManga) }

    suspend fun deleteSubscription(id: String): Result<Subscription> =
        resultWrap { it.deleteSubscription(id) }

    suspend fun enableSubscription(id: String): Result<Subscription> =
        resultWrap { it.enableSubscription(id) }

    suspend fun disableSubscription(id: String): Result<Subscription> =
        resultWrap { it.disableSubscription(id) }
}