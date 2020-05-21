package com.fishhawk.driftinglibraryandroid.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.HistoryThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import java.text.SimpleDateFormat
import java.util.*

class HistoryListAdapter(
    private val context: Context,
    private var data: List<ReadingHistory>,
    private val listener: (ReadingHistory, ImageView) -> Unit
) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HistoryThumbnailBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: HistoryThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        fun bind(item: ReadingHistory) {
            binding.title.text = item.title
            if (item.collectionTitle.isEmpty())
                binding.record.text = "Seen: ${item.chapterTitle} Page${item.pageIndex + 1}"
            else
                binding.record.text =
                    "Seen: ${item.collectionTitle} ${item.chapterTitle} Page${item.pageIndex + 1}"
            binding.date.text = "Time: " + dateFormat.format(Date(item.date))

            binding.thumb.transitionName = item.id
            Glide.with(context).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)
            binding.root.setOnClickListener { listener(item, binding.thumb) }
        }
    }
}
