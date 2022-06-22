package com.fishhawk.lisu.data.remote

import com.fishhawk.lisu.data.remote.model.*
import com.fishhawk.lisu.data.remote.util.*
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

class LisuRepository(
    urlFlow: Flow<String>,
    private val client: HttpClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val daoFlow = urlFlow
        .map {
            if (it.isBlank()) {
                Result.failure(Exception("sdaf"))
            } else {
                val url = if (
                    it.startsWith("https:", ignoreCase = true) ||
                    it.startsWith("http:", ignoreCase = true)
                ) it else "http://$it"
                runCatching { URLBuilder(url).buildString() }
            }
        }
        .distinctUntilChanged()
        .map {
            it.map { url -> LisuDao(client, url) }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)


    private suspend inline fun <T> oneshot(crossinline func: suspend (LisuDao) -> T): Result<T> {
        return daoFlow.filterNotNull().first().mapCatching { func(it) }
    }

    // Provider API
    private val mangaActionChannels = mutableListOf<RemoteDataActionChannel<MangaDetailDto>>()

    val providers =
        daoFlow.filterNotNull().flatMapLatest {
            remoteData(loader = { it.mapCatching { dao -> dao.listProvider() } })
        }.stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun login(providerId: String, cookies: Map<String, String>): Result<String> =
        oneshot { it.login(providerId, cookies) }.onSuccess {
            providers.value?.mutate { list ->
                list.toMutableList().map {
                    if (it.id == providerId &&
                        it.isLogged == false
                    ) it.copy(isLogged = true)
                    else it
                }
            }
        }

    suspend fun logout(providerId: String): Result<String> =
        oneshot { it.logout(providerId) }.onSuccess {
            providers.value?.mutate { list ->
                list.toMutableList().map {
                    if (it.id == providerId &&
                        it.isLogged == true
                    ) it.copy(isLogged = false)
                    else it
                }
            }
        }

    suspend fun search(
        providerId: String,
        keywords: String,
    ) = daoFlow.filterNotNull().flatMapLatest {
        remotePagingList(
            startKey = 0,
            loader = { page ->
                it.mapCatching { dao -> dao.search(providerId, page, keywords) }
                    .map { Page(it, if (it.isEmpty()) null else page + 1) }
            },
        )
    }

    fun getBoard(
        providerId: String,
        boardId: String,
        filters: Map<String, Int>,
    ) = daoFlow.filterNotNull().flatMapLatest {
        remotePagingList(
            startKey = 0,
            loader = { page ->
                it.mapCatching { dao -> dao.getBoard(providerId, boardId, page, filters) }
                    .map { Page(it, if (it.isEmpty()) null else page + 1) }
            },
        )
    }

    fun getManga(
        providerId: String,
        mangaId: String
    ): Flow<RemoteData<MangaDetailDto>> =
        daoFlow.filterNotNull().flatMapLatest {
            remoteData(
                loader = { it.mapCatching { dao -> dao.getManga(providerId, mangaId) } },
                onStart = { mangaActionChannels.add(it) },
                onClose = { mangaActionChannels.remove(it) },
            )
        }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadataDto
    ): Result<String> = oneshot {
        it.updateMangaMetadata(providerId, mangaId, metadata)
    }

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        cover: ByteArray,
        coverType: String,
    ): Result<String> = oneshot {
        it.updateMangaCover(providerId, mangaId, cover, coverType)
    }

    fun getComment(
        providerId: String,
        mangaId: String,
    ) = daoFlow.filterNotNull().flatMapLatest {
        remotePagingList(
            startKey = 0,
            loader = { page ->
                it.mapCatching { dao -> dao.getComment(providerId, mangaId, page) }
                    .map { Page(it, if (it.isEmpty()) null else page + 1) }
            },
        )
    }

    suspend fun getContent(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String
    ): Result<List<String>> = oneshot {
        it.getContent(providerId, mangaId, collectionId, chapterId)
    }

    suspend fun getRandomMangaFromLibrary(): Result<MangaDto> =
        oneshot { it.getRandomMangaFromLibrary() }

    // Library API
    suspend fun searchFromLibrary(keywords: String = "") =
        daoFlow.filterNotNull().flatMapLatest {
            remotePagingList(
                startKey = 0,
                loader = { page ->
                    it.mapCatching { dao -> dao.searchFromLibrary(page, keywords) }
                        .map { Page(it, if (it.isEmpty()) null else page + 1) }
                },
            )
        }

    suspend fun addMangaToLibrary(providerId: String, mangaId: String): Result<String> =
        oneshot { it.addMangaToLibrary(providerId, mangaId) }.onSuccess {
            mangaActionChannels.forEach { manga ->
                manga.mutate {
                    if (it.providerId == providerId &&
                        it.id == mangaId &&
                        it.state == MangaState.Remote
                    ) it.copy(state = MangaState.RemoteInLibrary)
                    else it
                }
            }
        }

    suspend fun removeMangaFromLibrary(providerId: String, mangaId: String): Result<String> =
        oneshot { it.removeMangaFromLibrary(providerId, mangaId) }.onSuccess {
            mangaActionChannels.forEach { manga ->
                manga.mutate {
                    if (it.providerId == providerId &&
                        it.id == mangaId &&
                        it.state == MangaState.RemoteInLibrary
                    ) it.copy(state = MangaState.Remote)
                    else it
                }
            }
        }

    suspend fun removeMultipleMangasFromLibrary(mangas: List<MangaKeyDto>): Result<String> =
        oneshot { it.removeMultipleMangasFromLibrary(mangas) }
}