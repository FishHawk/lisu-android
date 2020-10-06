package com.fishhawk.driftinglibraryandroid.repository.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class DownloadStatus(val value: String) {
    @SerializedName("waiting")
    WAITING("waiting"),

    @SerializedName("downloading")
    DOWNLOADING("downloading"),

    @SerializedName("paused")
    PAUSED("paused"),

    @SerializedName("error")
    ERROR("error"),
}

@Parcelize
data class DownloadDesc(
    val id: String,
    val providerId: String,
    val sourceManga: String,
    val status: DownloadStatus
) : Parcelable