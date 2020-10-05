package com.fishhawk.driftinglibraryandroid.ui.main.history

import androidx.appcompat.app.AlertDialog
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

fun HistoryFragment.createHistoryFilterSwitchDialog() {
    val checkedItem = when (SettingsHelper.historyFilter.getValueDirectly()) {
        SettingsHelper.HistoryFilter.ALL -> 0
        SettingsHelper.HistoryFilter.FROM_LIBRARY -> 1
        SettingsHelper.HistoryFilter.FROM_SOURCES -> 2
    }
    AlertDialog.Builder(requireContext())
        .setTitle(R.string.dialog_filter_history)
        .setSingleChoiceItems(
            R.array.settings_history_filter_entries,
            checkedItem
        ) { _, which ->
            SettingsHelper.historyFilter.setValue(
                when (which) {
                    0 -> SettingsHelper.HistoryFilter.ALL
                    1 -> SettingsHelper.HistoryFilter.FROM_LIBRARY
                    2 -> SettingsHelper.HistoryFilter.FROM_SOURCES
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