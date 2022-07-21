package com.fishhawk.lisu.data.network

import com.fishhawk.lisu.data.network.model.GitHubReleaseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.content.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.io.InputStream

class GitHubRepository(private val client: HttpClient) {
    suspend fun getLatestRelease(owner: String, repo: String): Result<GitHubReleaseDto> =
        runCatching {
            client.get("https://api.github.com/repos/${owner.path}/${repo.path}/releases/latest") {
                headers.append("Accept", "application/vnd.github.v3.full+json")
            }.body()
        }

    suspend fun downloadReleaseFile(url: String, listener: ProgressListener?): Result<InputStream> =
        runCatching {
            client.get(url) {
                onDownload(listener)
            }.body()
        }

    private val String.path
        get() = encodeURLPath()
}