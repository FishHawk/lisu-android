package com.fishhawk.lisu.data.remote.model

import android.os.Build
import com.google.gson.annotations.SerializedName

data class GitHubReleaseDto(
    @SerializedName("tag_name") val version: String,
    @SerializedName("body") val info: String,
    @SerializedName("html_url") val releaseLink: String,
    @SerializedName("assets") private val assets: List<Assets>
) {
    fun getDownloadLink(): String {
        val apkVariant = when (Build.SUPPORTED_ABIS[0]) {
            "arm64-v8a" -> "-arm64-v8a"
            "armeabi-v7a" -> "-armeabi-v7a"
            "x86", "x86_64" -> "-x86"
            else -> ""
        }
        val asset = assets.find {
            it.downloadLink.contains("lisu$apkVariant-")
        } ?: assets.first()
        return asset.downloadLink
    }

    data class Assets(@SerializedName("browser_download_url") val downloadLink: String)
}
