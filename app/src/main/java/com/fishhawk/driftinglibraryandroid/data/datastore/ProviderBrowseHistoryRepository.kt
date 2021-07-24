package com.fishhawk.driftinglibraryandroid.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

class ProviderBrowseHistoryRepository(context: Context) {
    private val Context.store by preferencesDataStore(name = "provider_history")
    private val store = context.store

    fun getPageHistory(id: String) =
        store.get("$id:page", 0).value

    fun getOptionHistory(id: String, page: Int, optionName: String) =
        store.get("$id:$page:$optionName", 0).value
}