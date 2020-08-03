package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private val linearLayoutManager = LinearLayoutManager(context)
    private val gridLayoutManager = GridLayoutManager(context, 3).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter?.getItemViewType(position)) {
                    ContentAdapter.ViewType.CHAPTER.value -> 1
                    ContentAdapter.ViewType.CHAPTER_MARKED.value -> 1
                    else -> 3
                }
            }
        }
    }

    init {
        layoutManager = gridLayoutManager
    }

    enum class ViewMode(val value: Int) {
        GRID(0),
        LINEAR(1)
    }

    var viewMode = ViewMode.GRID
        set(value) {
            adapter?.viewMode = when (value) {
                ViewMode.GRID -> ContentAdapter.ViewMode.GRID
                ViewMode.LINEAR -> ContentAdapter.ViewMode.GRID
            }
            layoutManager = when (value) {
                ViewMode.GRID -> gridLayoutManager
                ViewMode.LINEAR -> linearLayoutManager
            }
            adapter = adapter
            field = value
        }

    var adapter: ContentAdapter? = null
        set(value) {
            super.setAdapter(value)
            field = value
        }
}
