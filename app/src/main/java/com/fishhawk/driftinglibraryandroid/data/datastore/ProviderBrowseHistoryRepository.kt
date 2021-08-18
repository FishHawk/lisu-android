package com.fishhawk.driftinglibraryandroid.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fishhawk.driftinglibraryandroid.data.remote.model.OptionModel
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class OptionGroup(
    val name: String,
    val options: List<String>,
    val selected: Int
)

class ProviderBrowseHistoryRepository(context: Context) {
    private val Context.store by preferencesDataStore(name = "provider_history")
    private val store = context.store

    suspend fun setOption(id: String, page: Int, name: String, selected: Int) =
        store.get("$id:$page:$name", 0).value.set(selected)

    fun getOption(id: String, page: Int, model: OptionModel) = store.data.map {
        model.map { (name, options) ->
            val key = intPreferencesKey("$id:$page:$name")
            OptionGroup(name, options, it[key] ?: 0)
        }
    }.distinctUntilChanged().conflate()

    fun getPageHistory(id: String) =
        store.get("$id:page", 0).value
}