package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MangaOutline(
    val id: String,
    val title: String,
    var thumb: String,
    val author: String,
    val status: Int,
    val update:String
) : Parcelable
