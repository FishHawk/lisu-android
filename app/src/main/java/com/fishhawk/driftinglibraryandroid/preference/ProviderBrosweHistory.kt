package com.fishhawk.driftinglibraryandroid.preference

import android.content.Context

class ProviderBrowseHistory(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        "${context.packageName}.provider_browse_history",
        Context.MODE_PRIVATE
    )

    private fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    private fun getInt(key: String, defValue: Int): Int {
        return sharedPreferences.getInt(key, defValue)
    }

    private fun buildPageHistoryKey(id: String) = "$id:page"

    fun setPageHistory(id: String, page: Int) =
        putInt(buildPageHistoryKey(id), page)

    fun getPageHistory(id: String): Int =
        getInt(buildPageHistoryKey(id), 0)

    private fun buildOptionHistoryKey(id: String, page: Int, optionName: String) =
        "$id:$page:$optionName"

    fun setOptionHistory(id: String, page: Int, optionName: String, optionIndex: Int) =
        putInt(buildOptionHistoryKey(id, page, optionName), optionIndex)

    fun getOptionHistory(id: String, page: Int, optionName: String): Int =
        getInt(buildOptionHistoryKey(id, page, optionName), 0)
}