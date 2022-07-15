package com.fishhawk.lisu.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.ProviderDto
import com.fishhawk.lisu.ui.reader.ReaderActivity
import kotlinx.serialization.json.Json

object MangaNavType : NavType<MangaDto>(isNullableAllowed = true) {
    override fun get(bundle: Bundle, key: String): MangaDto? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): MangaDto {
        return Json.decodeFromString(MangaDto.serializer(), value)
    }

    override fun put(bundle: Bundle, key: String, value: MangaDto) {
        bundle.putParcelable(key, value)
    }
}

object ProviderNavType : NavType<ProviderDto>(isNullableAllowed = true) {
    override fun get(bundle: Bundle, key: String): ProviderDto? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): ProviderDto {
        return Json.decodeFromString(ProviderDto.serializer(), value)
    }

    override fun put(bundle: Bundle, key: String, value: ProviderDto) {
        bundle.putParcelable(key, value)
    }
}

fun NavHostController.navToProvider(providerId: String, boardId: String) {
    navigate("provider/${providerId}/board/${boardId}")
}

fun NavHostController.navToProviderLogin(provider: ProviderDto) {
    val json = Uri.encode(Json.encodeToString(ProviderDto.serializer(), provider))
    navigate("provider/${provider.id}/login?provider=${json}")
}

fun NavHostController.navToProviderSearch(providerId: String, keywords: String? = null) {
    val query = keywords?.let { "?keywords=${Uri.encode(keywords)}" } ?: ""
    navigate("provider/${providerId}/search$query")
}

fun NavHostController.navToGlobalSearch(keywords: String? = null) {
    val query = keywords?.let { "?keywords=${Uri.encode(keywords)}" } ?: ""
    navigate("global-search$query")
}

fun NavHostController.navToGallery(manga: MangaDto) {
    val json = Uri.encode(Json.encodeToString(MangaDto.serializer(), manga))
    navigate("gallery/${manga.id}/detail?manga=${json}")
}

fun NavHostController.navToGalleryEdit() = navigate("edit")

fun NavHostController.navToGalleryComment() = navigate("comment")

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
