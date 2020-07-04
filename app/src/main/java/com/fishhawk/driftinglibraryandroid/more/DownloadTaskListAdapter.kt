package com.fishhawk.driftinglibraryandroid.more

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.base.BaseRecyclerViewAdapter
import com.fishhawk.driftinglibraryandroid.databinding.DownloadTaskCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.DownloadTask
import com.fishhawk.driftinglibraryandroid.repository.data.DownloadTaskStatus


class DownloadTaskListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<DownloadTask, DownloadTaskListAdapter.ViewHolder>(mutableListOf()) {
    var onDelete: (Int) -> Unit = {}
    var onStart: (Int) -> Unit = {}
    var onPause: (Int) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadTaskListAdapter.ViewHolder {
        return ViewHolder(
            DownloadTaskCardBinding.inflate(
                LayoutInflater.from(activity),
                parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: DownloadTaskCardBinding) :
        BaseRecyclerViewAdapter.ViewHolder<DownloadTask>(binding) {

        private fun hideActions() {
            binding.actionPanel.visibility = View.INVISIBLE
            binding.actionProgressBar.visibility = View.VISIBLE
        }

        private fun showActions() {
            binding.actionProgressBar.visibility = View.INVISIBLE
            binding.actionPanel.visibility = View.VISIBLE
        }

        override fun bind(item: DownloadTask) {
            binding.downloadTask = item
            showActions()

            when (item.status) {
                DownloadTaskStatus.PAUSED, DownloadTaskStatus.ERROR -> {
                    binding.pauseButton.visibility = View.GONE
                }
                DownloadTaskStatus.WAITING, DownloadTaskStatus.DOWNLOADING -> {
                    binding.startButton.visibility = View.GONE
                }
            }

            binding.startButton.setOnClickListener {
                onStart(item.id)
                hideActions()
            }
            binding.pauseButton.setOnClickListener {
                onPause(item.id)
                hideActions()
            }
            binding.deleteButton.setOnClickListener {
                onDelete(item.id)
                hideActions()
            }
        }
    }
}
