package com.fishhawk.driftinglibraryandroid.library

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary

class MangaListAdapter(
    private val context: Context,
    private var data: MutableList<MangaSummary>,
    private val listener: (MangaSummary, ImageView) -> Unit
) : RecyclerView.Adapter<MangaListAdapter.ViewHolder>() {

    fun update(newData: MutableList<MangaSummary>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(View.inflate(context, R.layout.item_thumbnail_card, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val view: View = itemView
        private val thumbView: ImageView = itemView.findViewById(R.id.thumb)
        private val titleView: TextView = itemView.findViewById(R.id.title)

        fun bind(item: MangaSummary) {
            titleView.text = item.title
            thumbView.transitionName = item.id
            Glide.with(view).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(thumbView)
            view.setOnClickListener { listener(item, thumbView) }
        }
    }
}
