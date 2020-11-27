package com.fishhawk.driftinglibraryandroid.ui.subscription

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.base.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory

class SubscriptionFragment : Fragment() {
    private lateinit var binding: SubscriptionFragmentBinding
    private val viewModel: SubscriptionViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    val adapter = SubscriptionListAdapter(object : SubscriptionListAdapter.Listener {
        override fun onSubscriptionDelete(id: String) {
            viewModel.deleteSubscription(id)
        }

        override fun onSubscriptionEnable(id: String) {
            viewModel.enableSubscription(id)
        }

        override fun onSubscriptionDisable(id: String) {
            viewModel.disableSubscription(id)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SubscriptionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.list.adapter = adapter

        bindToListViewModel(binding.multipleStatusView, binding.refreshLayout, viewModel, adapter)
        viewModel.load()
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_enable_all -> viewModel.enableAllSubscription()
            R.id.action_disable_all -> viewModel.disableAllSubscription()
            else -> return false
        }
        return true
    }
}