package com.fishhawk.driftinglibraryandroid.data.remote.model

import com.google.gson.annotations.SerializedName

enum class MangaStatus(val value: String) {
    @SerializedName("0")
    COMPLETED("0"),

    @SerializedName("1")
    ONGOING("1"),

    @SerializedName("2")
    UNKNOWN("2"),
}