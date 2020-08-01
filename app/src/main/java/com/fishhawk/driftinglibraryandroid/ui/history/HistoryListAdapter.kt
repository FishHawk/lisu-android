package com.fishhawk.driftinglibraryandroid.ui.history

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.HistoryThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*


class HistoryListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<ReadingHistory, HistoryListAdapter.ViewHolder>(mutableListOf()) {
    var onThumbClicked: (ReadingHistory) -> Unit = {}
    var onCardClicked: (ReadingHistory) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HistoryThumbnailBinding.inflate(LayoutInflater.from(activity), parent, false)
        )
    }

    inner class ViewHolder(private val binding: HistoryThumbnailBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ReadingHistory>(binding) {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        @SuppressLint("SetTextI18n")
        override fun bind(item: ReadingHistory, position: Int) {
            binding.readingHistory = item

            val seenHint = when {
                item.collectionTitle.isEmpty() -> "${item.chapterTitle} Page${item.pageIndex + 1}"
                else -> "${item.collectionTitle} ${item.chapterTitle} Page${item.pageIndex + 1}"
            }
            binding.seen.text = seenHint
            binding.date.text = dateFormat.format(Date(item.date))

            Glide.with(activity).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)
            binding.thumb.setOnClickListener { onThumbClicked(item) }
            binding.root.setOnClickListener { onCardClicked(item) }
        }
    }
}
