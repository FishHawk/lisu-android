package com.fishhawk.lisu.data.remote.service

import com.fishhawk.lisu.data.remote.model.GitHubReleaseDto
import okhttp3.ResponseBody
import retrofit2.http.*

interface GitHubService {
    @Headers("Accept: application/vnd.github.v3.full+json")
    @GET("/repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubReleaseDto

    @Streaming
    @GET
    suspend fun downloadReleaseFile(
        @Url url: String
    ): ResponseBody
}