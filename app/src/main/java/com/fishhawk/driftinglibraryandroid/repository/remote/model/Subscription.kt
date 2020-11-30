package com.fishhawk.driftinglibraryandroid.repository.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val id: String,
    val providerId: String,
    val sourceManga: String,
    var isEnabled: Boolean
) : Parcelable
