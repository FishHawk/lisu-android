package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class SubscriptionMode(val value: String) {
    @SerializedName("enabled")
    ENABLED("enabled"),

    @SerializedName("disabled")
    DISABLED("disabled"),
}

//enum class SubscriptionStatus(val value: String) {
//    @SerializedName("waiting")
//    WAITING("waiting"),
//
//    @SerializedName("downloading")
//    DOWNLOADING("downloading"),
//
//    @SerializedName("completed")
//    COMPLETED("completed"),
//
//    @SerializedName("paused")
//    PAUSED("paused"),
//
//    @SerializedName("error")
//    ERROR("error"),
//}

@Parcelize
data class Subscription(
    val id: Int,
    val source: String,
    val sourceManga: String,
    val targetManga: String,
    val mode: SubscriptionMode
) : Parcelable
