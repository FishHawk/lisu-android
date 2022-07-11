package com.fishhawk.lisu.ui.reader.viewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

fun nestedScrollConnection(
    requestMoveToPrevChapter: () -> Unit,
    requestMoveToNextChapter: () -> Unit,
    isPrepareToPrev: (offset: Offset) -> Boolean,
    isPrepareToNext: (offset: Offset) -> Boolean
) = object : NestedScrollConnection {
    var prepareToNext = false
    var prepareToPrev = false

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (source == NestedScrollSource.Drag) {
            prepareToPrev = isPrepareToPrev(available)
            prepareToNext = isPrepareToNext(available)
        }
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (prepareToNext) {
            prepareToNext = false
            requestMoveToNextChapter()
        } else if (prepareToPrev) {
            prepareToPrev = false
            requestMoveToPrevChapter()
        }
        return Velocity.Zero
    }
}