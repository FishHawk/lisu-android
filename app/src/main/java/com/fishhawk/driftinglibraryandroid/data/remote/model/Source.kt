package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

enum class SourceState(val value: String) {
    @SerializedName("downloading")
    DOWNLOADING("downloading"),

    @SerializedName("waiting")
    WAITING("waiting"),

    @SerializedName("error")
    ERROR("error"),

    @SerializedName("updated")
    UPDATED("updated"),
}

@Parcelize
data class Source(
    val providerId: String,
    val mangaId: String,
    val shouldDeleteAfterUpdated: Boolean,
    val state: SourceState,
    val message: String?
) : Parcelable
