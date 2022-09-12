package com.fishhawk.lisu.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


enum class BoardId { Main, Rank, Search }

@Serializable
sealed interface FilterModel {
    @Serializable
    @SerialName("Text")
    data class Text(val showSearchBar: Boolean = false) : FilterModel

    @Serializable
    @SerialName("Switch")
    data class Switch(val default: Boolean = false) : FilterModel

    @Serializable
    @SerialName("Select")
    data class Select(val options: List<String>) : FilterModel

    @Serializable
    @SerialName("MultipleSelect")
    data class MultipleSelect(val options: List<String>) : FilterModel
}

@Serializable
data class BoardModel(
    val hasSearchBar: Boolean = false,
    val base: Map<String, FilterModel> = emptyMap(),
    val advance: Map<String, FilterModel> = emptyMap(),
)

data class FilterValue(
    val model: FilterModel,
    val value: Any,
)

data class BoardFilterValue(
    val base: Map<String, FilterValue>,
    val advance: Map<String, FilterValue>,
) {
    companion object {
        val Empty = BoardFilterValue(emptyMap(), emptyMap())
    }
}

@Serializable
data class ProviderDto(
    val id: String,
    val lang: String,
    var icon: String? = null,
    val boardModels: Map<BoardId, BoardModel>,
    val isLogged: Boolean? = null,
    val loginSite: String? = null,
) {
    val searchBoardId: BoardId?
        get() =
            if (boardModels.containsKey(BoardId.Search)) BoardId.Search
            else boardModels.entries.find { it.value.hasSearchBar }?.key
}