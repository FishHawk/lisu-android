package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Subscription(
    val id: Int,
    val source: String,
    val sourceManga: String,
    val targetManga: String,
    var isEnabled: Boolean
) : Parcelable
