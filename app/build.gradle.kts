import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    signingConfigs {
        getByName("debug") {
            val properties = Properties()
            properties.load(FileInputStream(project.rootProject.file("local.properties")))

            storeFile = file(properties.getProperty("signingConfigs.storeFile"))
            storePassword = properties.getProperty("signingConfigs.storePassword")
            keyAlias = properties.getProperty("signingConfigs.keyAlias")
            keyPassword = properties.getProperty("signingConfigs.keyPassword")
        }
        create("release") {
            val properties = Properties()
            properties.load(FileInputStream(project.rootProject.file("local.properties")))

            storeFile = file(properties.getProperty("signingConfigs.storeFile"))
            storePassword = properties.getProperty("signingConfigs.storePassword")
            keyAlias = properties.getProperty("signingConfigs.keyAlias")
            keyPassword = properties.getProperty("signingConfigs.keyPassword")
        }
    }

    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.fishhawk.driftinglibraryandroid"
        minSdk = 21
        targetSdk = 30
        versionCode = 5
        versionName = "0.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.0-rc02"
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("androidx.compose.ui:ui:1.0.0-rc02")
    implementation("androidx.compose.ui:ui-tooling:1.0.0-rc02")
    implementation("androidx.compose.foundation:foundation:1.0.0-rc02")
    implementation("androidx.compose.material:material:1.0.0-rc02")
    implementation("androidx.compose.material:material-icons-core:1.0.0-rc02")
    implementation("androidx.compose.material:material-icons-extended:1.0.0-rc02")
    implementation("androidx.compose.runtime:runtime-livedata:1.0.0-rc02")

    implementation("androidx.datastore:datastore-preferences:1.0.0-rc01")

    implementation("androidx.room:room-runtime:2.3.0")
    implementation("androidx.room:room-ktx:2.3.0")
    kapt("androidx.room:room-compiler:2.3.0")

    implementation("androidx.paging:paging-runtime-ktx:3.0.0")
    implementation("androidx.paging:paging-compose:1.0.0-alpha11")

    implementation("com.google.accompanist:accompanist-flowlayout:0.14.0")
    implementation("com.google.accompanist:accompanist-insets:0.14.0")
    implementation("com.google.accompanist:accompanist-insets-ui:0.14.0")
    implementation("com.google.accompanist:accompanist-pager:0.14.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.14.0")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.14.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.14.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.14.0")

    implementation("io.coil-kt:coil:1.3.0")
    implementation("io.coil-kt:coil-compose:1.3.0")

    implementation("com.google.dagger:hilt-android:2.37")
    kapt("com.google.dagger:hilt-android-compiler:2.37")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.navigation:navigation-compose:2.4.0-alpha04")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")

    // UI Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.0-rc02")


    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.core:core-ktx:1.6.0")

    implementation("androidx.activity:activity-ktx:1.3.0-rc02")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")

    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    // Restful
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.code.gson:gson:2.8.7")

    // UI library
    implementation("com.google.android.material:material:1.5.0-alpha01")

    // OpenCV
    implementation("com.quickbirdstudios:opencv:4.1.0")
}
