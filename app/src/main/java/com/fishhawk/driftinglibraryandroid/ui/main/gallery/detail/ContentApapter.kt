package com.fishhawk.driftinglibraryandroid.ui.main.gallery.detail

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fishhawk.driftinglibraryandroid.databinding.*
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter
import com.fishhawk.driftinglibraryandroid.ui.extension.navToReaderActivity

data class MarkedPosition(
    val collectionIndex: Int,
    val chapterIndex: Int,
    val pageIndex: Int
)

sealed class ContentItem {
    data class Chapter(
        val name: String,
        val title: String,
        val collectionIndex: Int,
        val chapterIndex: Int
    ) : ContentItem() {
        fun marked(pageIndex: Int): ChapterMarked {
            return ChapterMarked(name, title, collectionIndex, chapterIndex, pageIndex)
        }
    }

    data class ChapterMarked(
        val name: String,
        val title: String,
        val collectionIndex: Int,
        val chapterIndex: Int,
        val pageIndex: Int
    ) : ContentItem() {
        fun unmarked(): Chapter {
            return Chapter(name, title, collectionIndex, chapterIndex)
        }
    }

    data class CollectionHeader(val title: String) : ContentItem()
    object NoChapterHint : ContentItem()
}

class ContentAdapter(
    private val fragment: Fragment,
    private val id: String,
    private val providerId: String?
) : BaseAdapter<ContentItem>() {
    enum class ViewMode(val value: Int) {
        GRID(0),
        LINEAR(1)
    }

    var viewMode = ViewMode.GRID

    fun unmarkChapter() {
        val index = list.indexOfFirst { it is ContentItem.ChapterMarked }
        if (index != -1) {
            list[index] = (list[index] as ContentItem.ChapterMarked).unmarked()
            notifyItemChanged(index)
        }
    }

    fun markChapter(markedPosition: MarkedPosition) {
        unmarkChapter()

        val index = list.indexOfFirst {
            it is ContentItem.Chapter &&
                    it.collectionIndex == markedPosition.collectionIndex &&
                    it.chapterIndex == markedPosition.chapterIndex
        }
        if (index != -1) {
            list[index] = (list[index] as ContentItem.Chapter).marked(markedPosition.pageIndex)
            notifyItemChanged(index)
        }
    }

    enum class ViewType(val value: Int) {
        CHAPTER(0),
        CHAPTER_MARKED(1),
        COLLECTION_HEADER(2),
        NO_CHAPTER_HINT(3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ContentItem> {
        return when (viewType) {
            ViewType.CHAPTER.value ->
                when (viewMode) {
                    ViewMode.GRID -> ChapterViewHolder(parent)
                    ViewMode.LINEAR -> ChapterLinearViewHolder(parent)
                }
            ViewType.CHAPTER_MARKED.value ->
                when (viewMode) {
                    ViewMode.GRID -> ChapterMarkedViewHolder(parent)
                    ViewMode.LINEAR -> ChapterLinearMarkedViewHolder(parent)
                }
            ViewType.COLLECTION_HEADER.value -> CollectionHeaderViewHolder(parent)
            ViewType.NO_CHAPTER_HINT.value -> NoChapterHintViewHolder(parent)
            else -> throw IllegalAccessError()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is ContentItem.Chapter -> ViewType.CHAPTER.value
            is ContentItem.ChapterMarked -> ViewType.CHAPTER_MARKED.value
            is ContentItem.CollectionHeader -> ViewType.COLLECTION_HEADER.value
            is ContentItem.NoChapterHint -> ViewType.NO_CHAPTER_HINT.value
        }
    }

    override fun getItemCount() = list.size

    inner class ChapterViewHolder(private val binding: GalleryChapterGridBinding) :
        BaseAdapter.ViewHolder<ContentItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryChapterGridBinding::inflate, parent)
        )

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.Chapter
            binding.button.text = newItem.name
            binding.button.setOnClickListener {
                fragment.navToReaderActivity(
                    id,
                    providerId,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    0
                )
            }
        }
    }

    inner class ChapterMarkedViewHolder(private val binding: GalleryChapterGridMarkedBinding) :
        BaseAdapter.ViewHolder<ContentItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryChapterGridMarkedBinding::inflate, parent)
        )

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.ChapterMarked
            binding.button.text = newItem.name
            binding.button.setOnClickListener {
                fragment.navToReaderActivity(
                    id,
                    providerId,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    newItem.pageIndex
                )
            }
        }
    }

    inner class ChapterLinearViewHolder(private val binding: GalleryChapterLinearBinding) :
        BaseAdapter.ViewHolder<ContentItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryChapterLinearBinding::inflate, parent)
        )

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.Chapter
            binding.name.text = newItem.name
            binding.title.text = newItem.title
            binding.root.setOnClickListener {
                fragment.navToReaderActivity(
                    id,
                    providerId,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    0
                )
            }
        }
    }

    inner class ChapterLinearMarkedViewHolder(private val binding: GalleryChapterLinearMarkedBinding) :
        BaseAdapter.ViewHolder<ContentItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryChapterLinearMarkedBinding::inflate, parent)
        )

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.ChapterMarked
            binding.name.text = newItem.name
            binding.title.text = newItem.title
            binding.root.setOnClickListener {
                fragment.navToReaderActivity(
                    id,
                    providerId,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    0
                )
            }
        }
    }

    inner class CollectionHeaderViewHolder(private val binding: GalleryContentCollectionHeaderBinding) :
        BaseAdapter.ViewHolder<ContentItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryContentCollectionHeaderBinding::inflate, parent)
        )

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.CollectionHeader
            binding.title.text = newItem.title
        }
    }

    inner class NoChapterHintViewHolder(binding: GalleryContentNoChapterBinding) :
        BaseAdapter.ViewHolder<ContentItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryContentNoChapterBinding::inflate, parent)
        )
    }
}
