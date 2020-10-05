package com.fishhawk.driftinglibraryandroid.repository.remote.service

import com.fishhawk.driftinglibraryandroid.repository.remote.model.Subscription
import retrofit2.http.*

interface RemoteSubscriptionService {
    @GET("/subscription/list")
    suspend fun getAllSubscriptions(): List<Subscription>

    @PATCH("/subscription/list/enable")
    suspend fun enableAllSubscriptions(): List<Subscription>

    @PATCH("/subscription/list/disable")
    suspend fun disableAllSubscriptions(): List<Subscription>

    @FormUrlEncoded
    @POST("/subscription/item")
    suspend fun postSubscription(
        @Field("providerId") providerId: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): Subscription

    @DELETE("/subscription/item/{id}")
    suspend fun deleteSubscription(@Path("id") id: String): Subscription

    @PATCH("/subscription/item/{id}/enable")
    suspend fun enableSubscription(@Path("id") id: String): Subscription

    @PATCH("/subscription/item/{id}/disable")
    suspend fun disableSubscription(@Path("id") id: String): Subscription
}