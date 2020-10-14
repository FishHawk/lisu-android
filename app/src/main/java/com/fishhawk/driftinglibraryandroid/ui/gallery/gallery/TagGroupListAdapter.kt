package com.fishhawk.driftinglibraryandroid.ui.gallery.gallery

import android.view.View
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.GalleryTagGroupBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.TagGroup
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class TagGroupListAdapter(
    private val listener: Listener
) : BaseAdapter<TagGroup>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: GalleryTagGroupBinding) :
        BaseAdapter.ViewHolder<TagGroup>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryTagGroupBinding::inflate, parent)
        )

        override fun bind(item: TagGroup, position: Int) {
            binding.key.text = item.key
            binding.key.visibility =
                if (itemCount == 1 && item.key.isBlank()) View.GONE
                else View.VISIBLE

            val adapter = TagGroupAdapter(object : TagGroupAdapter.Listener {
                override fun onTagClick(value: String) {
                    listener.onTagClick(item.key, value)
                }

                override fun onTagLongClick(value: String) {
                    listener.onTagLongClick(item.key, value)
                }
            })
            adapter.setList(item.value)
            binding.value.adapter = adapter
        }
    }

    interface Listener {
        fun onTagClick(key: String, value: String)
        fun onTagLongClick(key: String, value: String)
    }
}