package com.fishhawk.driftinglibraryandroid.ui.search

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderSearchFragmentBinding
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderActionSheet
import com.fishhawk.driftinglibraryandroid.util.setNext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SearchFragment : Fragment() {
    private lateinit var binding: ProviderSearchFragmentBinding
    private val viewModel: SearchViewModel by viewModels {
        MainViewModelFactory(this)
    }

    private val actionAdapter = object : ProviderActionSheet.Listener {
        override fun onReadClick(outline: MangaOutline, provider: String) {
            navToReaderActivity(outline.id, viewModel.provider.id, 0, 0, 0)
        }

        override fun onLibraryAddClick(outline: MangaOutline, provider: String) {
            viewModel.addToLibrary(outline.id, outline.title)
        }
    }

    private val mangaAdapter = MangaListAdapter(object : MangaListAdapter.Listener {
        override fun onCardClick(outline: MangaOutline) {
            findNavController().navigate(
                R.id.action_to_gallery_detail,
                bundleOf(
                    "outline" to outline,
                    "provider" to viewModel.provider.id
                )
            )
        }

        override fun onCardLongClick(outline: MangaOutline) {
            ProviderActionSheet(
                requireContext(),
                outline,
                viewModel.provider.id,
                actionAdapter
            ).show()
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProviderSearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMenu(binding.toolbar.menu)
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.recyclerView.adapter = mangaAdapter

        GlobalPreference.displayMode.asFlow()
            .onEach { binding.recyclerView.changeMangaListDisplayMode(mangaAdapter) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.outlines.data.observe(viewLifecycleOwner) {
            mangaAdapter.setList(it)
        }
        viewModel.outlines.state.observe(viewLifecycleOwner) {
            binding.multiStateView.viewState = it
        }
        bindToRemotePagingList(binding.refreshLayout, viewModel.outlines)
    }

    private fun setupMenu(menu: Menu) {
        with(menu.findItem(R.id.action_search).actionView as SearchView) {
            queryHint = getString(R.string.menu_search_hint)
            maxWidth = Int.MAX_VALUE
            setQuery(viewModel.keywords.value, false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.keywords.value = query
                    closeInputMethod()
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean = true
            })
        }
        with(menu.findItem(R.id.action_display_mode)) {
            setIcon(getDisplayModeIcon())
        }
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
