package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.annotation.SuppressLint
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import java.text.SimpleDateFormat
import java.util.*

class GalleryInfo {
    val providerId: String?
    val title: String
    val author: String?
    val status: String?
    val update: String?
    val description: String?
    val hasTag: Boolean
    val hasContent: Boolean

    constructor(providerId: String?, title: String) {
        this.providerId = providerId
        this.title = title
        author = null
        status = null
        update = null
        description = null
        hasTag = false
        hasContent = false
    }

    @SuppressLint("SimpleDateFormat")
    constructor(detail: MangaDetail) {
        providerId = detail.providerId
        title = detail.metadata.title ?: detail.id
        author = detail.metadata.authors?.let {
            if (it.isEmpty()) null
            else it.joinToString(separator = ";")
        }
        status = detail.metadata.status.toString()
        update = detail.updateTime?.let {
            val date = Date(it)
            val format = SimpleDateFormat("yyyy-MM-dd")
            format.format(date)
        }
        description = detail.metadata.description?.let {
            if (it.isBlank()) null
            else it
        }
        hasTag = !(detail.metadata.tags == null || detail.metadata.tags.isEmpty())
        hasContent = detail.collections.isNotEmpty()
    }
}