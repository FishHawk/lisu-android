package com.fishhawk.lisu.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

typealias BoardModel = Map<String, List<String>>

@Parcelize
data class ProviderDto(
    val id: String,
    val lang: String,
    var icon: String?,
    val boardModels: Map<String, BoardModel>,
    val isLogged: Boolean? = null,
    val loginSite: String? = null,
) : Parcelable
