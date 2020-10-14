package com.fishhawk.driftinglibraryandroid.ui.main.download

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.DownloadFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DownloadFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.list.adapter = adapter

        bindToListViewModel(binding.multipleStatusView, binding.refreshLayout, viewModel, adapter)
        viewModel.load()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_subscribe, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_start_all -> viewModel.startAllDownloadTasks()
            R.id.action_pause_all -> viewModel.pauseAllDownloadTasks()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}