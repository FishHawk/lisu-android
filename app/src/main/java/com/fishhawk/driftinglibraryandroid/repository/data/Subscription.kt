package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class SubscriptionUpdateStrategy(val value: String) {
    @SerializedName("disposable")
    DISPOSABLE("disposable"),

    @SerializedName("periodic")
    PERIODIC("periodic"),

    @SerializedName("never")
    NEVER("never"),
}

@Parcelize
data class Subscription(
    val id: Int,
    val source: String,
    val sourceManga: String,
    val targetManga: String,
    val updateStrategy: SubscriptionUpdateStrategy
) : Parcelable
