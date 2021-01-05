package com.fishhawk.driftinglibraryandroid.ui.download

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.DownloadFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.bindToRemoteList

class DownloadFragment : Fragment() {
    private lateinit var binding: DownloadFragmentBinding
    private val viewModel: DownloadViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    val adapter = DownloadTaskListAdapter(object : DownloadTaskListAdapter.Listener {
        override fun onDownloadTaskDelete(id: String) {
            viewModel.deleteDownloadTask(id)
        }

        override fun onDownloadTaskStart(id: String) {
            viewModel.startDownloadTask(id)
        }

        override fun onDownloadTaskPause(id: String) {
            viewModel.pauseDownloadTask(id)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DownloadFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.recyclerView.adapter = adapter

        viewModel.downloads.reload()
        viewModel.downloads.data.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        viewModel.downloads.state.observe(viewLifecycleOwner) {
            binding.multiStateView.viewState = it
        }
        bindToRemoteList(binding.refreshLayout, viewModel.downloads)
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_start_all -> viewModel.startAllDownloadTasks()
            R.id.action_pause_all -> viewModel.pauseAllDownloadTasks()
            else -> return false
        }
        return true
    }
}