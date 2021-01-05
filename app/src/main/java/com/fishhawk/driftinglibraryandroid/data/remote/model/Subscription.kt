package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val id: String,
    val providerId: String,
    val sourceManga: String,
    var isEnabled: Boolean
) : Parcelable
