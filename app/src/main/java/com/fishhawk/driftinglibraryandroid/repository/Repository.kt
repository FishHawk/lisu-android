package com.fishhawk.driftinglibraryandroid.repository

import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.repository.service.RemoteLibraryService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Repository(private val url: String) {
    private val remoteLibraryService: RemoteLibraryService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        remoteLibraryService = retrofit.create(RemoteLibraryService::class.java)
    }

    fun getMangaList(
        callback: (data: Result<List<MangaSummary>>) -> Unit,
        lastId: String,
        filter: String
    ) {
        remoteLibraryService.getMangaList(lastId, filter)
            .enqueue(object : Callback<List<MangaSummary>> {
                override fun onResponse(
                    call: Call<List<MangaSummary>>,
                    response: Response<List<MangaSummary>>
                ) {
                    response.body()?.let {
                        for (s in it) {
                            s.thumb = "${url}image/${s.id}/${s.thumb}"
                        }
                        callback(Result.Success(it))
                    } ?: callback(Result.Error(UnknownError()))
                }

                override fun onFailure(call: Call<List<MangaSummary>>, t: Throwable) {
                    callback(Result.Error(t))
                }
            })
    }

    fun getMangaDetail(
        callback: (data: Result<MangaDetail>) -> Unit,
        id: String
    ) {
        remoteLibraryService.getMangaDetail(id)
            .enqueue(object : Callback<MangaDetail?> {
                override fun onResponse(
                    call: Call<MangaDetail?>,
                    response: Response<MangaDetail?>
                ) {
                    response.body()?.let {
                        it.thumb = "${url}image/${it.id}/${it.thumb}"
                        callback(Result.Success(it))
                    } ?: callback(Result.Error(UnknownError()))
                }

                override fun onFailure(call: Call<MangaDetail?>, t: Throwable) {
                    callback(Result.Error(t))
                }
            })
    }

    fun getChapterContent(
        callback: (data: Result<List<String>>) -> Unit,
        id: String,
        collection: String,
        chapter: String
    ) {
        remoteLibraryService.getChapterContent(id, collection, chapter)
            .enqueue(object : Callback<List<String>> {
                override fun onResponse(
                    call: Call<List<String>>,
                    response: Response<List<String>>
                ) {
                    response.body()?.let {
                        val content = it.map { "${url}image/$id/$collection/$chapter/$it" }
                        callback(Result.Success(content))
                    } ?: callback(Result.Error(UnknownError()))
                }

                override fun onFailure(call: Call<List<String>>, t: Throwable) {
                    callback(Result.Error(t))
                }
            })
    }
}
