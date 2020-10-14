package com.fishhawk.driftinglibraryandroid.ui.main.history

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.HistoryThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter
import java.text.SimpleDateFormat
import java.util.*

class HistoryListAdapter(
    private val listener: Listener
) : BaseAdapter<ReadingHistory>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: HistoryThumbnailBinding) :
        BaseAdapter.ViewHolder<ReadingHistory>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(HistoryThumbnailBinding::inflate, parent)
        )

        @SuppressLint("SetTextI18n")
        override fun bind(item: ReadingHistory, position: Int) {
            binding.title.text = item.title

            binding.provider.text = item.providerId
            binding.providerLabel.visibility =
                if (item.providerId != null) View.VISIBLE else View.GONE

            val seenHint = when {
                item.collectionTitle.isEmpty() -> "${item.chapterTitle} Page${item.pageIndex + 1}"
                else -> "${item.collectionTitle} ${item.chapterTitle} Page${item.pageIndex + 1}"
            }
            binding.seen.text = seenHint
            binding.date.text = dateFormat.format(Date(item.date))

            Glide.with(itemView.context).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)

            binding.thumb.setOnClickListener { listener.onThumbClicked(item) }
            binding.root.setOnClickListener { listener.onCardClicked(item) }
        }
    }

    interface Listener {
        fun onThumbClicked(history: ReadingHistory)
        fun onCardClicked(history: ReadingHistory)
    }
}
