package com.fishhawk.driftinglibraryandroid.repository.service

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary

interface RemoteLibraryService {
    @GET("library")
    suspend fun getMangaList(
        @Query("last_id") lastId: String,
        @Query("filter") filter: String
    ): List<MangaSummary>

    @GET("manga/{id}")
    suspend fun getMangaDetail(@Path("id") id: String): MangaDetail


    @GET("chapter/{id}")
    suspend fun getChapterContent(
        @Path("id") id: String,
        @Query("collection") collection: String,
        @Query("chapter") chapter: String
    ): List<String>
}
