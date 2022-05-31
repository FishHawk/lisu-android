package com.fishhawk.lisu.ui.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.outlined.HideSource
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.widget.BottomSheet
import com.fishhawk.lisu.ui.widget.LocalBottomSheetHelper
import com.fishhawk.lisu.ui.widget.SheetListItem
import kotlinx.coroutines.launch

internal class ExploreSheet(
    private val provider: ProviderDto,
    private val onAction: ExploreActionHandler,
) : BottomSheet() {
    @Composable
    override fun Content() {
        ExploreSheetContent(provider, onAction)
        BackHandler()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ExploreSheetContent(
    provider: ProviderDto,
    onAction: ExploreActionHandler,
) {
    val scope = rememberCoroutineScope()
    val bottomSheetHelper = LocalBottomSheetHelper.current

    Column(modifier = Modifier.fillMaxWidth()) {
        if (provider.isLogged == true) {
            SheetListItem(
                icon = LisuIcons.Login,
                title = "Re-login",
            ) {
                onAction(ExploreAction.NavToProviderLogin(provider))
                scope.launch { bottomSheetHelper.state.hide() }
            }
            SheetListItem(
                icon = LisuIcons.Logout,
                title = "Logout",
            ) {
                onAction(ExploreAction.Logout(provider))
                scope.launch { bottomSheetHelper.state.hide() }
            }
        } else if (provider.isLogged == false) {
            SheetListItem(
                icon = LisuIcons.Login,
                title = "Login",
            ) {
                onAction(ExploreAction.NavToProviderLogin(provider))
                scope.launch { bottomSheetHelper.state.hide() }
            }
        }
        SheetListItem(
            icon = LisuIcons.HideSource,
            title = "Disable",
        ) {
            onAction(ExploreAction.Disable(provider))
            scope.launch { bottomSheetHelper.state.hide() }
        }
    }
}
