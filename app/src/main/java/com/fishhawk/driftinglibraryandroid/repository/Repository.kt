package com.fishhawk.driftinglibraryandroid.repository

import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.repository.service.RemoteLibraryService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Repository {
    private var url: String
    private lateinit var remoteLibraryService: RemoteLibraryService

    fun setRemoteLibraryService(url: String) {
        this.url = url
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        remoteLibraryService = retrofit.create(RemoteLibraryService::class.java)
    }

    fun getUrl(): String {
        return url
    }

    init {
        url = "http://192.168.0.103:8080/api/"
        setRemoteLibraryService(url)
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
                    val list = response.body()!!
                    for (s in list) {
                        s.thumb = "$url/image/" + s.id + "/" + s.thumb
                    }
                    callback(Result.Success(list))
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
                    val detail: MangaDetail = response.body()!!
                    detail.thumb = url + "/image/" + detail.id + "/" + detail.thumb
                    callback(Result.Success(detail))
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
                    val content =
                        response.body()!!.map { "$url/image/$id/$collection/$chapter/$it" }
                    callback(Result.Success(content))
                }

                override fun onFailure(call: Call<List<String>>, t: Throwable) {
                    callback(Result.Error(t))
                }
            })
    }
}
