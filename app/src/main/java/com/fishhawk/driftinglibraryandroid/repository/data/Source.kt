package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Source(
    val name: String,
    val lang: String,
    val isLatestSupport: Boolean
) : Parcelable

