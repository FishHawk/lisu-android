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
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.*
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.launch

class SubscriptionFragment : Fragment() {
    private val viewModel: SubscriptionViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        SubscriptionViewModelFactory(remoteLibraryRepository)
    }
    private lateinit var binding: SubscriptionFragmentBinding

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
        binding.refreshLayout.bindingToViewModel(viewModel)
        setupAdapter()
        observeViewModel()
        viewModel.load()
    }

    private fun setupAdapter() {
        val adapter = SubscriptionListAdapter(requireActivity())
        adapter.onEnable = { id -> viewModel.enableSubscription(id) }
        adapter.onDisable = { id -> viewModel.disableSubscription(id) }
        adapter.onDelete = { id -> viewModel.deleteSubscription(id) }
        binding.list.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.list.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    (binding.list.adapter as SubscriptionListAdapter).changeList(result.data)
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
            showErrorMessage(exception)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_subscribe, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_enable_all -> viewModel.enableAllSubscription()
            R.id.action_disable_all -> viewModel.disableAllSubscription()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}