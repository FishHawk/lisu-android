package com.fishhawk.lisu

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.fishhawk.lisu.data.database.*
import com.fishhawk.lisu.data.datastore.PreferenceRepository
import com.fishhawk.lisu.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.lisu.data.network.GitHubRepository
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.base.Connectivity
import com.fishhawk.lisu.notification.Notifications
import com.fishhawk.lisu.ui.download.DownloadViewModel
import com.fishhawk.lisu.ui.explore.ExploreViewModel
import com.fishhawk.lisu.ui.gallery.GalleryViewModel
import com.fishhawk.lisu.ui.globalsearch.GlobalSearchViewModel
import com.fishhawk.lisu.ui.history.HistoryViewModel
import com.fishhawk.lisu.ui.library.LibraryViewModel
import com.fishhawk.lisu.ui.main.MainViewModel
import com.fishhawk.lisu.ui.more.MoreViewModel
import com.fishhawk.lisu.ui.provider.ProviderViewModel
import com.fishhawk.lisu.ui.reader.ReaderViewModel
import com.fishhawk.lisu.util.interceptor.ProgressInterceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

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
        return ImageLoader
            .Builder(applicationContext)
            .respectCacheHeaders(false)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor(ProgressInterceptor())
                    .build()
            }
            .build()
    }
}

val appModule = module {
    single { Connectivity(get()) }

    single { PreferenceRepository(androidApplication()) }
    single { ProviderBrowseHistoryRepository(androidApplication()) }

    single(named("address")) {
        get<PreferenceRepository>().serverAddress.flow
    }

    single { LisuRepository(get(named("address"))) }
    single { GitHubRepository() }

    single {
        Room.databaseBuilder(
            androidApplication(),
            ApplicationDatabase::class.java,
            "Test.db"
        ).build()
    }

    single { MangaSettingRepository(get<ApplicationDatabase>().mangaSettingDao()) }
    single { ReadingHistoryRepository(get<ApplicationDatabase>().readingHistoryDao()) }
    single { SearchHistoryRepository(get<ApplicationDatabase>().searchHistoryDao()) }
    single { ServerHistoryRepository(get<ApplicationDatabase>().serverHistoryDao()) }

    single { MainViewModel(get()) }

    viewModel { LibraryViewModel(get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { ExploreViewModel(get()) }
    viewModel { MoreViewModel(get()) }

    viewModel { DownloadViewModel(get()) }
    viewModel { GlobalSearchViewModel(get(), get(), get()) }
    viewModel { ProviderViewModel(get(), get(), get(), get()) }
    viewModel { GalleryViewModel(get(), get(), get()) }

    viewModel { ReaderViewModel(get(), get(), get(), get()) }
}