package com.fishhawk.driftinglibraryandroid.ui.main.history

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.HistoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.ui.extension.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory

class HistoryFragment : Fragment() {
    private lateinit var binding: HistoryFragmentBinding
    val viewModel: HistoryViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    val adapter = HistoryListAdapter(object : HistoryListAdapter.Listener {
        override fun onThumbClicked(history: ReadingHistory) {
            with(history) {
                findNavController().navigate(
                    R.id.action_to_gallery,
                    bundleOf(
                        "id" to mangaId,
                        "title" to title,
                        "thumb" to thumb,
                        "providerId" to providerId
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
    ): View? {
        binding = HistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)

        binding.list.adapter = adapter

        viewModel.filteredReadingHistoryList.observe(viewLifecycleOwner, Observer {
            adapter.setList(it)
            if (it.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        })
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
