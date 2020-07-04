package com.fishhawk.driftinglibraryandroid.more

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.EmptyRefreshResultError
import com.fishhawk.driftinglibraryandroid.databinding.DownloadFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.EventObserver
import com.fishhawk.driftinglibraryandroid.util.makeSnackBar
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.launch

class DownloadFragment : Fragment() {
    private val viewModel: DownloadViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        DownloadViewModelFactory(remoteLibraryRepository)
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
        setupRefreshLayout()
        setupAdapter()
        obverseViewModel()
        viewModel.load()
    }

    private fun setupAdapter() {
        val adapter = DownloadTaskListAdapter(requireActivity())
        adapter.onStart = { id -> viewModel.startDownloadTask(id) }
        adapter.onPause = { id -> viewModel.pauseDownloadTask(id) }
        adapter.onDelete = { id -> viewModel.deleteDownloadTask(id) }
        binding.list.adapter = adapter
    }

    private fun setupRefreshLayout() {
        binding.refreshLayout.apply {
            setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = viewModel.refresh()
                override fun onFooterRefresh() {
                    isFooterRefreshing = false
                }
            })

            setHeaderColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_purple,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_cyan,
                R.color.loading_indicator_green,
                R.color.loading_indicator_yellow
            )
            setFooterColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_green,
                R.color.loading_indicator_orange
            )
        }
    }

    private fun obverseViewModel() {
        viewModel.list.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    (binding.list.adapter as DownloadTaskListAdapter).changeList(result.data)
                    if (binding.list.adapter!!.itemCount == 0) binding.multipleStatusView.showEmpty()
                    else binding.multipleStatusView.showContent()
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        })

        viewModel.refreshFinish.observe(viewLifecycleOwner, EventObserver {
            binding.refreshLayout.isHeaderRefreshing = false
        })

        viewModel.operationError.observe(viewLifecycleOwner, EventObserver { exception ->
            val message = when (exception) {
                is EmptyRefreshResultError -> getString(R.string.library_empty_hint)
                else -> exception.message ?: getString(R.string.library_unknown_error_hint)
            }
            binding.root.makeSnackBar(message)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_subscribe, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_enable_all -> {
                viewModel.startAllDownloadTasks()
                true
            }
            R.id.action_disable_all -> {
                viewModel.pauseAllDownloadTasks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}