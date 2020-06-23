package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(
    val id: Int,
    val source: String,
    val sourceMangaId: String,
    val targetMangaId: String,
    val isActive: Boolean,
    val status: String
) : Parcelable
