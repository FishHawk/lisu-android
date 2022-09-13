package com.fishhawk.lisu.ui.explore

import android.webkit.CookieManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.data.network.model.CookiesLoginDto
import com.fishhawk.lisu.data.network.model.ProviderDto
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LisuToolBar
import com.fishhawk.lisu.widget.OneLineText
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import org.koin.androidx.compose.viewModel
import java.net.URLDecoder

internal typealias LoginActionHandler = (LoginAction) -> Unit

internal sealed interface LoginAction {
    object NavUp : LoginAction
    data class LoginByCookies(val cookies: Map<String, String>) : LoginAction
    data class LoginByPassword(val username: String, val password: String) : LoginAction
}

@Composable
private fun LoginScreen(
    navController: NavHostController,
    content: @Composable (ProviderDto, LoginActionHandler) -> Unit,
) {
    val viewModel by viewModel<ExploreViewModel>()

    val context = LocalContext.current
    val provider = remember {
        val providerId = navController.currentBackStackEntry!!.arguments!!.getString("providerId")!!
        val providersResult = viewModel.providersLoadState.value
        val provider = providersResult?.getOrNull()?.values?.flatten()
            ?.find { it.id == providerId }
        if (provider == null) {
            context.toast("Provider $providerId not found.")
            navController.navigateUp()
            return
        }
        provider
    }

    val onAction: LoginActionHandler = { action ->
        when (action) {
            LoginAction.NavUp ->
                navController.navigateUp()
            is LoginAction.LoginByCookies ->
                viewModel.loginByCookies(provider.id, action.cookies)
            is LoginAction.LoginByPassword ->
                viewModel.loginByPassword(provider.id, action.username, action.password)
        }
    }

    OnEvent(event = viewModel.event) {
        when (it) {
            is ExploreEvent.LoginFailure -> context.toast("Login fail.")
            ExploreEvent.LoginSuccess -> {
                context.toast("Login successful.")
                navController.navigateUp()
            }
        }
    }

    content(provider, onAction)
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun LoginWebsiteScreen(
    navController: NavHostController,
) = LoginScreen(navController = navController) { provider, onAction ->
    val context = LocalContext.current
    val cookiesLogin = remember {
        val cookiesLogin = provider.cookiesLogin
        if (cookiesLogin == null) {
            context.toast("Provider ${provider.id} not support website login.")
            onAction(LoginAction.NavUp)
            return@LoginScreen
        }
        cookiesLogin
    }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = {
                    ListItem(
                        text = { OneLineText(text = "${provider.id}-Login") },
                        secondaryText = { OneLineText(text = cookiesLogin.loginSite) },
                    )
                },
                onNavUp = { onAction(LoginAction.NavUp) },
            ) {
                IconButton(onClick = {
                    val cookies = CookieManager.getInstance().getCookie(cookiesLogin.loginSite)
                        .split(";")
                        .associate {
                            val name = it.substringBefore('=').trim()
                            val value = it.substringAfter('=').trim()
                            name to URLDecoder.decode(value, "UTF-8")
                        }
                        .filterKeys { it in cookiesLogin.cookieNames }
                    onAction(LoginAction.LoginByCookies(cookies))
                }) {
                    Icon(LisuIcons.Login, "test")
                }
            }
        },
        content = { paddingValues ->
            LisuTransition {
                val state = rememberWebViewState(cookiesLogin.loginSite)
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

@Composable
fun LoginCookiesScreen(
    navController: NavHostController,
) = LoginScreen(navController = navController) { provider, onAction ->
    val context = LocalContext.current
    val cookiesLogin = remember {
        val cookiesLogin = provider.cookiesLogin
        if (cookiesLogin == null) {
            context.toast("Provider ${provider.id} not support website login.")
            onAction(LoginAction.NavUp)
            return@LoginScreen
        }
        cookiesLogin
    }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = "${provider.id}-Login",
                onNavUp = { onAction(LoginAction.NavUp) },
            )
        },
        content = { paddingValues ->
            LisuTransition {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            elevation = 2.dp,
                        ) {
                            val painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(provider.icon)
                                    .size(Size.ORIGINAL)
                                    .crossfade(true)
                                    .build()
                            )
                            Image(
                                painter = painter,
                                contentDescription = provider.id,
                                modifier = Modifier.size(64.dp),
                            )
                        }

                        val cookies = remember { mutableStateMapOf<String, String>() }

                        val state = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(state),
                        ) {
                            cookiesLogin.cookieNames.forEach { name ->
                                OutlinedTextField(
                                    value = cookies[name] ?: "",
                                    onValueChange = { cookies[name] = it },
                                    modifier = Modifier.fillMaxSize(),
                                    singleLine = true,
                                    label = { Text(text = name) },
                                )
                            }
                        }
                        Button(
                            onClick = { onAction(LoginAction.LoginByCookies(cookies)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Login")
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun LoginPasswordScreen(
    navController: NavHostController,
) = LoginScreen(navController = navController) { provider, onAction ->
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!provider.passwordLogin) {
            context.toast("Provider ${provider.id} not support password login.")
            onAction(LoginAction.NavUp)
        }
    }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = "${provider.id}-Login",
                onNavUp = { onAction(LoginAction.NavUp) },
            )
        },
        content = { paddingValues ->
            LisuTransition {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            elevation = 2.dp,
                        ) {
                            val painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(provider.icon)
                                    .size(Size.ORIGINAL)
                                    .crossfade(true)
                                    .build()
                            )
                            Image(
                                painter = painter,
                                contentDescription = provider.id,
                                modifier = Modifier.size(64.dp),
                            )
                        }

                        var username by rememberSaveable { mutableStateOf("") }
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            modifier = Modifier.fillMaxSize(),
                            singleLine = true,
                            label = { Text(text = "username") },
                        )
                        var password by rememberSaveable { mutableStateOf("") }
                        var passwordVisible by rememberSaveable { mutableStateOf(false) }
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxSize(),
                            singleLine = true,
                            label = { Text(text = "password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image =
                                    if (passwordVisible) LisuIcons.Visibility
                                    else LisuIcons.VisibilityOff
                                val description =
                                    if (passwordVisible) "Hide password"
                                    else "Show password"
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, description)
                                }
                            }
                        )

                        Button(
                            onClick = { onAction(LoginAction.LoginByPassword(username, password)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Login")
                        }
                    }
                }
            }
        }
    )
}
