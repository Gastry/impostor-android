import java.nio.charset.MalformedInputException
import java.nio.charset.StandardCharsets

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
}

val debugAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
val debugHomeBannerAdUnitId = "ca-app-pub-3940256099942544/9214589741"
val removeAdsProductId = providers.gradleProperty("removeAdsProductId").orNull ?: "remove_ads"
val releaseAdMobAppId = providers.gradleProperty("admobAppId").orNull ?: debugAdMobAppId
val releaseHomeBannerAdUnitId = providers.gradleProperty("admobHomeBannerAdUnitId").orNull.orEmpty()
val releaseStoreFilePath =
    providers.gradleProperty("releaseStoreFile").orNull ?: "${rootDir}/signing/ElImpostorUpload.jks"
val releaseKeyAlias = providers.gradleProperty("releaseKeyAlias").orNull ?: "gastry"
val releaseStorePassword = providers.gradleProperty("releaseStorePassword").orNull
val releaseKeyPassword = providers.gradleProperty("releaseKeyPassword").orNull ?: releaseStorePassword
val hasReleaseSigning =
    releaseStorePassword.isNullOrBlank().not() &&
        releaseKeyPassword.isNullOrBlank().not() &&
        file(releaseStoreFilePath).exists()
val appVersionCode = 9

val suspiciousStringMarkers = listOf(
    "\u00C3",
    "\u00C2",
    "\u00E2\u20AC\u00A6",
    "\u00E2\u20AC\u2122",
    "\u00E2\u20AC\u0153",
    "\u00E2\u20AC\u009D",
    "\u00E2\u20AC\u201C",
    "\u00E2\u20AC\u201D",
    "\u00E6\u2014\u00A5\u00E6\u0153\u00AC\u00E8\u00AA\u017E",
)

val checkStringsEncoding by tasks.registering {
    group = "verification"
    description = "Fails if any Android strings.xml file contains mojibake or invalid UTF-8."

    val stringFiles = fileTree("src/main/res") {
        include("**/strings.xml")
    }

    inputs.files(stringFiles)

    doLast {
        val failures = mutableListOf<String>()

        stringFiles.files.sorted().forEach { file ->
            val content = try {
                file.readText(StandardCharsets.UTF_8)
            } catch (error: MalformedInputException) {
                failures += "${file.relativeTo(projectDir)}: invalid UTF-8 (${error.message ?: "malformed input"})"
                return@forEach
            }

            suspiciousStringMarkers.forEach { marker ->
                if (content.contains(marker)) {
                    failures += "${file.relativeTo(projectDir)}: contains suspicious marker '$marker'"
                }
            }
        }

        if (failures.isNotEmpty()) {
            error(
                buildString {
                    appendLine("Detected broken encoding in Android string resources:")
                    failures.distinct().sorted().forEach { appendLine(" - $it") }
                },
            )
        }
    }
}

android {
    namespace = "com.impostorparty.app"
    compileSdk = 35

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseStoreFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.gastry.elinfiltrado"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = "1.$appVersionCode"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField("boolean", "ADS_ENABLED", (releaseHomeBannerAdUnitId.isNotBlank()).toString())
            buildConfigField("String", "HOME_BANNER_AD_UNIT_ID", "\"$releaseHomeBannerAdUnitId\"")
            buildConfigField("String", "REMOVE_ADS_PRODUCT_ID", "\"$removeAdsProductId\"")
            resValue("string", "admob_app_id", releaseAdMobAppId)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField("String", "HOME_BANNER_AD_UNIT_ID", "\"$debugHomeBannerAdUnitId\"")
            buildConfigField("String", "REMOVE_ADS_PRODUCT_ID", "\"$removeAdsProductId\"")
            resValue("string", "admob_app_id", debugAdMobAppId)
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

    lint {
        // AGP/Kotlin can crash lint when analyzing test-only Kotlin sources.
        checkTestSources = false
        ignoreTestSources = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.named("preBuild") {
    dependsOn(checkStringsEncoding)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.play.review.ktx)
    implementation(libs.google.play.services.ads.lite)
    implementation(libs.google.play.billing)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.app.cash.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
