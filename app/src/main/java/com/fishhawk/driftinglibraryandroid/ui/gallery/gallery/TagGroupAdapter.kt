package com.fishhawk.driftinglibraryandroid.ui.gallery.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.fishhawk.driftinglibraryandroid.databinding.GalleryTagItemBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class TagGroupAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<String, TagGroupAdapter.ViewHolder>() {
    var onTagClicked: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GalleryTagItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    inner class ViewHolder(private val binding: GalleryTagItemBinding) :
        BaseRecyclerViewAdapter.ViewHolder<String>(binding) {

        override fun bind(item: String, position: Int) {
            binding.tagValue = item
            binding.root.setOnClickListener { onTagClicked?.invoke(item) }
        }
    }
}