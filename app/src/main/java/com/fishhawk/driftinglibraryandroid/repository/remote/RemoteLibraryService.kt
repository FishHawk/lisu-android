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
    * Api: subscribe
    */

    @GET("/subscriptions")
    suspend fun getSubscriptions(): List<Subscription>

    @FormUrlEncoded
    @POST("/subscription")
    suspend fun postSubscription(
        @Field("source") source: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): Subscription

    @DELETE("/subscription/{id}")
    suspend fun deleteSubscription(@Path("id") id: Int): Subscription
}
