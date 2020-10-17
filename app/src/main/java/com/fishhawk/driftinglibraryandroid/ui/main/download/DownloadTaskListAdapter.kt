package com.fishhawk.driftinglibraryandroid.ui.main.download

import android.view.View
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.DownloadTaskCardBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadDesc
import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadStatus
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class DownloadTaskListAdapter(
    private val listener: Listener
) : BaseAdapter<DownloadDesc>() {

    override val enableDiffUtil: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: DownloadTaskCardBinding) :
        BaseAdapter.ViewHolder<DownloadDesc>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(DownloadTaskCardBinding::inflate, parent)
        )

        private fun hideActions() {
            binding.actionPanel.visibility = View.INVISIBLE
            binding.actionProgressBar.visibility = View.VISIBLE
        }

        private fun showActions() {
            binding.actionProgressBar.visibility = View.INVISIBLE
            binding.actionPanel.visibility = View.VISIBLE
        }

        override fun bind(item: DownloadDesc, position: Int) {
            binding.targetManga.text = item.id
            binding.provider.text = item.providerId
            binding.sourceManga.text = item.sourceManga

            showActions()

            binding.status.text = item.status.value
            when (item.status) {
                DownloadStatus.PAUSED, DownloadStatus.ERROR -> {
                    binding.pauseButton.visibility = View.GONE
                    binding.startButton.visibility = View.VISIBLE
                }
                DownloadStatus.WAITING, DownloadStatus.DOWNLOADING -> {
                    binding.startButton.visibility = View.GONE
                    binding.pauseButton.visibility = View.VISIBLE
                }
            }

            binding.startButton.setOnClickListener {
                listener.onDownloadTaskStart(item.id)
                hideActions()
            }
            binding.pauseButton.setOnClickListener {
                listener.onDownloadTaskPause(item.id)
                hideActions()
            }
            binding.deleteButton.setOnClickListener {
                listener.onDownloadTaskDelete(item.id)
                hideActions()
            }
        }
    }

    interface Listener {
        fun onDownloadTaskDelete(id: String)
        fun onDownloadTaskStart(id: String)
        fun onDownloadTaskPause(id: String)
    }
}
