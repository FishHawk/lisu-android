package com.fishhawk.driftinglibraryandroid.repository.remote

import com.fishhawk.driftinglibraryandroid.repository.data.*
import retrofit2.http.*


interface RemoteLibraryService {
    /*
    * Api: library
    */

    @GET("library/search")
    suspend fun getMangaList(
        @Query("last_id") lastId: String,
        @Query("filter") filter: String
    ): List<MangaOutline>

    @GET("library/manga/{id}")
    suspend fun getMangaDetail(@Path("id") id: String): MangaDetail

    @GET("library/chapter/{id}")
    suspend fun getChapterContent(
        @Path("id") id: String,
        @Query("collection") collection: String,
        @Query("chapter") chapter: String
    ): List<String>


    /*
    * Api: source
    */

    @GET("/sources")
    suspend fun getSources(): List<Source>

    @GET("/source/{source}/search")
    suspend fun search(
        @Path("source") source: String,
        @Query("keywords") keywords: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/source/{source}/popular")
    suspend fun getPopular(
        @Path("source") source: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/source/{source}/latest")
    suspend fun getLatest(
        @Path("source") source: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/source/{source}/manga/{id}")
    suspend fun getMangaDetail(
        @Path("source") source: String,
        @Path("id") id: String
    ): MangaDetail

    @GET("/source/{source}/chapter/{id}")
    suspend fun getChapterContent(
        @Path("source") source: String,
        @Path("id") id: String
    ): List<String>


    /*
    * Api: download
    */

    @GET("/downloads")
    suspend fun getAllDownloadTasks(): List<DownloadTask>

    @PATCH("/downloads/start")
    suspend fun startAllDownloadTasks(): List<DownloadTask>

    @PATCH("/downloads/pause")
    suspend fun pauseAllDownloadTasks(): List<DownloadTask>

    @FormUrlEncoded
    @POST("/download")
    suspend fun postDownloadTask(
        @Field("source") source: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): DownloadTask

    @DELETE("/download/{id}")
    suspend fun deleteDownloadTask(@Path("id") id: Int): DownloadTask

    @PATCH("/download/{id}/start")
    suspend fun startDownloadTask(@Path("id") id: Int): DownloadTask

    @PATCH("/download/{id}/pause")
    suspend fun pauseDownloadTask(@Path("id") id: Int): DownloadTask


    /*
    * Api: subscribe
    */

    @GET("/subscriptions")
    suspend fun getAllSubscriptions(): List<Subscription>

    @PATCH("/subscriptions/enable")
    suspend fun enableAllSubscriptions(): List<Subscription>

    @PATCH("/subscriptions/disable")
    suspend fun disableAllSubscriptions(): List<Subscription>

    @FormUrlEncoded
    @POST("/subscription")
    suspend fun postSubscription(
        @Field("source") source: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): Subscription

    @DELETE("/subscription/{id}")
    suspend fun deleteSubscription(@Path("id") id: Int): Subscription

    @PATCH("/subscription/{id}/enable")
    suspend fun enableSubscription(@Path("id") id: Int): Subscription

    @PATCH("/subscription/{id}/disable")
    suspend fun disableSubscription(@Path("id") id: Int): Subscription
}
