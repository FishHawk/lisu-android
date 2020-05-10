package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MangaSummary(
    val id: String,
    val title: String,
    var thumb: String
) : Parcelable
