package com.fishhawk.driftinglibraryandroid.repository.remote.service

import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MetadataDetail
import okhttp3.MultipartBody
import retrofit2.http.*

interface RemoteLibraryService {
    @GET("library/search")
    suspend fun search(
        @Query("lastTime") lastTime: Long,
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int
    ): List<MangaOutline>

    @GET("library/manga/{mangaId}")
    suspend fun getManga(@Path("mangaId") mangaId: String): MangaDetail

    @DELETE("library/manga/{mangaId}")
    suspend fun deleteManga(@Path("mangaId") mangaId: String): String

    @PATCH("library/manga/{mangaId}/metadata")
    suspend fun patchMangaMetadata(
        @Path("mangaId") mangaId: String,
        @Body metadata: MetadataDetail
    ): MangaDetail

    @Multipart
    @PATCH("library/manga/{mangaId}/thumb")
    suspend fun patchMangaThumb(
        @Path("mangaId") mangaId: String,
        @Part file: MultipartBody.Part
    ): MangaDetail

    @GET("library/chapter/{mangaId}")
    suspend fun getChapterContent(
        @Path("mangaId") mangaId: String,
        @Query("collection") collection: String,
        @Query("chapter") chapter: String
    ): List<String>
}