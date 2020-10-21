package com.fishhawk.driftinglibraryandroid.ui.main.provider

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderPagerFragmentBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.getDisplayModeIcon
import com.fishhawk.driftinglibraryandroid.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_main.*

class ProviderPagerFragment : Fragment() {
    private lateinit var binding: ProviderPagerFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val providerName = requireArguments().getString("providerName")!!
        requireActivity().toolbar.title = providerName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProviderPagerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity

        val providerName = requireArguments().getString("providerName")!!
        activity.toolbar.title = providerName

        val providerId = requireArguments().getString("providerId")!!
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = ProviderPagerAdapter(
            requireContext(), childFragmentManager, providerId
        )
    }

    override fun onStart() {
        super.onStart()
        val activity = requireActivity() as MainActivity
        activity.enableTabs(binding.viewPager)
    }

    override fun onPause() {
        super.onPause()
        val activity = requireActivity() as MainActivity
        activity.disableTabs()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_provider, menu)

        val providerId = requireArguments().getString("providerId")!!
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_hint)
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.setQuery("", false)
                findNavController().navigate(
                    R.id.action_to_search,
                    bundleOf(
                        "providerId" to providerId,
                        "keywords" to query
                    )
                )
                return true
            }

            override fun onQueryTextChange(query: String): Boolean = true
        })

        menu.findItem(R.id.action_display_mode).setIcon(getDisplayModeIcon())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                SettingsHelper.displayMode.setNextValue()
                item.setIcon(getDisplayModeIcon())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}