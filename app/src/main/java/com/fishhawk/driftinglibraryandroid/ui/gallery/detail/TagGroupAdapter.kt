package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.fishhawk.driftinglibraryandroid.databinding.GalleryTagItemBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class TagGroupAdapter(
    private val listener: Listener? = null,
    private val editable: Boolean = false
) : BaseAdapter<String>() {

    fun addTag(value: String) {
        val newList = list + value
        setList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    inner class ViewHolder(private val binding: GalleryTagItemBinding) :
        BaseAdapter.ViewHolder<String>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryTagItemBinding::inflate, parent)
        )

        override fun bind(item: String, position: Int) {
            binding.root.text = item
            binding.value.isCloseIconVisible = editable
            binding.value.setOnCloseIconClickListener {
                list.removeAt(position)
                notifyItemRemoved(position)
            }

            binding.value.setOnClickListener { listener?.onTagClick(item) }
            binding.value.setOnLongClickListener {
                listener?.onTagLongClick(item)
                true
            }
        }
    }

    interface Listener {
        fun onTagClick(value: String)
        fun onTagLongClick(value: String)
    }
}