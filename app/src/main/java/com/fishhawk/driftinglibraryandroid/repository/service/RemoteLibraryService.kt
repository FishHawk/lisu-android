package com.fishhawk.driftinglibraryandroid.repository.service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import retrofit2.http.Query

interface RemoteLibraryService {
    @GET("library")
    fun getMangaList(
        @Query("last_id") lastId: String,
        @Query("filter") filter: String
    ): Call<List<MangaSummary>>

    @GET("manga/{id}")
    fun getMangaDetail(@Path("id") id: String): Call<MangaDetail>

    @GET("chapter/{id}")
    fun getChapterContent(
        @Path("id") id: String,
        @Query("collection") collection: String,
        @Query("chapter") chapter: String
    ): Call<List<String>>


//    /**
//     * Companion object to create the GithubApiService
//     */
//    companion object Factory {
//        fun create(): GithubApiService {
//            val retrofit = Retrofit.Builder()
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .baseUrl("https://api.github.com/")
//                .build()
//
//            return retrofit.create(GithubApiService::class.java);
//        }
//    }
}
