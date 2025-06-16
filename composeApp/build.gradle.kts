import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

// Firebase plugins are applied conditionally to avoid issues when Google Services JSON is not available
if (project.file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.appdistribution")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            // kotlinx.datetime
            implementation(libs.kotlinx.datetime)
            
            // Napier
            implementation(libs.napier)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mockk)
        }
        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.androidx.room.testing)
            implementation(libs.androidx.test.core)
            implementation(libs.kotlin.testJunit)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.robolectric)
        }
    }
}

android {
    namespace = "com.nagopy.kmp.habittracker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.nagopy.kmp.habittracker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        disable += "NullSafeMutableLiveData"
    }
}

// Firebase App Distribution configuration - only applied when google-services.json exists
if (project.file("google-services.json").exists()) {
    configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
        // アプリID - 実際のFirebaseプロジェクトで設定が必要
        appId = project.findProperty("FIREBASE_APP_ID") as String? ?: ""
        
        // サービスアカウントの鍵ファイル - 実際のサービスアカウントファイルのパスを設定
        serviceCredentialsFile = project.findProperty("FIREBASE_SERVICE_ACCOUNT_FILE") as String? ?: ""
        
        // 配信設定
        groups = project.findProperty("TESTER_GROUPS") as String? ?: "internal-testers"
        releaseNotes = project.findProperty("RELEASE_NOTES") as String? ?: "最新のテストビルドです。新機能や修正内容をテストしてください。"
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

