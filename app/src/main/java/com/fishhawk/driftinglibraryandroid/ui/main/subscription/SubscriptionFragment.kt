package com.fishhawk.driftinglibraryandroid.ui.main.subscription

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory

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