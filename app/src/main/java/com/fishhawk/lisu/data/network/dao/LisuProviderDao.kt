package com.fishhawk.lisu.data.network.dao

import com.fishhawk.lisu.data.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/provider")
private class Provider {
    @Serializable
    @Resource("/{providerId}/icon")
    data class Icon(
        val parent: Provider = Provider(),
        val providerId: String,
    )

    @Serializable
    @Resource("/{providerId}/login-cookies")
    data class LoginByCookies(
        val parent: Provider = Provider(),
        val providerId: String,
    )

    @Serializable
    @Resource("/{providerId}/login-password")
    data class LoginByPassword(
        val parent: Provider = Provider(),
        val providerId: String,
        val username: String,
        val password: String,
    )

    @Serializable
    @Resource("/{providerId}/logout")
    data class Logout(
        val parent: Provider = Provider(),
        val providerId: String,
    )

    @Serializable
    @Resource("/{providerId}/board/{boardId}")
    data class Board(
        val parent: Provider = Provider(),
        val providerId: String,
        val boardId: BoardId,
        val page: Int,
        val keywords: String?,
    )

    @Serializable
    @Resource("/{providerId}/manga/{mangaId}")
    data class Manga(
        val parent: Provider = Provider(),
        val providerId: String,
        val mangaId: String,
    )

    @Serializable
    @Resource("/{providerId}/manga/{mangaId}/comment")
    data class Comment(
        val parent: Provider = Provider(),
        val providerId: String,
        val mangaId: String,
        val page: Int,
    )

    @Serializable
    @Resource("/{providerId}/manga/{mangaId}/cover")
    data class Cover(
        val parent: Provider = Provider(),
        val providerId: String,
        val mangaId: String,
        val imageId: String?,
    )

    @Serializable
    @Resource("/{providerId}/manga/{mangaId}/content")
    data class Content(
        val parent: Provider = Provider(),
        val providerId: String,
        val mangaId: String,
        val collectionId: String,
        val chapterId: String,
    )

    @Serializable
    @Resource("/{providerId}/manga/{mangaId}/image")
    data class Image(
        val parent: Provider = Provider(),
        val providerId: String,
        val mangaId: String,
        val collectionId: String,
        val chapterId: String,
        val imageId: String,
    )
}

class LisuProviderDao(private val client: HttpClient) {

    suspend fun listProvider(
    ): List<ProviderDto> = client.get(
        Provider()
    ).run {
        body<List<ProviderDto>>().map {
            it.copy(icon = client.generateProviderIcon(request.url, it.id))
        }
    }

    suspend fun loginByCookies(
        providerId: String,
        cookies: Map<String, String>,
    ): String = client.post(
        Provider.LoginByCookies(providerId = providerId)
    ) {
        contentType(ContentType.Application.Json)
        setBody(cookies)
    }.body()

    suspend fun loginByPassword(
        providerId: String,
        username: String,
        password: String,
    ): String = client.post(
        Provider.LoginByPassword(
            providerId = providerId,
            username = username,
            password = password,
        )
    ).body()

    suspend fun logout(
        providerId: String,
    ): String = client.post(
        Provider.Logout(
            providerId = providerId,
        )
    ).body()

    suspend fun getBoard(
        providerId: String,
        boardId: BoardId,
        page: Int,
        keywords: String?,
        filterValues: BoardFilterValue,
    ): List<MangaDto> = client.get(
        Provider.Board(
            providerId = providerId,
            boardId = boardId,
            page = page,
            keywords = keywords,
        )
    ) {
        (filterValues.base + filterValues.advance).forEach {
            val value = it.value.value
            val str = if (value is Set<*>) value.joinToString(",") else value.toString()
            parameter(it.key, str)
        }
    }.run {
        body<List<MangaDto>>()
            .map {
                it.copy(
                    cover = client.processCover(
                        url = request.url,
                        providerId = it.providerId,
                        mangaId = it.id,
                        imageId = it.cover,
                    )
                )
            }
    }

    suspend fun getManga(
        providerId: String,
        mangaId: String,
    ): MangaDetailDto = client.get(
        Provider.Manga(
            providerId = providerId,
            mangaId = mangaId,
        )
    ).run {
        body<MangaDetailDto>().let { detail ->
            detail.copy(
                cover = client.processCover(
                    url = request.url,
                    providerId = detail.providerId,
                    mangaId = detail.id,
                    imageId = detail.cover,
                ),
                chapterPreviews = detail.chapterPreviews.map {
                    client.processImage(
                        url = request.url,
                        providerId = providerId,
                        mangaId = mangaId,
                        collectionId = "",
                        chapterId = "",
                        imageId = it,
                    )
                },
            )
        }
    }

    suspend fun getComment(
        providerId: String,
        mangaId: String,
        page: Int,
    ): List<CommentDto> = client.get(
        Provider.Comment(
            providerId = providerId,
            mangaId = mangaId,
            page = page,
        )
    ).body()

    suspend fun getContent(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): List<String> = client.get(
        Provider.Content(
            providerId = providerId,
            mangaId = mangaId,
            collectionId = collectionId,
            chapterId = chapterId,

            )
    ).run {
        body<List<String>>().map {
            client.processImage(
                url = request.url,
                providerId = providerId,
                mangaId = mangaId,
                collectionId = collectionId,
                chapterId = chapterId,
                imageId = it,
            )
        }
    }
}

private fun HttpClient.generateProviderIcon(
    url: Url,
    providerId: String,
): String {
    val builder = URLBuilder(url)
    builder.pathSegments = emptyList()
    href(
        Provider.Icon(
            providerId = providerId,
        ),
        builder,
    )
    return builder.build().toString()
}

internal fun HttpClient.processCover(
    url: Url,
    providerId: String,
    mangaId: String,
    imageId: String?,
): String {
    val builder = URLBuilder(url)
    // Hacky
    if (builder.protocol == URLProtocol.WS) {
        builder.protocol = URLProtocol.HTTP
    }
    builder.parameters.clear()
    href(
        Provider.Cover(
            providerId = providerId,
            mangaId = mangaId,
            imageId = imageId,
        ),
        builder,
    )
    return builder.build().toString()
}

private fun HttpClient.processImage(
    url: Url,
    providerId: String,
    mangaId: String,
    collectionId: String,
    chapterId: String,
    imageId: String,
): String {
    val builder = URLBuilder(url)
    builder.parameters.clear()
    href(
        Provider.Image(
            providerId = providerId,
            mangaId = mangaId,
            collectionId = collectionId,
            chapterId = chapterId,
            imageId = imageId,
        ),
        builder,
    )
    return builder.build().toString()
}