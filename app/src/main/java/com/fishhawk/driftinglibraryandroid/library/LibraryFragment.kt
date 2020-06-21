package com.fishhawk.driftinglibraryandroid.library

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.databinding.LibraryFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.EventObserver
import com.fishhawk.driftinglibraryandroid.util.SpacingItemDecoration
import com.fishhawk.driftinglibraryandroid.util.makeSnackBar
import com.hippo.refreshlayout.RefreshLayout


class LibraryFragment : Fragment() {
    private val viewModel: LibraryViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        LibraryViewModelFactory(remoteLibraryRepository)
    }
    private lateinit var binding: LibraryFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LibraryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mangaList.setup(viewModel, requireActivity())

        SettingsHelper.displayMode.observe(viewLifecycleOwner, Observer {
            binding.mangaList.updateMangaListDisplayMode()
        })

        viewModel.mangaList.observe(viewLifecycleOwner, Observer { result ->
            binding.mangaList.onMangaListChanged(result)
        })

        viewModel.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver { exception ->
            binding.mangaList.onFetchMoreFinishEvent(exception)
        })

        viewModel.refreshFinish.observe(viewLifecycleOwner, EventObserver { exception ->
            binding.mangaList.onRefreshFinishEvent(exception)
        })

        val filter: String? = arguments?.getString("filter")
        viewModel.reloadIfNeed(filter ?: "")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_library, menu)

        val searchView: SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_hint)
        if (viewModel.filter != "") searchView.setQuery(viewModel.filter, false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.reload(query ?: "")
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return true
            }
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