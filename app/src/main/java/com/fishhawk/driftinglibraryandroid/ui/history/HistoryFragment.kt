package com.fishhawk.driftinglibraryandroid.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.HistoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity

class HistoryFragment : Fragment() {
    private lateinit var binding: HistoryFragmentBinding
    val viewModel: HistoryViewModel by viewModels {
        MainViewModelFactory(this)
    }

    val adapter = HistoryListAdapter(object : HistoryListAdapter.Listener {
        override fun onCoverClicked(history: ReadingHistory) {
            with(history) {
                findNavController().navigate(
                    R.id.action_to_gallery_detail,
                    bundleOf(
                        "outline" to MangaOutline(
                            mangaId,
                            cover,
                            null,
                            null,
                            MetadataOutline(title, null, null),
                            null
                        ),
                        "provider" to providerId?.let { ProviderInfo(it, it, "", "") }
                    )
                )
            }
        }

        override fun onCardClicked(history: ReadingHistory) {
            with(history) {
                navToReaderActivity(mangaId, providerId, collectionIndex, chapterIndex, pageIndex)
            }
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
    ): View {
        binding = HistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.recyclerView.adapter = adapter
        viewModel.viewState.observe(viewLifecycleOwner) { binding.multiStateView.viewState = it }
        viewModel.filteredHistoryList.observe(viewLifecycleOwner) { adapter.setList(it) }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> createHistoryFilterSwitchDialog()
            R.id.action_clear_history -> createClearHistoryDialog()
            else -> return false
        }
        return true
    }
}
