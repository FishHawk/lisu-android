package com.fishhawk.lisu.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.fishhawk.lisu.data.network.model.BoardId
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.ui.reader.ReaderActivity
import kotlinx.serialization.json.Json

object MangaNavType : NavType<MangaDto>(isNullableAllowed = true) {
    override fun get(bundle: Bundle, key: String): MangaDto? {
        return bundle.getString(key)?.let { parseValue(it) }
    }

    override fun parseValue(value: String): MangaDto {
        return Json.decodeFromString(MangaDto.serializer(), value)
    }

    override fun put(bundle: Bundle, key: String, value: MangaDto) {
        bundle.putString(key, Json.encodeToString(MangaDto.serializer(), value))
    }
}

fun NavHostController.navToLoginWebsite(providerId: String) {
    navigate("provider/${providerId}/login-website")
}

fun NavHostController.navToLoginCookies(providerId: String) {
    navigate("provider/${providerId}/login-cookies")
}

fun NavHostController.navToLoginPassword(providerId: String) {
    navigate("provider/${providerId}/login-password")
}

fun NavHostController.navToProvider(
    providerId: String,
    boardId: BoardId,
    keywords: String? = null,
) {
    val query = keywords?.let { "?keywords=${Uri.encode(keywords)}" } ?: ""
    navigate("provider/${providerId}/board/${boardId.name}$query")
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

fun NavHostController.navToDownload() = navigate("download")
fun NavHostController.navToSettingGeneral() = navigate("setting-general")
fun NavHostController.navToSettingReader() = navigate("setting-reader")
fun NavHostController.navToSettingAdvanced() = navigate("setting-advanced")
fun NavHostController.navToAbout() = navigate("about")
fun NavHostController.navToOpenSourceLicense() = navigate("open-source-license")

fun Context.navToReader(
    providerId: String,
    mangaId: String,
    collectionId: String,
    chapterId: String,
    page: Int = 0,
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
    page: Int,
) {
    val bundle = bundleOf(
        "detail" to Json.encodeToString(MangaDetailDto.serializer(), detail),
        "collectionId" to collectionId,
        "chapterId" to chapterId,
        "page" to page
    )
    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}
