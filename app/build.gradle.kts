plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

import java.io.File
import java.util.Properties

fun loadReleaseSigning(): Map<String, String>? {
    val fromEnv = listOf(
        "BANDNO_STORE_FILE",
        "BANDNO_STORE_PASSWORD",
        "BANDNO_KEY_ALIAS",
        "BANDNO_KEY_PASSWORD",
    ).associateWith { env -> System.getenv(env).orEmpty() }
    if (fromEnv.values.all { it.isNotBlank() }) {
        return mapOf(
            "storeFile" to fromEnv.getValue("BANDNO_STORE_FILE"),
            "storePassword" to fromEnv.getValue("BANDNO_STORE_PASSWORD"),
            "keyAlias" to fromEnv.getValue("BANDNO_KEY_ALIAS"),
            "keyPassword" to fromEnv.getValue("BANDNO_KEY_PASSWORD"),
        )
    }
    val propsFile = rootProject.file("keystore.properties")
    if (!propsFile.isFile) return null
    val props = Properties().apply { propsFile.inputStream().use { load(it) } }
    val keys = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
    if (keys.any { props.getProperty(it).isNullOrBlank() }) return null
    return keys.associateWith { props.getProperty(it) }
}

android {
    namespace = "dev.bandno.app"
    compileSdk = 36

    val releaseSigning = loadReleaseSigning()
    if (releaseSigning != null) {
        signingConfigs {
            create("release") {
                val raw = releaseSigning.getValue("storeFile")
                val store = File(raw)
                storeFile = if (store.isAbsolute) store else rootProject.file(raw)
                storePassword = releaseSigning.getValue("storePassword")
                keyAlias = releaseSigning.getValue("keyAlias")
                keyPassword = releaseSigning.getValue("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "dev.bandno.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 5
        versionName = "0.0.5"
        val abiFiltersProp = (project.findProperty("abiFilters") as String?)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
        if (abiFiltersProp.isNotEmpty()) {
            ndk {
                abiFilters += abiFiltersProp
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (releaseSigning != null) {
                signingConfigs.getByName("release")
            } else {
                null
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

afterEvaluate {
    tasks.named("assembleRelease").configure {
        doFirst {
            check(android.signingConfigs.findByName("release") != null) {
                "Release signing is missing. Copy keystore.properties.example to keystore.properties " +
                    "or set BANDNO_STORE_FILE / BANDNO_STORE_PASSWORD / BANDNO_KEY_ALIAS / BANDNO_KEY_PASSWORD."
            }
        }
    }
}

dependencies {
    implementation(project(":decision"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
