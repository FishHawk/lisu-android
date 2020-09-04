package com.fishhawk.driftinglibraryandroid.repository.remote.service

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline

interface RemoteLibraryService {
    @GET("library/search")
    suspend fun search(
        @Query("lastTime") lastTime: Long?,
        @Query("keywords") keywords: String
    ): List<MangaOutline>

    @GET("library/manga/{mangaId}")
    suspend fun getManga(@Path("mangaId") mangaId: String): MangaDetail

    @DELETE("library/manga/{mangaId}")
    suspend fun deleteManga(@Path("mangaId") mangaId: String): String

    @GET("library/chapter/{mangaId}")
    suspend fun getChapterContent(
        @Path("mangaId") mangaId: String,
        @Query("collection") collection: String,
        @Query("chapter") chapter: String
    ): List<String>
}