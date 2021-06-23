package com.fishhawk.driftinglibraryandroid.ui.base

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.databinding.*
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import java.text.SimpleDateFormat
import java.util.*

class MangaListAdapter(
    private val listener: Listener
) : BaseAdapter<MangaOutline>() {

    // TODO: Don't need it, why?
//    override val enableDiffUtil: Boolean = true

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    enum class ViewMode(val value: Int) {
        GRID(0),
        LINEAR(1)
    }

    var viewMode = ViewMode.GRID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<MangaOutline> {
        return when (this.viewMode) {
            ViewMode.GRID -> GridViewHolder(parent)
            ViewMode.LINEAR -> LinearViewHolder(parent)
        }
    }

    inner class GridViewHolder(private val binding: MangaGridCardBinding) :
        BaseAdapter.ViewHolder<MangaOutline>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(MangaGridCardBinding::inflate, parent)
        )

        override fun bind(item: MangaOutline, position: Int) {
            binding.title.text = item.title

            binding.newMark.visibility =
                if (item.hasNewMark == true) View.VISIBLE
                else View.GONE

            Glide.with(itemView.context)
                .load(item.cover)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.cover)

            binding.root.setOnClickListener {
                listener.onCardClick(item)
                if (item.hasNewMark == true) {
                    item.hasNewMark = false
                    notifyItemChanged(position)
                }
            }
            binding.root.setOnLongClickListener {
                listener.onCardLongClick(item)
                true
            }
        }
    }

    inner class LinearViewHolder(private val binding: MangaLinearCardBinding) :
        BaseAdapter.ViewHolder<MangaOutline>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(MangaLinearCardBinding::inflate, parent)
        )

        override fun bind(item: MangaOutline, position: Int) {
            binding.title.text = item.title
            binding.author.text = item.metadata.authors?.joinToString(";")
            binding.update.text = item.updateTime?.let {
                val date = Date(item.updateTime)
                dateFormat.format(date)
            }

            binding.newMark.visibility =
                if (item.hasNewMark == true) View.VISIBLE
                else View.GONE

            Glide.with(itemView.context)
                .load(item.cover)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.cover)

            binding.root.setOnClickListener {
                listener.onCardClick(item)
                if (item.hasNewMark == true) {
                    item.hasNewMark = false
                    notifyItemChanged(position)
                }
            }
            binding.root.setOnLongClickListener {
                listener.onCardLongClick(item)
                true
            }
        }
    }

    interface Listener {
        fun onCardClick(outline: MangaOutline)
        fun onCardLongClick(outline: MangaOutline)
    }
}
