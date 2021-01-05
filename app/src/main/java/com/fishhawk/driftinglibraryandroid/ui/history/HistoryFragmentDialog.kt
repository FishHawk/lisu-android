package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.appcompat.app.AlertDialog
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference

fun HistoryFragment.createHistoryFilterSwitchDialog() {
    val checkedItem = when (GlobalPreference.historyFilter.get()) {
        GlobalPreference.HistoryFilter.ALL -> 0
        GlobalPreference.HistoryFilter.FROM_LIBRARY -> 1
        GlobalPreference.HistoryFilter.FROM_SOURCES -> 2
    }
    AlertDialog.Builder(requireContext())
        .setTitle(R.string.dialog_filter_history)
        .setSingleChoiceItems(
            R.array.settings_history_filter_entries,
            checkedItem
        ) { _, which ->
            GlobalPreference.historyFilter.set(
                when (which) {
                    0 -> GlobalPreference.HistoryFilter.ALL
                    1 -> GlobalPreference.HistoryFilter.FROM_LIBRARY
                    2 -> GlobalPreference.HistoryFilter.FROM_SOURCES
                    else -> throw Error()
                }
            )
        }
        .show()
}

fun HistoryFragment.createClearHistoryDialog() {
    AlertDialog.Builder(requireContext())
        .setTitle(R.string.dialog_clear_history)
        .setPositiveButton(R.string.dialog_clear_history_positive) { _, _ ->
            viewModel.clearReadingHistory()
        }
        .show()
}