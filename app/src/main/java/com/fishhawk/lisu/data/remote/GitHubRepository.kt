package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.GitHubReleaseDto
import com.fishhawk.lisu.data.remote.service.GitHubService
import com.fishhawk.lisu.util.interceptor.ProgressInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

class GitHubRepository {
    private val retrofit = Retrofit.Builder()
        .client(
            OkHttpClient()
                .newBuilder()
                .addInterceptor(ProgressInterceptor())
                .build()
        )
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GitHubService::class.java)

    suspend fun getLatestRelease(owner: String, repo: String): Result<GitHubReleaseDto> =
        runCatching { service.getLatestRelease(owner, repo) }

    suspend fun downloadReleaseFile(url: String): Result<InputStream> =
        runCatching { service.downloadReleaseFile(url).byteStream() }
}