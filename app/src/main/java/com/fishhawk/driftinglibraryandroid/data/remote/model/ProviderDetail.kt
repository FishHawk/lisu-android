package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OptionModels(
    val popular: Map<String, List<String>>,
    val latest: Map<String, List<String>>,
    val category: Map<String, List<String>>
) : Parcelable


@Parcelize
data class ProviderDetail(
    val id: String,
    val name: String,
    val lang: String,
    val optionModels: OptionModels
) : Parcelable

