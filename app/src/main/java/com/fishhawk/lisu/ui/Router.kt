package com.fishhawk.lisu.ui

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.setBlocking
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.ProviderDto

fun NavHostController.navToProvider(provider: ProviderDto) {
    currentBackStackEntry?.arguments =
        bundleOf("provider" to provider)
    navigate("provider/${provider.id}")
    PR.lastUsedProvider.setBlocking(provider.id)
}

fun NavHostController.navToSearch(providerId: String, keywords: String? = null) {
    currentBackStackEntry?.arguments =
        bundleOf("keywords" to keywords)
    navigate("search/${providerId}")
}

fun NavHostController.navToGlobalSearch(keywords: String? = null) {
    currentBackStackEntry?.arguments =
        bundleOf("keywords" to keywords)
    navigate("global-search")
}

fun NavHostController.navToGallery(manga: MangaDto) {
    currentBackStackEntry?.arguments =
        bundleOf("manga" to manga)
    navigate("gallery/${manga.id}")
}

fun NavHostController.navToGalleryEdit() = navigate("edit")

fun NavHostController.navToSettingGeneral() = navigate("setting-general")
fun NavHostController.navToSettingReader() = navigate("setting-reader")
fun NavHostController.navToSettingAdvanced() = navigate("setting-advanced")
fun NavHostController.navToAbout() = navigate("about")
fun NavHostController.navToOpenSourceLicense() = navigate("open-source-license")

fun Context.navToReader(
    mangaId: String,
    providerId: String,
    collectionId: String,
    chapterId: String,
    page: Int = 0
) {
    val bundle = bundleOf(
        "mangaId" to mangaId,
        "providerId" to providerId,
        "collectionId" to collectionId,
        "chapterId" to chapterId,
        "page" to page
    )
    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun Context.navToReader(
    detail: MangaDetailDto,
    collectionId: String,
    chapterId: String,
    page: Int
) {
    val bundle = bundleOf(
        "detail" to detail,
        "collectionId" to collectionId,
        "chapterId" to chapterId,
        "page" to page
    )
    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}
