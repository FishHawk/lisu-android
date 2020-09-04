package com.fishhawk.driftinglibraryandroid.repository.remote.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Subscription(
    val id: Int,
    val providerId: String,
    val sourceManga: String,
    val targetManga: String,
    var isEnabled: Boolean
) : Parcelable
