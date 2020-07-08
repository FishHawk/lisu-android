package com.fishhawk.driftinglibraryandroid.gallery

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.base.BaseRecyclerViewAdapter
import com.fishhawk.driftinglibraryandroid.databinding.GalleryChapterBinding
import com.fishhawk.driftinglibraryandroid.databinding.GalleryChapterMarkedBinding
import com.fishhawk.driftinglibraryandroid.databinding.GalleryCollectionTitleBinding
import com.fishhawk.driftinglibraryandroid.databinding.GalleryNoChapterHintBinding
import com.fishhawk.driftinglibraryandroid.util.navToReaderActivity

sealed class ContentItem {
    data class Chapter(
        val title: String,
        val collectionIndex: Int,
        val chapterIndex: Int
    ) : ContentItem()

    data class ChapterMarked(
        val title: String,
        val collectionIndex: Int,
        val chapterIndex: Int,
        val pageIndex: Int
    ) : ContentItem()

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
    enum class ViewType(val value: Int) {
        CHAPTER(0),
        CHAPTER_MARKED(1),
        COLLECTION_HEADER(2),
        NO_CHAPTER_HINT(3)
    }

    fun unmarkChapter() {
        val index = list.indexOfFirst { it is ContentItem.ChapterMarked }
        if (index != -1) {
            val item = list[index] as ContentItem.ChapterMarked
            list[index] = ContentItem.Chapter(
                item.title, item.collectionIndex, item.chapterIndex
            )
            notifyItemChanged(index)
        }
    }

    fun markChapter(collectionIndex: Int, chapterIndex: Int, pageIndex: Int) {
        unmarkChapter()

        val index = list.indexOfFirst {
            it is ContentItem.Chapter &&
                    it.collectionIndex == collectionIndex &&
                    it.chapterIndex == chapterIndex
        }
        if (index != -1) {
            val item = list[index] as ContentItem.Chapter
            list[index] = ContentItem.ChapterMarked(
                item.title, item.collectionIndex, item.chapterIndex, pageIndex
            )
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ContentItem> {
        return when (viewType) {
            ViewType.CHAPTER.value -> ChapterViewHolder(
                GalleryChapterBinding.inflate(LayoutInflater.from(activity), parent, false)
            )
            ViewType.CHAPTER_MARKED.value -> ChapterMarkedViewHolder(
                GalleryChapterMarkedBinding.inflate(LayoutInflater.from(activity), parent, false)
            )
            ViewType.COLLECTION_HEADER.value -> CollectionHeaderViewHolder(
                GalleryCollectionTitleBinding.inflate(LayoutInflater.from(activity), parent, false)
            )
            ViewType.NO_CHAPTER_HINT.value -> NoChapterHintViewHolder(
                GalleryNoChapterHintBinding.inflate(LayoutInflater.from(activity), parent, false)
            )
            else -> throw IllegalAccessError()
        }
    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (holder.itemViewType) {
//            ViewType.COLLECTION_HEADER.value ->
//                (holder as CollectionHeaderViewHolder).bind(data[position] as ContentItem.CollectionHeader)
//            ViewType.CHAPTER.value ->
//                (holder as ChapterViewHolder).bind(data[position] as ContentItem.Chapter)
//            ViewType.CHAPTER_MARKED.value ->
//                (holder as ChapterMarkedViewHolder).bind(data[position] as ContentItem.ChapterMarked)
//        }
//    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is ContentItem.Chapter -> ViewType.CHAPTER.value
            is ContentItem.ChapterMarked -> ViewType.CHAPTER_MARKED.value
            is ContentItem.CollectionHeader -> ViewType.COLLECTION_HEADER.value
            is ContentItem.NoChapterHint -> ViewType.NO_CHAPTER_HINT.value
        }
    }

    override fun getItemCount() = list.size


    inner class ChapterViewHolder(private val binding: GalleryChapterBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem) {
            val item = item as ContentItem.Chapter
            binding.button.text = item.title
            binding.button.setOnClickListener {
                (activity as AppCompatActivity).navToReaderActivity(
                    id,
                    source,
                    item.collectionIndex,
                    item.chapterIndex,
                    0
                )
            }
        }
    }

    inner class ChapterMarkedViewHolder(private val binding: GalleryChapterMarkedBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem) {
            val item = item as ContentItem.ChapterMarked
            binding.button.text = item.title
            binding.button.setOnClickListener {
                (activity as AppCompatActivity).navToReaderActivity(
                    id,
                    source,
                    item.collectionIndex,
                    item.chapterIndex,
                    item.pageIndex
                )
            }
        }
    }

    inner class CollectionHeaderViewHolder(private val binding: GalleryCollectionTitleBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding) {

        override fun bind(item: ContentItem) {
            val item = item as ContentItem.CollectionHeader
            binding.title.text = item.title
        }
    }

    inner class NoChapterHintViewHolder(binding: GalleryNoChapterHintBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ContentItem>(binding)
}
