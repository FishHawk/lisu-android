package com.fishhawk.driftinglibraryandroid.ui.download

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.DownloadFragmentBinding
import com.fishhawk.driftinglibraryandroid.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.ViewModelFactory


class DownloadFragment : Fragment() {
    private val viewModel: DownloadViewModel by viewModels {
        ViewModelFactory(requireActivity().application as MainApplication)
    }
    private lateinit var binding: DownloadFragmentBinding

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

        val adapter =
            DownloadTaskListAdapter(
                requireActivity()
            )
        adapter.onStart = { id -> viewModel.startDownloadTask(id) }
        adapter.onPause = { id -> viewModel.pauseDownloadTask(id) }
        adapter.onDelete = { id -> viewModel.deleteDownloadTask(id) }
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