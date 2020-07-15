package com.fishhawk.driftinglibraryandroid.ui.history

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.HistoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.extension.navToReaderActivity

class HistoryFragment : Fragment() {
    private val viewModel: HistoryViewModel by viewModels {
        val readingHistoryRepository =
            (requireContext().applicationContext as MainApplication).readingHistoryRepository
        HistoryViewModelFactory(readingHistoryRepository)
    }
    private lateinit var binding: HistoryFragmentBinding

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
        super.onViewCreated(view, savedInstanceState)

        val adapter = HistoryListAdapter(requireActivity())
        adapter.onThumbClicked = {
            (requireActivity() as AppCompatActivity).navToGalleryActivity(
                it.id, it.title, it.thumb, it.source
            )
        }
        adapter.onCardClicked = {
            (requireActivity() as AppCompatActivity).navToReaderActivity(
                it.id, it.source, it.collectionIndex, it.chapterIndex, it.pageIndex
            )
        }
        binding.list.adapter = adapter

        viewModel.filteredReadingHistoryList.observe(viewLifecycleOwner, Observer { data ->
            (binding.list.adapter as HistoryListAdapter).changeList(data.toMutableList())
            if (data.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val checkedItem = when (SettingsHelper.historyFilter.getValueDirectly()) {
                    SettingsHelper.HISTORY_FILTER_ALL -> 0
                    SettingsHelper.HISTORY_FILTER_FROM_LIBRARY -> 1
                    SettingsHelper.HISTORY_FILTER_FROM_SOURCES -> 2
                    else -> -1
                }
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.dialog_filter_history)
                    .setSingleChoiceItems(
                        R.array.settings_history_filter_entries,
                        checkedItem
                    ) { _, which ->
                        when (which) {
                            0 -> SettingsHelper.historyFilter.setValue(SettingsHelper.HISTORY_FILTER_ALL)
                            1 -> SettingsHelper.historyFilter.setValue(SettingsHelper.HISTORY_FILTER_FROM_LIBRARY)
                            2 -> SettingsHelper.historyFilter.setValue(SettingsHelper.HISTORY_FILTER_FROM_SOURCES)
                        }
                    }
                    .show()
            }
            R.id.action_clear_history -> {
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.dialog_clear_history)
                    .setPositiveButton(R.string.dialog_clear_history_positive) { _, _ -> viewModel.clearReadingHistory() }
                    .show()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
