package com.fishhawk.lisu.data.network

import com.fishhawk.lisu.data.network.model.GitHubRelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.content.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.InputStream

@Serializable
@Resource("/repos/{owner}/{repo}/releases/latest")
private data class GitHubLatestRelease(
    val owner: String,
    val repo: String,
)

class GitHubRepository {
    private val client: HttpClient = HttpClient(OkHttp) {
        expectSuccess = true
        install(Resources)
        install(ContentNegotiation) { json(Json) }
    }

    suspend fun getLatestRelease(
        owner: String,
        repo: String,
    ): Result<GitHubRelease> = runCatching {
        val builder = URLBuilder("https://api.github.com/")
        client.href(GitHubLatestRelease(owner = owner, repo = repo), builder)
        val url = builder.build()
        client.get(url) {
            headers.append("Accept", "application/vnd.github.v3.full+json")
        }.body()
    }

    suspend fun downloadReleaseFile(url: String, listener: ProgressListener?): Result<InputStream> =
        runCatching {
            client.get(url) {
                onDownload(listener)
            }.body()
        }
}