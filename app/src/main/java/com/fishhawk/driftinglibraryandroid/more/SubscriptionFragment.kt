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
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionFragmentBinding
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.EventObserver
import com.fishhawk.driftinglibraryandroid.util.makeSnackBar
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.launch

class SubscriptionFragment : Fragment() {
    private val viewModel: SubscriptionViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        SubscriptionViewModelFactory(remoteLibraryRepository)
    }
    private lateinit var binding: SubscriptionFragmentBinding

    private val adapter by lazy { SubscriptionListAdapter(requireActivity(), mutableListOf()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SubscriptionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRefreshLayout()
        setupAdapter()
        observeViewModel()
        viewModel.load()
    }

    private fun setupRefreshLayout() {
        binding.refreshLayout.apply {
            setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = viewModel.refresh()
                override fun onFooterRefresh() {
                    binding.refreshLayout.isFooterRefreshing = false
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

    private fun setupAdapter() {
        val onError: (Int, String?) -> Unit = { id: Int, message: String? ->
            adapter.refreshSubscription(id)
            message?.let { binding.root.makeSnackBar(it) }
        }

        adapter.onEnable = { id ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = viewModel.enableSubscription(id)) {
                    is Result.Success -> adapter.enableSubscription(id)
                    is Result.Error -> onError(id, result.exception.message)
                }
            }
        }

        adapter.onDisable = { id ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = viewModel.disableSubscription(id)) {
                    is Result.Success -> adapter.disableSubscription(id)
                    is Result.Error -> onError(id, result.exception.message)
                }
            }
        }

        adapter.onDelete = { id ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = viewModel.deleteSubscription(id)) {
                    is Result.Success -> adapter.deleteSubscription(id)
                    is Result.Error -> onError(id, result.exception.message)
                }
            }
        }

        binding.list.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.list.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    adapter.changeList(result.data)
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
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = viewModel.enableAllSubscription()
                    if (result is Result.Error) binding.root.makeSnackBar(
                        result.exception.message ?: "Unknown error"
                    )
                }
                true
            }
            R.id.action_disable_all -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = viewModel.disableAllSubscription()
                    if (result is Result.Error) binding.root.makeSnackBar(
                        result.exception.message ?: "Unknown error"
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}