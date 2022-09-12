package com.fishhawk.lisu.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.fishhawk.lisu.data.network.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ProviderBrowseHistoryRepository(context: Context) {
    private val Context.store by preferencesDataStore(name = "provider_history")
    private val store = context.store

    suspend fun setFilterValue(
        providerId: String,
        boardId: BoardId,
        name: String,
        value: Any,
    ) {
        val fullName = "$providerId:${boardId.name}:$name"
        when (value) {
            is String -> store.get(fullName, value).value.set(value)
            is Boolean -> store.get(fullName, value).value.set(value)
            is Int -> store.get(fullName, value).value.set(value)
            else -> {
                val newValue = (value as Set<*>).map { it.toString() }.toSet()
                store.get(fullName, newValue).value.set(newValue)
            }
        }
    }

    fun getBoardFilterValue(
        providerId: String,
        boardId: BoardId,
        model: BoardModel,
    ): Flow<BoardFilterValue> {
        return store.data
            .map {
                BoardFilterValue(
                    base = getFilterValues(providerId, boardId, model.base, it),
                    advance = getFilterValues(providerId, boardId, model.advance, it),
                )
            }
            .distinctUntilChanged()
            .conflate()
    }

    private fun getFilterValues(
        providerId: String,
        boardId: BoardId,
        boardModel: Map<String, FilterModel>,
        preferences: Preferences,
    ): Map<String, FilterValue> {
        return boardModel.mapValues { (name, filterModel) ->
            val fullName = "$providerId:${boardId.name}:$name"
            val value: Any = when (filterModel) {
                is FilterModel.Text ->
                    preferences[stringPreferencesKey(fullName)]
                        ?: ""
                is FilterModel.Switch ->
                    preferences[booleanPreferencesKey(fullName)]
                        ?: filterModel.default
                is FilterModel.Select ->
                    preferences[intPreferencesKey(fullName)]
                        ?: 0
                is FilterModel.MultipleSelect ->
                    preferences[stringSetPreferencesKey(fullName)]
                        ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet<Int>()
            }
            FilterValue(filterModel, value)
        }
    }

}