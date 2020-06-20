package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MangaSummary(
    val source: String,
    val id: String,
    val title: String,
    var thumb: String,
    val author: String,
    val status: Int,
    val update:String
) : Parcelable
