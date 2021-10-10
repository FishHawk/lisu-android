package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

typealias BoardModel = Map<String, List<String>>

@Parcelize
data class Provider(
    val id: String,
    val lang: String,
    var icon: String?,
    val boardModels: Map<String, BoardModel>
) : Parcelable
