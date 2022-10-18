package com.fishhawk.lisu.data.network

import com.fishhawk.lisu.data.network.base.*
import com.fishhawk.lisu.data.network.dao.LisuDownloadDao
import com.fishhawk.lisu.data.network.dao.LisuLibraryDao
import com.fishhawk.lisu.data.network.dao.LisuProviderDao
import com.fishhawk.lisu.data.network.model.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class LisuRepository(
    private val connectivity: Connectivity,
    urlFlow: Flow<String>,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val urlState = urlFlow
        .map { runCatching { URLBuilder(it).buildString() } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val client: HttpClient = HttpClient(OkHttp) {
        expectSuccess = true

        install(Resources)
        install(WebSockets) { pingInterval = 20_000 }
        install(ContentNegotiation) { json(Json) }

        defaultRequest {
            urlState.value?.onSuccess { url(it) }
        }
    }

    private val providerDaoFlow =
        urlState.filterNotNull().map { it.map { LisuProviderDao(client) } }

    private val libraryDaoFlow =
        urlState.filterNotNull().map { it.map { LisuLibraryDao(client) } }

    private val downloadDaoFlow =
        urlState.filterNotNull().map { it.map { LisuDownloadDao(client) } }

    private suspend inline fun <T> providerOneshot(
        action: LisuProviderDao.() -> T,
    ) = providerDaoFlow.first().mapCatching { with(it) { action() } }

    private suspend inline fun <T> libraryOneshot(
        action: LisuLibraryDao.() -> T,
    ) = libraryDaoFlow.first().mapCatching { with(it) { action() } }

    private suspend inline fun <T> downloadOneshot(
        action: LisuDownloadDao.() -> T,
    ) = downloadDaoFlow.first().mapCatching { with(it) { action() } }

    private val mangaActionChannels = mutableListOf<RemoteDataActionChannel<MangaDetailDto>>()
    private val providerMangaListActionChannels = mutableListOf<RemoteListActionChannel<MangaDto>>()
    private val libraryMangaListActionChannels = mutableListOf<RemoteListActionChannel<MangaDto>>()

    // Provider API
    val providers = providerDaoFlow.flatMapLatest {
        remoteData(
            connectivity = connectivity,
            loader = { it.mapCatching { dao -> dao.listProvider() } },
        )
    }.stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun loginByCookies(
        providerId: String,
        cookies: Map<String, String>,
    ): Result<String> = providerOneshot {
        loginByCookies(
            providerId = providerId,
            cookies = cookies,
        )
    }.onSuccess {
        mutateProvider(providerId = providerId) {
            it.copy(isLogged = true)
        }
    }

    suspend fun loginByPassword(
        providerId: String,
        username: String,
        password: String,
    ): Result<String> = providerOneshot {
        loginByPassword(
            providerId = providerId,
            username = username,
            password = password,
        )
    }.onSuccess {
        mutateProvider(providerId = providerId) {
            it.copy(isLogged = true)
        }
    }

    suspend fun logout(
        providerId: String,
    ): Result<String> = providerOneshot {
        logout(
            providerId = providerId,
        )
    }.onSuccess {
        mutateProvider(providerId = providerId) {
            it.copy(isLogged = false)
        }
    }

    fun getBoard(
        providerId: String,
        boardId: BoardId,
        filterValues: BoardFilterValue,
        keywords: String? = null,
    ): Flow<RemoteList<MangaDto>> = providerDaoFlow.flatMapLatest {
        remotePagingList(
            connectivity = connectivity,
            loader = { page ->
                it.mapCatching { dao ->
                    dao.getBoard(
                        providerId = providerId,
                        boardId = boardId,
                        page = page,
                        keywords = keywords,
                        filterValues = filterValues,
                    )
                }
            },
            onStart = { providerMangaListActionChannels.add(it) },
            onClose = { providerMangaListActionChannels.remove(it) },
        )
    }

    fun getManga(
        providerId: String,
        mangaId: String,
    ): Flow<RemoteData<MangaDetailDto>> = providerDaoFlow.flatMapLatest {
        remoteData(
            connectivity = connectivity,
            loader = {
                it.mapCatching { dao ->
                    dao.getManga(
                        providerId = providerId,
                        mangaId = mangaId,
                    )
                }
            },
            onStart = { mangaActionChannels.add(it) },
            onClose = { mangaActionChannels.remove(it) },
        )
    }

    fun getComment(
        providerId: String,
        mangaId: String,
    ): Flow<RemoteList<CommentDto>> = providerDaoFlow.flatMapLatest {
        remotePagingList(
            connectivity = connectivity,
            loader = { page ->
                it.mapCatching { dao ->
                    dao.getComment(
                        providerId = providerId,
                        mangaId = mangaId,
                        page = page,
                    )
                }
            },
        )
    }

    suspend fun getContent(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): Result<List<String>> = providerOneshot {
        getContent(
            providerId = providerId,
            mangaId = mangaId,
            collectionId = collectionId,
            chapterId = chapterId,
        )
    }

    // Library API
    suspend fun searchFromLibrary(
        keywords: String,
    ): Flow<RemoteList<MangaDto>> = libraryDaoFlow.flatMapLatest {
        remotePagingList(
            connectivity = connectivity,
            loader = { page -> it.mapCatching { dao -> dao.search(page, keywords) } },
            onStart = { libraryMangaListActionChannels.add(it) },
            onClose = { libraryMangaListActionChannels.remove(it) },
        )
    }

    suspend fun getRandomMangaFromLibrary(
    ): Result<MangaDto> = libraryOneshot {
        getRandomManga()
    }

    suspend fun removeMultipleMangasFromLibrary(
        mangas: List<MangaKeyDto>,
    ): Result<String> = libraryOneshot {
        removeMultipleMangas(
            mangas = mangas,
        )
    }.onSuccess {
        providerMangaListActionChannels.forEach { ch ->
            ch.mutate { list ->
                list.map {
                    if (
                        mangas.any { removedManga ->
                            it.providerId == removedManga.providerId &&
                                    it.id == removedManga.id
                        }
                    ) it.copy(state = MangaState.Remote)
                    else it
                }.toMutableList()
            }
        }
        libraryMangaListActionChannels.forEach { ch ->
            ch.mutate { list ->
                val removed = list.removeIf {
                    mangas.any { removedManga ->
                        it.providerId == removedManga.providerId &&
                                it.id == removedManga.id
                    }
                }
                if (removed) list.toMutableList() else list
            }
        }
    }

    suspend fun addMangaToLibrary(
        providerId: String,
        mangaId: String,
    ): Result<String> = libraryOneshot {
        addManga(
            providerId = providerId,
            mangaId = mangaId,
        )
    }.onSuccess {
        mutateManga(providerId = providerId, mangaId = mangaId) {
            it.copy(state = MangaState.RemoteInLibrary)
        }
        mutateMangaInProviderList(providerId = providerId, mangaId = mangaId) {
            it.copy(state = MangaState.RemoteInLibrary)
        }
        libraryMangaListActionChannels.forEach { ch -> ch.reload() }
    }

    suspend fun removeMangaFromLibrary(
        providerId: String,
        mangaId: String,
    ): Result<String> = libraryOneshot {
        removeManga(
            providerId = providerId,
            mangaId = mangaId,
        )
    }.onSuccess {
        mutateManga(providerId = providerId, mangaId = mangaId) {
            it.copy(state = MangaState.Remote)
        }
        mutateMangaInProviderList(providerId = providerId, mangaId = mangaId) {
            it.copy(state = MangaState.Remote)
        }
        libraryMangaListActionChannels.forEach { ch ->
            ch.mutate { list ->
                val removed = list.removeIf { it.providerId == providerId && it.id == mangaId }
                if (removed) list.toMutableList() else list
            }
        }
    }

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        cover: ByteArray,
        coverType: String,
    ): Result<String> = libraryOneshot {
        updateMangaCover(
            providerId = providerId,
            mangaId = mangaId,
            cover = cover,
            coverType = coverType,
        )
    }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadata,
    ): Result<MangaMetadata> = libraryOneshot {
        updateMangaMetadata(
            providerId = providerId,
            mangaId = mangaId,
            metadata = metadata,
        )
    }.onSuccess { newMetadata ->
        mutateManga(providerId = providerId, mangaId = mangaId) {
            it.copy(
                title = newMetadata.title,
                authors = newMetadata.authors,
                isFinished = newMetadata.isFinished,
                description = newMetadata.description,
                tags = newMetadata.tags,
            )
        }
        mutateMangaInLibraryList(providerId = providerId, mangaId = mangaId) {
            it.copy(
                title = newMetadata.title,
                authors = newMetadata.authors,
                isFinished = newMetadata.isFinished,
            )
        }
        mutateMangaInProviderList(providerId = providerId, mangaId = mangaId) {
            it.copy(
                title = newMetadata.title,
                authors = newMetadata.authors,
                isFinished = newMetadata.isFinished,
            )
        }
    }

    // Download API
    fun listMangaDownloadTask(
    ): Flow<RemoteData<List<MangaDownloadTask>>> =
        downloadDaoFlow.flatMapLatest {
            remoteDataFromFlow(
                connectivity = connectivity,
                loader = { it.map { dao -> dao.listMangaDownloadTask() } },
            )
        }

    suspend fun startAllTasks(
    ): Result<String> = downloadOneshot {
        startAllTasks()
    }

    suspend fun startMangaTask(
        providerId: String,
        mangaId: String,
    ): Result<String> = downloadOneshot {
        startMangaTask(
            providerId = providerId,
            mangaId = mangaId,
        )
    }

    suspend fun startChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): Result<String> = downloadOneshot {
        startChapterTask(
            providerId = providerId,
            mangaId = mangaId,
            collectionId = collectionId,
            chapterId = chapterId,
        )
    }

    suspend fun cancelAllTasks(
    ): Result<String> = downloadOneshot {
        cancelAllTasks()
    }

    suspend fun cancelMangaTask(
        providerId: String,
        mangaId: String,
    ): Result<String> = downloadOneshot {
        cancelMangaTask(
            providerId = providerId,
            mangaId = mangaId,
        )
    }

    suspend fun cancelChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): Result<String> = downloadOneshot {
        cancelChapterTask(
            providerId = providerId,
            mangaId = mangaId,
            collectionId = collectionId,
            chapterId = chapterId,
        )
    }

    // Util
    private suspend fun mutateProvider(
        providerId: String,
        transform: (ProviderDto) -> ProviderDto,
    ) {
        providers.value?.mutate { list ->
            list.toMutableList().map {
                if (it.id == providerId) transform(it)
                else it
            }
        }
    }

    private suspend fun mutateManga(
        providerId: String,
        mangaId: String,
        transform: (MangaDetailDto) -> MangaDetailDto,
    ) {
        mangaActionChannels.forEach { ch ->
            ch.mutate {
                if (it.providerId == providerId && it.id == mangaId) transform(it)
                else it
            }
        }
    }

    private suspend fun mutateMangaInProviderList(
        providerId: String,
        mangaId: String,
        transform: (MangaDto) -> MangaDto,
    ) {
        providerMangaListActionChannels.forEach { ch ->
            ch.mutate { list ->
                if (list.firstOrNull()?.providerId != providerId) {
                    list
                } else {
                    list
                        .map { if (it.id == mangaId) transform(it) else it }
                        .toMutableList()
                }
            }
        }
    }

    private suspend fun mutateMangaInLibraryList(
        providerId: String,
        mangaId: String,
        transform: (MangaDto) -> MangaDto,
    ) {
        libraryMangaListActionChannels.forEach { ch ->
            ch.mutate { list ->
                list
                    .map { if (it.providerId == providerId && it.id == mangaId) transform(it) else it }
                    .toMutableList()
            }
        }
    }
}