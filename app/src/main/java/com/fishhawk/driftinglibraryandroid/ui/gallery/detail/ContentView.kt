package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.data.remote.model.ChapterCollection

class ContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private val linearLayoutManager = LinearLayoutManager(context)

    private val gridSpanCount = 4
    private val gridLayoutManager = GridLayoutManager(context, gridSpanCount).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter?.getItemViewType(position)) {
                    ContentAdapter.ViewType.CHAPTER.value -> 1
                    ContentAdapter.ViewType.CHAPTER_MARKED.value -> 1
                    else -> gridSpanCount
                }
            }
        }
    }

    init {
        layoutManager = gridLayoutManager
    }

    enum class ViewMode { GRID, LINEAR }
    enum class ViewOrder { ASCEND, DESCEND }

    var viewMode = ViewMode.GRID
        set(value) {
            field = value
            adapter?.viewMode = when (value) {
                ViewMode.GRID -> ContentAdapter.ViewMode.GRID
                ViewMode.LINEAR -> ContentAdapter.ViewMode.LINEAR
            }
            layoutManager = when (value) {
                ViewMode.GRID -> gridLayoutManager
                ViewMode.LINEAR -> linearLayoutManager
            }
            adapter = adapter
        }

    var viewOrder = ViewOrder.ASCEND
        set(value) {
            field = value
            adapter = adapter
        }

    var adapter: ContentAdapter? = null
        set(value) {
            field = value
            updateAdapterContent()
            updateAdapterMarkedPosition()
            super.setAdapter(value)
        }

    var markedPosition: MarkedPosition? = null
        set(value) {
            field = value
            updateAdapterMarkedPosition()
        }

    private fun updateAdapterMarkedPosition() {
        markedPosition?.let {
            adapter?.markChapter(it)
        } ?: adapter?.unmarkChapter()
    }

    var collections: List<ChapterCollection>? = null
        set(value) {
            field = value
            adapter = adapter
        }

    private fun updateAdapterContent() {
        if (adapter == null || collections == null) return

        val items = mutableListOf<ContentItem>()

        for ((collectionIndex, collection) in collections!!.withIndex()) {
            if (viewMode == ViewMode.GRID && collection.id.isNotEmpty())
                items.add(ContentItem.CollectionHeader(collection.id))

            val itemsInCollection = mutableListOf<ContentItem>()
            for ((chapterIndex, chapter) in collection.chapters.withIndex()) {
                val contentItem = ContentItem.Chapter(
                    chapter.name, chapter.title, collectionIndex, chapterIndex
                )
                when (viewOrder) {
                    ViewOrder.ASCEND -> itemsInCollection.add(contentItem)
                    ViewOrder.DESCEND -> itemsInCollection.add(0, contentItem)
                }
            }
            items.addAll(itemsInCollection)
        }
        adapter!!.setList(items)
    }
}
