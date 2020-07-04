package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class DownloadTaskStatus(val value: String) {
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
data class DownloadTask(
    val id: Int,
    val source: String,
    val sourceManga: String,
    val targetManga: String,
    val status: DownloadTaskStatus,
    val isCreatedBySubscription: Boolean
) : Parcelable