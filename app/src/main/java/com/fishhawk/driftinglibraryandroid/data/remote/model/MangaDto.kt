package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MangaDto(
    val providerId: String,
    val id: String,

    var cover: String? = null,
    val updateTime: Long? = null,

    val title: String? = null,
    val authors: List<String>? = null,
    val isFinished: Boolean? = null,
) : Parcelable
