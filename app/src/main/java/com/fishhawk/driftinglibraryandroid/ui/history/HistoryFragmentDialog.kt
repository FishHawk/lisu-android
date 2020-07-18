package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.appcompat.app.AlertDialog
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper


fun HistoryFragment.createHistoryFilterSwitchDialog() {
    val checkedItem = when (SettingsHelper.historyFilter.getValueDirectly()) {
        SettingsHelper.HISTORY_FILTER_ALL -> 0
        SettingsHelper.HISTORY_FILTER_FROM_LIBRARY -> 1
        SettingsHelper.HISTORY_FILTER_FROM_SOURCES -> 2
        else -> -1
    }
    AlertDialog.Builder(requireContext())
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

fun HistoryFragment.createClearHistoryDialog() {
    AlertDialog.Builder(requireContext())
        .setTitle(R.string.dialog_clear_history)
        .setPositiveButton(R.string.dialog_clear_history_positive) { _, _ ->
            viewModel.clearReadingHistory()
        }
        .show()
}