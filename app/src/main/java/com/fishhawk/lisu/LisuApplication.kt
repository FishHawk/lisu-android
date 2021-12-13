package com.fishhawk.lisu

import android.app.Application
import android.webkit.URLUtil
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.fishhawk.lisu.data.database.ApplicationDatabase
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.ServerHistoryRepository
import com.fishhawk.lisu.data.datastore.PreferenceRepository
import com.fishhawk.lisu.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.lisu.data.remote.GitHubRepository
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.notification.Notifications
import com.fishhawk.lisu.ui.main.MainViewModel
import com.fishhawk.lisu.ui.explore.ExploreViewModel
import com.fishhawk.lisu.ui.gallery.GalleryViewModel
import com.fishhawk.lisu.ui.globalsearch.GlobalSearchViewModel
import com.fishhawk.lisu.ui.history.HistoryViewModel
import com.fishhawk.lisu.ui.library.LibrarySearchViewModel
import com.fishhawk.lisu.ui.library.LibraryViewModel
import com.fishhawk.lisu.ui.more.MoreViewModel
import com.fishhawk.lisu.ui.provider.ProviderSearchViewModel
import com.fishhawk.lisu.ui.provider.ProviderViewModel
import com.fishhawk.lisu.ui.reader.ReaderViewModel
import com.fishhawk.lisu.util.interceptor.ProgressInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

lateinit var PR: PreferenceRepository

@Suppress("unused")
class LisuApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LisuApplication)
            modules(appModule)
        }

        PR = object : KoinComponent {
            val pr by inject<PreferenceRepository>()
        }.pr

        Notifications.createChannels(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor(ProgressInterceptor())
                    .build()
            }
            .build()
    }
}

val appModule = module {
    single { PreferenceRepository(androidApplication()) }
    single { ProviderBrowseHistoryRepository(androidApplication()) }

    single<Flow<Result<Retrofit>?>> {
        get<PreferenceRepository>().serverAddress.flow
            .map { address ->
                address
                    .let { it.ifBlank { null } }
                    ?.let { if (URLUtil.isNetworkUrl(it)) it else "http://$it" }
                    ?.let { if (it.last() == '/') it else "$it/" }
            }
            .distinctUntilChanged()
            .map { url ->
                if (url == null)
                    return@map Result.failure(Exception("No server selected"))
                try {
                    Result.success(
                        Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                    )
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
            .shareIn(
                CoroutineScope(SupervisorJob() + Dispatchers.Main),
                SharingStarted.Eagerly,
                1
            )
    }

    single { RemoteLibraryRepository(get()) }
    single { RemoteProviderRepository(get()) }
    single { GitHubRepository() }

    single {
        Room.databaseBuilder(
            androidApplication(),
            ApplicationDatabase::class.java,
            "Test.db"
        ).build()
    }

    single { ReadingHistoryRepository(get<ApplicationDatabase>().readingHistoryDao()) }
    single { SearchHistoryRepository(get<ApplicationDatabase>().searchHistoryDao()) }
    single { ServerHistoryRepository(get<ApplicationDatabase>().serverHistoryDao()) }

    single { MainViewModel(get()) }

    viewModel { LibraryViewModel(get()) }
    viewModel { LibrarySearchViewModel(get(), get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { ExploreViewModel(get()) }
    viewModel { MoreViewModel(get()) }
    viewModel { GlobalSearchViewModel(get(), get(), get()) }
    viewModel { ProviderViewModel(get(), get(), get(), get()) }
    viewModel { ProviderSearchViewModel(get(), get(), get()) }

    viewModel { GalleryViewModel(get(), get(), get(), get()) }

    viewModel { ReaderViewModel(get(), get(), get()) }
}