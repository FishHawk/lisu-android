package com.fishhawk.driftinglibraryandroid.ui.explore

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.databinding.ExploreLatestFragmentBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.extension.changeMangaListDisplayMode

class LatestFragment : Fragment() {
    private val viewModel: LatestViewModel by viewModels {
        val source = arguments?.getString("source")!!
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        ExploreViewModelFactory(source, remoteLibraryRepository)
    }
    private lateinit var binding: ExploreLatestFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ExploreLatestFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val source = arguments?.getString("source")!!
        val adapter = MangaListAdapter(requireActivity(), source)
        adapter.onCardLongClicked = { outline ->
            createMangaOutlineActionDialog(source, outline, viewModel)
        }
        binding.mangaList.list.adapter = adapter

        SettingsHelper.displayMode.observe(viewLifecycleOwner, Observer {
            binding.mangaList.list.changeMangaListDisplayMode(adapter)
        })

        bindToListViewModel(
            binding.mangaList.multipleStatusView,
            binding.mangaList.refreshLayout,
            viewModel,
            adapter
        )
        viewModel.load()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_popular, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.setQuery("", false);
                val bundle = bundleOf(
                    "source" to arguments?.getString("source")!!,
                    "keywords" to (query ?: "")
                )
                binding.root.findNavController().navigate(R.id.action_latest_to_search, bundle)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean = true
        })

        val item = menu.findItem(R.id.action_display_mode)
        when (SettingsHelper.displayMode.getValueDirectly()) {
            SettingsHelper.DISPLAY_MODE_GRID -> {
                item.setIcon(R.drawable.ic_baseline_view_list_24)
            }
            SettingsHelper.DISPLAY_MODE_LINEAR -> {
                item.setIcon(R.drawable.ic_baseline_view_module_24)
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                when (SettingsHelper.displayMode.getValueDirectly()) {
                    SettingsHelper.DISPLAY_MODE_GRID -> {
                        item.setIcon(R.drawable.ic_baseline_view_module_24)
                        SettingsHelper.displayMode.setValue(SettingsHelper.DISPLAY_MODE_LINEAR)
                    }
                    SettingsHelper.DISPLAY_MODE_LINEAR -> {
                        item.setIcon(R.drawable.ic_baseline_view_list_24)
                        SettingsHelper.displayMode.setValue(SettingsHelper.DISPLAY_MODE_GRID)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
