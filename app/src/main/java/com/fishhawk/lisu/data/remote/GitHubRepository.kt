package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.GitHubReleaseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.InputStream

class GitHubRepository {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json) }
    }

    suspend fun getLatestRelease(owner: String, repo: String): Result<GitHubReleaseDto> =
        runCatching {
            client.get("https://api.github.com/repos/${owner.path}/${repo.path}/releases/latest") {
                headers.append("Accept", "application/vnd.github.v3.full+json")
            }.body()
        }

    suspend fun downloadReleaseFile(url: String): Result<InputStream> =
        runCatching { client.get(url).body() }

    private val String.path
        get() = encodeURLPath()
}