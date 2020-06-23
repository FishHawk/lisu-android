package com.fishhawk.driftinglibraryandroid.repository.remote

import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.data.Order
import com.fishhawk.driftinglibraryandroid.repository.data.Source
import okhttp3.Response
import retrofit2.http.*


interface RemoteLibraryService {
    @GET("library")
    suspend fun getMangaList(
        @Query("last_id") lastId: String,
        @Query("filter") filter: String
    ): List<MangaOutline>

    @GET("manga/{id}")
    suspend fun getMangaDetail(@Path("id") id: String): MangaDetail

    @GET("chapter/{id}")
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
    * Api: order
    */

    @GET("/orders")
    suspend fun getOrders(): List<Order>

    @FormUrlEncoded
    @POST("/order")
    suspend fun postUser(
        @Field("source") source: String,
        @Field("sourceMangaId") sourceMangaId: String,
        @Field("targetMangaId") targetMangaId: String
    ): Order

    @DELETE("/order/{id}")
    suspend fun deleteOrder(@Path("id") id: Int): Order
}
