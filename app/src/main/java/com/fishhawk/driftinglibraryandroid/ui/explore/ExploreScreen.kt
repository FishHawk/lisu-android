package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.google.accompanist.coil.rememberCoilPainter

@Composable
fun ExploreScreen(navHostController: NavHostController) {
    Scaffold(
        topBar = { ToolBar() },
        content = { ApplicationTransition { Content(navHostController) } }
    )
}

@Composable
private fun ToolBar() {
    ApplicationToolBar(stringResource(R.string.label_explore)) {
        //  queryHint = getString(R.string.menu_search_global_hint)
        //  binding.root.findNavController().navigate(
        //      R.id.action_explore_to_global_search,
        //      bundleOf("keywords" to query)
        //  )
        IconButton(onClick = { }) {
            Icon(Icons.Filled.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun Content(navHostController: NavHostController) {
    val viewModel = hiltViewModel<ExploreViewModel>()
    val providers by viewModel.providerList.observeAsState(listOf())
    val lastUsedProvider by GlobalPreference.lastUsedProvider.asFlow().collectAsState(null)

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        providers.find { it.id == lastUsedProvider }?.let {
            Text(text = "Last used", style = MaterialTheme.typography.subtitle1)
            ProviderCard(navHostController, it)
        }
        val providerMap = providers.groupBy { it.lang }
        providerMap.map { (lang, list) ->
            Text(text = lang, style = MaterialTheme.typography.subtitle1)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                list.map { ProviderCard(navHostController, it) }
            }
        }
    }
}

@Composable
private fun ProviderCard(navController: NavHostController, provider: ProviderInfo) {
    Card(
        modifier = Modifier.clickable {
            navController.currentBackStackEntry?.arguments =
                bundleOf("provider" to provider)
            navController.navigate("provider/${provider.id}")
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val request = ImageRequest.Builder(LocalContext.current)
                .data(provider.icon)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            Image(
                modifier = Modifier
                    .size(48.dp, 48.dp)
                    .padding(8.dp),
                painter = rememberCoilPainter(request, fadeIn = true),
                contentDescription = "icon",
                contentScale = ContentScale.Crop
            )

            Text(
                modifier = Modifier.weight(1f, fill = true),
                text = provider.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun openProvider(provider: ProviderInfo) {
//    findNavController().navigate(
//        R.id.action_explore_to_provider_pager,
//        bundleOf("provider" to provider)
//    )
    GlobalPreference.lastUsedProvider.set(provider.id)
}
