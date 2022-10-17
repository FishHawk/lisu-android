package com.fishhawk.lisu.widget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

fun <T> LazyListScope.itemsVerticalGrid(
    items: Iterable<T>,
    nColumns: Int,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.(item: T) -> Unit,
) {
    items(items.chunked(nColumns)) { rowItems ->
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
        ) {
            rowItems.forEach { item ->
                content(item)
            }
            repeat(nColumns - rowItems.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}