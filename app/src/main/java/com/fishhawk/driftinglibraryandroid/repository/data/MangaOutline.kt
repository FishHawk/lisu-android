package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class MangaStatus(val value: String) {
    @SerializedName("completed")
    COMPLETED("completed"),

    @SerializedName("ongoing")
    ONGOING("ongoing"),

    @SerializedName("unknown")
    UNKNOWN("unknown"),
}

@Parcelize
data class MangaOutline(
    val id: String,
    val title: String,
    var thumb: String,
    val author: String?,
    val status: MangaStatus?,
    val update: Long?
) : Parcelable
