package com.fishhawk.driftinglibraryandroid.explore

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.ExploreThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.util.navToGalleryActivity

class ExploreListAdapter(
    private val activity: Activity,
    private var data: MutableList<MangaSummary>
) : RecyclerView.Adapter<ExploreListAdapter.ViewHolder>() {

    fun update(newData: MutableList<MangaSummary>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ExploreThumbnailBinding.inflate(LayoutInflater.from(activity), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: ExploreThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MangaSummary) {
            binding.title.text = item.title
            binding.thumb.transitionName = item.id
            binding.source.text = item.source
            binding.author.text = item.author
            binding.update.text = item.update

            println(item.thumb)
            Glide.with(activity).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)
            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToGalleryActivity(
                    item.id, item.title, item.thumb, "漫画人", binding.thumb
                )
            }
        }
    }
}

