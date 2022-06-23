package com.fishhawk.lisu.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fishhawk.lisu.data.network.model.BoardModel
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class BoardFilter(
    val name: String,
    val options: List<String>,
    val selected: Int
)

class ProviderBrowseHistoryRepository(context: Context) {
    private val Context.store by preferencesDataStore(name = "provider_history")
    private val store = context.store

    suspend fun setFilter(providerId: String, boardId: String, name: String, selected: Int) =
        store.get("$providerId:$boardId:$name", 0).value.set(selected)

    fun getFilters(providerId: String, boardId: String, model: BoardModel) = store.data.map {
        model.map { (name, options) ->
            val key = intPreferencesKey("$providerId:$boardId:$name")
            BoardFilter(name, options, it[key] ?: 0)
        }
    }.distinctUntilChanged().conflate()

    fun getBoardHistory(providerId: String) =
        store.get("$providerId:board", "").value
}