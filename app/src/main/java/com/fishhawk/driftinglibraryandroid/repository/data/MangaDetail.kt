package com.fishhawk.driftinglibraryandroid.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagGroup(
    val key: String,
    val value: List<String>
) : Parcelable

@Parcelize
data class Chapter(
    val id: String,
    val name: String,
    val title: String
) : Parcelable

@Parcelize
data class Collection(
    val title: String,
    val chapters: List<Chapter>
) : Parcelable

@Parcelize
data class MangaDetail(
    val source: String?,
    val id: String,
    val title: String,
    var thumb: String,
    val status: MangaStatus?,
    val author: List<String>?,
    val update: Long?,
    val description: String?,

    val tags: List<TagGroup>?,
    val collections: List<Collection>
) : Parcelable
