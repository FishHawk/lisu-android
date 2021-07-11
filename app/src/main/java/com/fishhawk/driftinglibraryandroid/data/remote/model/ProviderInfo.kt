package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProviderInfo(
    val id: String,
    val name: String,
    val lang: String,
    var icon: String?
) : Parcelable {
    val title
        get() = "$name($lang)"
}

