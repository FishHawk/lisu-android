package com.fishhawk.driftinglibraryandroid.ui.gallery

import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail


class GalleryInfo {
    val source: String?
    val title: String
    val author: String?
    val status: String?
    val update: String?
    val description: String?
    val hasTag: Boolean
    val hasContent: Boolean

    constructor(source: String?, title: String) {
        this.source = source
        this.title = title
        author = null
        status = null
        update = null
        description = null
        hasTag = false
        hasContent = false
    }

    constructor(detail: MangaDetail) {
        source = detail.source
        title = detail.title
        author = detail.author?.let {
            if (it.isEmpty()) null
            else it.joinToString(separator = ";")
        }
        status = detail.status.toString()
        update = detail.update
        description = detail.description?.let {
            if (it.isBlank()) null
            else it
        }
        hasTag = !(detail.tags == null || detail.tags.isEmpty())
        hasContent = detail.collections.isNotEmpty()
    }
}