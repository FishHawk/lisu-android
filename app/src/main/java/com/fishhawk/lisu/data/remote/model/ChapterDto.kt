package com.fishhawk.lisu.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChapterDto(
    val id: String,
    val name: String,
    val title: String,
    val isLocked: Boolean? = null,
    val updateTime: Long? = null
) : Parcelable