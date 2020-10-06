package com.fishhawk.driftinglibraryandroid.ui.main.history

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*
import com.fishhawk.driftinglibraryandroid.databinding.HistoryThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class HistoryListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<ReadingHistory, HistoryListAdapter.ViewHolder>() {
    var onThumbClicked: ((ReadingHistory) -> Unit)? = null
    var onCardClicked: ((ReadingHistory) -> Unit)? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HistoryThumbnailBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: HistoryThumbnailBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ReadingHistory>(binding) {

        @SuppressLint("SetTextI18n")
        override fun bind(item: ReadingHistory, position: Int) {
            binding.readingHistory = item

            val seenHint = when {
                item.collectionTitle.isEmpty() -> "${item.chapterTitle} Page${item.pageIndex + 1}"
                else -> "${item.collectionTitle} ${item.chapterTitle} Page${item.pageIndex + 1}"
            }
            binding.seen.text = seenHint
            binding.date.text = dateFormat.format(Date(item.date))

            Glide.with(context).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)
            binding.thumb.setOnClickListener { onThumbClicked?.invoke(item) }
            binding.root.setOnClickListener { onCardClicked?.invoke(item) }
        }
    }
}
