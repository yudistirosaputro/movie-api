plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// The key lives in the user-level ~/.gradle/gradle.properties and is never committed or printed.
// .properties files keep quotes literally, so MOVIE_API="..." would otherwise reach TMDB with the quotes.
val movieApiKey: Provider<String> = providers.gradleProperty("MOVIE_API")
    .map { it.trim().removeSurrounding("\"").removeSurrounding("'").trim() }
    .orElse("")

if (movieApiKey.get().isEmpty()) {
    logger.warn("w: MOVIE_API Gradle property is missing. The app builds, but every TMDB call shows \"API key missing\".")
}

android {
    namespace = "com.yudistirosaputro.movieapi"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.yudistirosaputro.movieapi"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val escapedKey = movieApiKey.get().replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "MOVIE_API", "\"$escapedKey\"")
        buildConfigField("String", "TMDB_BASE_URL", "\"https://api.themoviedb.org/3/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = false
        shaders = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = true
        checkDependencies = false
        disable += "GradleDependency"
        disable += "AndroidGradlePluginVersion"
        disable += "NewerVersionAvailable"
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.register("checkMovieApiKey") {
    group = "verification"
    description = "Reports whether the MOVIE_API Gradle property is set, without revealing it."
    val present = movieApiKey.map { it.isNotEmpty() }
    doLast {
        println("MOVIE_API: " + if (present.get()) "present" else "missing")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.youtube.player.core)

    debugImplementation(libs.dimock.ui)
    releaseImplementation(libs.dimock.okhttp.no.op)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver3)
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.paging.testing)
}
