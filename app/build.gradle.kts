import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("plugin.serialization") version "1.7.10"
    id("com.android.application") version "7.3.0-rc01"
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    id("org.jetbrains.kotlin.android") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.7.0"
    id("com.mikepenz.aboutlibraries.plugin") version "10.3.0"
}

android {
    signingConfigs {
        val properties = Properties()
        properties.load(FileInputStream(project.rootProject.file("local.properties")))
        getByName("debug") {
            storeFile = file(properties.getProperty("signingConfigs.storeFile"))
            storePassword = properties.getProperty("signingConfigs.storePassword")
            keyAlias = properties.getProperty("signingConfigs.keyAlias")
            keyPassword = properties.getProperty("signingConfigs.keyPassword")
        }
        create("release") {
            storeFile = file(properties.getProperty("signingConfigs.storeFile"))
            storePassword = properties.getProperty("signingConfigs.storePassword")
            keyAlias = properties.getProperty("signingConfigs.keyAlias")
            keyPassword = properties.getProperty("signingConfigs.keyPassword")
        }
    }

    namespace = "com.fishhawk.lisu"
    compileSdk = 33

    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.fishhawk.lisu"
        minSdk = 21
        targetSdk = 33
        versionCode = 5
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

//        ndk {
//            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
//        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.2")

    implementation("androidx.activity:activity-compose:1.6.0-rc02")

    val composeVersion = "1.3.0-beta02"
    implementation("androidx.compose.ui:ui:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")

    implementation("androidx.navigation:navigation-compose:2.5.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0-alpha02")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    val roomVersion = "2.5.0-alpha03"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    val koinVersion = "3.1.6"
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")

    val accompanistVersion = "0.26.3-beta"
    implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-insets:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-insets-ui:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-placeholder-material:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-webview:$accompanistVersion")

    val coilVersion = "2.2.1"
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")

    val ktorVersion = "2.1.1"
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Licenses
    implementation("com.mikepenz:aboutlibraries-compose:10.3.0")

    // UI Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.1.1")

    // implementation("com.quickbirdstudios:opencv:4.1.0")
}