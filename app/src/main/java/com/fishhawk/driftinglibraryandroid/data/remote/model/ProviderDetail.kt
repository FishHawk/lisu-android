package com.fishhawk.driftinglibraryandroid.data.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

typealias OptionModel = Map<String, List<String>>

@Parcelize
data class OptionModels(
    val popular: OptionModel,
    val latest: OptionModel,
    val category: OptionModel
) : Parcelable


@Parcelize
data class ProviderDetail(
    val id: String,
    val name: String,
    val lang: String,
    val optionModels: OptionModels
) : Parcelable

