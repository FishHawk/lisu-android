package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class OrderMode(val value: String) {
    @SerializedName("0")
    FORCE("0"),

    @SerializedName("1")
    PASS_IF_MANGA_EXIST("1"),

    @SerializedName("2")
    PASS_IF_COLLECTION_EXIST("2"),

    @SerializedName("3")
    PASS_IF_CHAPTER_EXIST("3"),

    @SerializedName("4")
    PASS_IF_IMAGE_EXIST("4"),
}

enum class OrderStatus(val value: String) {
    @SerializedName("0")
    WAITING("0"),

    @SerializedName("1")
    PROCESSING("1"),

    @SerializedName("2")
    COMPLETED("2"),

    @SerializedName("3")
    PAUSED("3"),

    @SerializedName("4")
    ERROR("4"),
}

@Parcelize
data class Order(
    val id: Int,
    val source: String,
    val sourceMangaId: String,
    val targetMangaId: String,
    val mode: OrderMode,
    val status: OrderStatus,
    val errorMessage: String
) : Parcelable
