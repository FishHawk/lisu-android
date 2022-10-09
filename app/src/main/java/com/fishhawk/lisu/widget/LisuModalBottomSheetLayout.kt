package com.fishhawk.lisu.widget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch

open class BottomSheet {
    open val scrimColor: Color
        @Composable
        get() = ModalBottomSheetDefaults.scrimColor

    @Composable
    open fun Content() {
        Text("No content")
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    protected fun BackHandler() {
        val helper = LocalBottomSheetHelper.current
        val scope = rememberCoroutineScope()
        BackHandler(helper.state.isVisible || helper.state.isAnimationRunning) {
            scope.launch { helper.hide() }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
class BottomSheetHelper(
    private val sheetState: MutableState<BottomSheet>,
    val state: ModalBottomSheetState
) {
    val sheet = sheetState.value

    suspend fun open(sheet: BottomSheet) {
        this.sheetState.value = sheet
        state.show()
    }

    suspend fun hide() {
        state.hide()
    }
}

val LocalBottomSheetHelper = compositionLocalOf<BottomSheetHelper> {
    error("CompositionLocal LocalBottomSheetHelper not present")
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LisuModalBottomSheetLayout(
    content: @Composable () -> Unit
) {
    val helper = BottomSheetHelper(
        remember { mutableStateOf(BottomSheet()) },
        rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    )
    CompositionLocalProvider(LocalBottomSheetHelper provides helper) {
        ModalBottomSheetLayout(
            sheetState = helper.state,
            sheetContent = { helper.sheet.Content() },
            scrimColor = helper.sheet.scrimColor,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetListItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { onClick() }),
        icon = { Icon(imageVector = icon, contentDescription = title) },
        text = { Text(text = title) }
    )
}
