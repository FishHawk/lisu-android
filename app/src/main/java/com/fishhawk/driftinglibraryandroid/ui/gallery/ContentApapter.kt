package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.databinding.*
import com.fishhawk.driftinglibraryandroid.extension.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter


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
    private val activity: Activity,
    private val id: String,
    private val source: String?
) : BaseRecyclerViewAdapter<ContentItem, BaseRecyclerViewAdapter.ViewHolder<ContentItem>>(
    mutableListOf()
) {
    enum class ViewMode(val value: Int) {
        GRID(0),
        LINEAR(1)
    }

    var viewMode = ViewMode.GRID

    enum class ViewType(val value: Int) {
        CHAPTER(0),
        CHAPTER_MARKED(1),
        COLLECTION_HEADER(2),
        NO_CHAPTER_HINT(3)
    }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ContentItem> {
        return when (viewType) {
            ViewType.CHAPTER.value ->
                when (viewMode) {
                    ViewMode.GRID ->
                        ChapterViewHolder(
                            GalleryChapterGridBinding.inflate(
                                LayoutInflater.from(activity), parent, false
                            )
                        )
                    ViewMode.LINEAR ->
                        ChapterLinearViewHolder(
                            GalleryChapterLinearBinding.inflate(
                                LayoutInflater.from(activity), parent, false
                            )
                        )
                }
            ViewType.CHAPTER_MARKED.value ->

                when (viewMode) {
                    ViewMode.GRID ->
                        ChapterMarkedViewHolder(
                            GalleryChapterGridMarkedBinding.inflate(
                                LayoutInflater.from(activity), parent, false
                            )
                        )
                    ViewMode.LINEAR ->
                        ChapterLinearMarkedViewHolder(
                            GalleryChapterLinearMarkedBinding.inflate(
                                LayoutInflater.from(activity), parent, false
                            )
                        )
                }
            ViewType.COLLECTION_HEADER.value -> CollectionHeaderViewHolder(
                GalleryCollectionTitleBinding.inflate(LayoutInflater.from(activity), parent, false)
            )
            ViewType.NO_CHAPTER_HINT.value -> NoChapterHintViewHolder(
                GalleryNoChapterHintBinding.inflate(LayoutInflater.from(activity), parent, false)
            )
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
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.Chapter
            binding.button.text = newItem.name
            binding.button.setOnClickListener {
                (activity as AppCompatActivity).navToReaderActivity(
                    id,
                    source,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    0
                )
            }
        }
    }

    inner class ChapterMarkedViewHolder(private val binding: GalleryChapterGridMarkedBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.ChapterMarked
            binding.button.text = newItem.name
            binding.button.setOnClickListener {
                (activity as AppCompatActivity).navToReaderActivity(
                    id,
                    source,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    newItem.pageIndex
                )
            }
        }
    }

    inner class ChapterLinearViewHolder(private val binding: GalleryChapterLinearBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.Chapter
            binding.name.text = newItem.name
            binding.title.text = newItem.title
            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToReaderActivity(
                    id,
                    source,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    0
                )
            }
        }
    }

    inner class ChapterLinearMarkedViewHolder(private val binding: GalleryChapterLinearMarkedBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.ChapterMarked
            binding.name.text = newItem.name
            binding.title.text = newItem.title
            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToReaderActivity(
                    id,
                    source,
                    newItem.collectionIndex,
                    newItem.chapterIndex,
                    0
                )
            }
        }
    }

    inner class CollectionHeaderViewHolder(private val binding: GalleryCollectionTitleBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem, position: Int) {
            val newItem = item as ContentItem.CollectionHeader
            binding.title.text = newItem.title
        }
    }

    inner class NoChapterHintViewHolder(binding: GalleryNoChapterHintBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding)
}
