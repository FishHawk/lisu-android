package com.fishhawk.lisu.ui.provider

import android.webkit.CookieManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.ui.widget.OneLineText
import com.fishhawk.lisu.util.toast
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf
import java.net.URLDecoder

internal typealias ProviderLoginActionHandler = (ProviderLoginAction) -> Unit

internal sealed interface ProviderLoginAction {
    object NavUp : ProviderLoginAction
    object Login : ProviderLoginAction
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProviderLoginScreen(navController: NavHostController) {
    val viewModel by viewModel<ProviderLoginViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }

    val context = LocalContext.current
    val provider = viewModel.provider
    val url = provider.loginSite
    if (url.isNullOrBlank()) {
        context.toast("Missing login site.")
        navController.navigateUp()
        return
    }

    val onAction: ProviderLoginActionHandler = { action ->
        when (action) {
            ProviderLoginAction.NavUp -> navController.navigateUp()
            is ProviderLoginAction.Login -> {
                val cm = CookieManager.getInstance()
                cm.removeSessionCookies { }
                val cookies = cm.getCookie(url).split(";")
                    .associate {
                        val name = it.substringBefore('=').trim()
                        val value = it.substringAfter('=').trim()
                        name to URLDecoder.decode(value, "UTF-8")
                    }
                viewModel.login(cookies)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProviderLoginEffect.LoginFailure -> context.toast("Login fail.")
                ProviderLoginEffect.LoginSuccess -> {
                    context.toast("Login successful.")
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = {
                    ListItem(
                        text = { OneLineText(text = "Login - ${provider.id}") },
                        secondaryText = { OneLineText(text = url) },
                    )
                },
                onNavUp = { onAction(ProviderLoginAction.NavUp) },
            ) {
                IconButton(onClick = { onAction(ProviderLoginAction.Login) }) {
                    Icon(LisuIcons.Login, "test")
                }
            }
        },
        content = { paddingValues ->
            LisuTransition {
                val state = rememberWebViewState(url)
                WebView(
                    state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onCreated = {
                        it.settings.apply {
                            javaScriptEnabled = true
                            userAgentString =
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                                        "Chrome/65.0.3325.146 Safari/537.36"
                            builtInZoomControls = true
                            displayZoomControls = false

                            loadWithOverviewMode = true
                            useWideViewPort = true
                        }
                    },
                )
            }
        }
    )
}
