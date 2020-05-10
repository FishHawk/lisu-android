package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagGroup(
    val key: String,
    val value: List<String>
) : Parcelable

@Parcelize
data class Collection(
    val title: String,
    val chapters: List<String>
) : Parcelable

@Parcelize
data class MangaDetail(
    val id: String,
    val title: String,
    var thumb: String,
    val tags: List<TagGroup>,
    val collections: List<Collection>
) : Parcelable
