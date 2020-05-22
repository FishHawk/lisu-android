package com.fishhawk.driftinglibraryandroid.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.databinding.GalleryChapterBinding
import com.fishhawk.driftinglibraryandroid.databinding.GalleryChapterMarkedBinding
import com.fishhawk.driftinglibraryandroid.databinding.GalleryCollectionTitleBinding
import com.fishhawk.driftinglibraryandroid.databinding.GalleryNoChapterHintBinding

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
    private val context: Context,
    private val data: List<ContentItem>,
    private val onChapterClick: (Int, Int, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ViewType(val value: Int) {
        CHAPTER(0),
        CHAPTER_MARKED(1),
        COLLECTION_HEADER(2),
        NO_CHAPTER_HINT(3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.CHAPTER.value -> ChapterViewHolder(
                GalleryChapterBinding.inflate(LayoutInflater.from(context), parent, false)
            )
            ViewType.CHAPTER_MARKED.value -> ChapterMarkedViewHolder(
                GalleryChapterMarkedBinding.inflate(LayoutInflater.from(context), parent, false)
            )
            ViewType.COLLECTION_HEADER.value -> CollectionHeaderViewHolder(
                GalleryCollectionTitleBinding.inflate(LayoutInflater.from(context), parent, false)
            )
            ViewType.NO_CHAPTER_HINT.value -> NoChapterHintViewHolder(
                GalleryNoChapterHintBinding.inflate(LayoutInflater.from(context), parent, false)
            )
            else -> throw IllegalAccessError()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ViewType.COLLECTION_HEADER.value ->
                (holder as CollectionHeaderViewHolder).bind(data[position] as ContentItem.CollectionHeader)
            ViewType.CHAPTER.value ->
                (holder as ChapterViewHolder).bind(data[position] as ContentItem.Chapter)
            ViewType.CHAPTER_MARKED.value ->
                (holder as ChapterMarkedViewHolder).bind(data[position] as ContentItem.ChapterMarked)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is ContentItem.Chapter -> ViewType.CHAPTER.value
            is ContentItem.ChapterMarked -> ViewType.CHAPTER_MARKED.value
            is ContentItem.CollectionHeader -> ViewType.COLLECTION_HEADER.value
            is ContentItem.NoChapterHint -> ViewType.NO_CHAPTER_HINT.value
        }
    }

    override fun getItemCount() = data.size


    inner class ChapterViewHolder(private val binding: GalleryChapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContentItem.Chapter) {
            binding.root.text = item.title
            binding.root.setOnClickListener {
                onChapterClick(
                    item.collectionIndex,
                    item.chapterIndex,
                    0
                )
            }
        }
    }

    inner class ChapterMarkedViewHolder(private val binding: GalleryChapterMarkedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContentItem.ChapterMarked) {
            binding.root.text = item.title
            binding.root.setOnClickListener {
                onChapterClick(
                    item.collectionIndex,
                    item.chapterIndex,
                    item.pageIndex
                )
            }
        }
    }

    inner class CollectionHeaderViewHolder(private val binding: GalleryCollectionTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContentItem.CollectionHeader) {
            binding.root.text = item.title
        }
    }

    inner class NoChapterHintViewHolder(binding: GalleryNoChapterHintBinding) :
        RecyclerView.ViewHolder(binding.root)
}
