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

    compileSdk = 31
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.fishhawk.driftinglibraryandroid"
        minSdk = 21
        targetSdk = 30
        versionCode = 5
        versionName = "0.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true

//        ndk {
//            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
//        }
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
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-alpha04"
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("androidx.activity:activity-compose:1.3.1")

    implementation("androidx.compose.ui:ui:1.1.0-alpha04")
    implementation("androidx.compose.ui:ui-tooling:1.1.0-alpha04")
    implementation("androidx.compose.foundation:foundation:1.1.0-alpha04")
    implementation("androidx.compose.material:material:1.1.0-alpha04")
    implementation("androidx.compose.material:material-icons-core:1.1.0-alpha04")
    implementation("androidx.compose.material:material-icons-extended:1.1.0-alpha04")

    implementation("androidx.navigation:navigation-compose:2.4.0-alpha07")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")

    implementation("androidx.paging:paging-compose:1.0.0-alpha12")

    implementation("com.google.dagger:hilt-android:2.38.1")
    kapt("com.google.dagger:hilt-android-compiler:2.38.1")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.room:room-runtime:2.4.0-alpha04")
    implementation("androidx.room:room-ktx:2.4.0-alpha04")
    kapt("androidx.room:room-compiler:2.4.0-alpha04")


    implementation("com.google.accompanist:accompanist-flowlayout:0.17.0")
    implementation("com.google.accompanist:accompanist-insets:0.17.0")
    implementation("com.google.accompanist:accompanist-insets-ui:0.17.0")
    implementation("com.google.accompanist:accompanist-pager:0.17.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.17.0")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.17.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.17.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.17.0")

    implementation("io.coil-kt:coil:1.3.2")
    implementation("io.coil-kt:coil-compose:1.3.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")


    // UI Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.2")

    // implementation("com.quickbirdstudios:opencv:4.1.0")
}
