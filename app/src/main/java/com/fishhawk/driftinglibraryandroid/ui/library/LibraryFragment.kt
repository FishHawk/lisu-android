package com.fishhawk.driftinglibraryandroid.ui.library

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.LibraryFragmentBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.util.setNext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LibraryFragment : Fragment() {
    private lateinit var binding: LibraryFragmentBinding
    private val viewModel: LibraryViewModel by viewModels {
        MainViewModelFactory(
            requireActivity().application as MainApplication,
            arguments ?: bundleOf()
        )
    }

    val adapter = MangaListAdapter(object : MangaListAdapter.Listener {
        override fun onCardClick(outline: MangaOutline) {
            findNavController().navigate(
                R.id.action_to_gallery,
                bundleOf(
                    "id" to outline.id,
                    "title" to outline.title,
                    "thumb" to outline.thumb
                )
            )
        }

        override fun onCardLongClick(outline: MangaOutline) {
            AlertDialog.Builder(requireActivity())
                .setTitle("Confirm to delete manga?")
                .setPositiveButton("OK") { _, _ -> viewModel.deleteManga(outline.id) }
                .setNegativeButton("cancel") { _, _ -> }
                .show()
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LibraryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMenu(binding.toolbar.menu)
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)

        binding.recyclerView.adapter = adapter

        GlobalPreference.displayMode.asFlow()
            .onEach { binding.recyclerView.changeMangaListDisplayMode(adapter) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.outlines.data.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        viewModel.outlines.state.observe(viewLifecycleOwner) {
            binding.multiStateView.viewState = it
        }
        bindToPagingList(binding.refreshLayout, viewModel.outlines)
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

                override fun onQueryTextChange(query: String?): Boolean = false
            })
            val closeButton: ImageView = findViewById(R.id.search_close_btn)
            closeButton.setOnClickListener {
                setQuery(null, false)
                isIconified = true
                viewModel.keywords.value = ""
            }
        }
        with(menu.findItem(R.id.action_display_mode)) {
            setIcon(getDisplayModeIcon())
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                GlobalPreference.run { displayMode.setNext() }
                item.setIcon(getDisplayModeIcon())
                true
            }
            else -> false
        }
    }
}