package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.GalleryTagGroupBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.TagGroup
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class TagGroupListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<TagGroup, TagGroupListAdapter.ViewHolder>() {
    var onTagClicked: ((String, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GalleryTagGroupBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: GalleryTagGroupBinding) :
        BaseRecyclerViewAdapter.ViewHolder<TagGroup>(binding) {

        override fun bind(item: TagGroup, position: Int) {
            binding.tagKey = if (itemCount == 1 && item.key.isBlank()) null else item.key

            val adapter = TagGroupAdapter(context)
            adapter.setList(item.value)
            adapter.onTagClicked = { onTagClicked?.invoke(item.key, it) }
            binding.value.adapter = adapter
        }
    }
}