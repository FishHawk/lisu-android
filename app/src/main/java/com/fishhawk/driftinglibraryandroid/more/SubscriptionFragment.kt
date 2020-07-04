package com.fishhawk.driftinglibraryandroid.more

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionFragmentBinding
import com.fishhawk.driftinglibraryandroid.util.*

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

        val adapter = SubscriptionListAdapter(requireActivity())
        adapter.onEnable = { id -> viewModel.enableSubscription(id) }
        adapter.onDisable = { id -> viewModel.disableSubscription(id) }
        adapter.onDelete = { id -> viewModel.deleteSubscription(id) }
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
            R.id.action_enable_all -> viewModel.enableAllSubscription()
            R.id.action_disable_all -> viewModel.disableAllSubscription()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}