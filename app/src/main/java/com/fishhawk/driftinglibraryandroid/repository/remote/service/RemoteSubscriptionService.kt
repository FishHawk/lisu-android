package com.fishhawk.driftinglibraryandroid.repository.remote.service

import com.fishhawk.driftinglibraryandroid.repository.remote.model.Subscription
import retrofit2.http.*

interface RemoteSubscriptionService {
    @GET("/subscriptions")
    suspend fun getAllSubscriptions(): List<Subscription>

    @PATCH("/subscriptions/enable")
    suspend fun enableAllSubscriptions(): List<Subscription>

    @PATCH("/subscriptions/disable")
    suspend fun disableAllSubscriptions(): List<Subscription>

    @FormUrlEncoded
    @POST("/subscription")
    suspend fun postSubscription(
        @Field("providerId") providerId: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): Subscription

    @DELETE("/subscription/{id}")
    suspend fun deleteSubscription(@Path("id") id: Int): Subscription

    @PATCH("/subscription/{id}/enable")
    suspend fun enableSubscription(@Path("id") id: Int): Subscription

    @PATCH("/subscription/{id}/disable")
    suspend fun disableSubscription(@Path("id") id: Int): Subscription
}