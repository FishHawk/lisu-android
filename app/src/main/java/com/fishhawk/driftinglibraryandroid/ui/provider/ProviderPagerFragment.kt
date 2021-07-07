package com.fishhawk.driftinglibraryandroid.ui.provider

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.preference.ProviderBrowseHistory
import com.fishhawk.driftinglibraryandroid.databinding.ProviderPagerFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.util.setNext

class ProviderPagerFragment : Fragment() {
    private lateinit var binding: ProviderPagerFragmentBinding
    private val viewModel: ProviderViewModel by viewModels {
        MainViewModelFactory(this)
    }

    private lateinit var providerBrowseHistory: ProviderBrowseHistory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProviderPagerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        providerBrowseHistory = ProviderBrowseHistory(requireContext())

        setupMenu(binding.toolbar.menu)
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.toolbar.title = viewModel.provider.title
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = ProviderPagerAdapter(
            requireContext(),
            childFragmentManager,
            requireArguments()
        )
        binding.viewPager.currentItem =
            providerBrowseHistory.getPageHistory(viewModel.provider.id).coerceIn(0, 2)
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    override fun onPause() {
        super.onPause()
        providerBrowseHistory.setPageHistory(viewModel.provider.id, binding.viewPager.currentItem)
    }

    private fun setupMenu(menu: Menu) {
        with(menu.findItem(R.id.action_search).actionView as SearchView) {
            queryHint = getString(R.string.menu_search_hint)
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    isIconified = true
                    isIconified = true
                    findNavController().navigate(
                        R.id.action_to_search,
                        bundleOf(
                            "provider" to viewModel.provider,
                            "keywords" to query
                        )
                    )
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean = true
            })
            setOnQueryTextFocusChangeListener { _, b ->
                if (!b && query.isNullOrBlank()) {
                    isIconified = true
                    isIconified = true
                }
            }
        }

        menu.findItem(R.id.action_display_mode).setIcon(getDisplayModeIcon())
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                GlobalPreference.displayMode.setNext()
                item.setIcon(getDisplayModeIcon())
                true
            }
            else -> false
        }
    }
}

fun getDisplayModeIcon(): Int {
    return when (GlobalPreference.displayMode.get()) {
        GlobalPreference.DisplayMode.GRID -> R.drawable.ic_baseline_view_module_24
        GlobalPreference.DisplayMode.LINEAR -> R.drawable.ic_baseline_view_list_24
    }
}
