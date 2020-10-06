package com.fishhawk.driftinglibraryandroid.ui.main.download

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.DownloadTaskCardBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadDesc
import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadStatus
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class DownloadTaskListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<DownloadDesc, DownloadTaskListAdapter.ViewHolder>() {
    var onDeleted: ((String) -> Unit)? = null
    var onStarted: ((String) -> Unit)? = null
    var onPaused: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DownloadTaskCardBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: DownloadTaskCardBinding) :
        BaseRecyclerViewAdapter.ViewHolder<DownloadDesc>(binding) {

        private fun hideActions() {
            binding.actionPanel.visibility = View.INVISIBLE
            binding.actionProgressBar.visibility = View.VISIBLE
        }

        private fun showActions() {
            binding.actionProgressBar.visibility = View.INVISIBLE
            binding.actionPanel.visibility = View.VISIBLE
        }

        override fun bind(item: DownloadDesc, position: Int) {
            binding.downloadTask = item
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
                onStarted?.invoke(item.id)
                hideActions()
            }
            binding.pauseButton.setOnClickListener {
                onPaused?.invoke(item.id)
                hideActions()
            }
            binding.deleteButton.setOnClickListener {
                onDeleted?.invoke(item.id)
                hideActions()
            }
        }
    }
}
