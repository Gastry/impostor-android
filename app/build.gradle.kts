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

android {
    namespace = "com.impostorparty.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gastry.elimpostor"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
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
