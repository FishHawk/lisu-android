package com.fishhawk.driftinglibraryandroid.history

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.HistoryThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.util.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.util.navToReaderActivity
import java.text.SimpleDateFormat
import java.util.*

class HistoryListAdapter(
    private val activity: Activity,
    private var data: List<ReadingHistory>
) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HistoryThumbnailBinding.inflate(LayoutInflater.from(activity), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: HistoryThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        @SuppressLint("SetTextI18n")
        fun bind(item: ReadingHistory) {
            binding.title.text = item.title

            val seenHint = when {
                item.collectionTitle.isEmpty() -> "Seen: ${item.chapterTitle} Page${item.pageIndex + 1}"
                else -> "Seen: ${item.collectionTitle} ${item.chapterTitle} Page${item.pageIndex + 1}"
            }
            binding.record.text = seenHint

            val timeHint = "Time: ${dateFormat.format(Date(item.date))}"
            binding.date.text = timeHint

            val sourceHint = "Source: ${item.source}"
            binding.source.text = sourceHint

            binding.thumb.transitionName = item.id
            Glide.with(activity).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)
            binding.thumb.setOnClickListener {
                (activity as AppCompatActivity).navToGalleryActivity(
                    item.id, item.title, item.thumb, item.source, binding.thumb
                )
            }
            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToReaderActivity(
                    item.id, item.source, item.collectionIndex, item.chapterIndex, item.pageIndex
                )
            }
        }
    }
}
