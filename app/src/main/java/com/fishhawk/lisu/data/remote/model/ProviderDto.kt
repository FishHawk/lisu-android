package com.fishhawk.lisu.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

typealias BoardModel = Map<String, List<String>>

@Serializable
@Parcelize
data class ProviderDto(
    val id: String,
    val lang: String,
    var icon: String? = null,
    val boardModels: Map<String, BoardModel>,
    val isLogged: Boolean? = null,
    val loginSite: String? = null,
) : Parcelable
