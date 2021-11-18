package com.fishhawk.lisu.ui.widget

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> VerticalGrid(
    items: List<T>,
    nColumns: Int,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.(index: Int, item: T) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
    ) {
        items.chunked(nColumns).onEach { rowItems ->
            Row(horizontalArrangement = horizontalArrangement) {
                rowItems.onEachIndexed { index, it -> content(index, it) }
                repeat(nColumns - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}