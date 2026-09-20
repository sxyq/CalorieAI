import java.io.File
import java.security.KeyStore
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

val localProperties = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

fun readSecretProperty(propertyName: String, envName: String): String {
    return localProperties.getProperty(propertyName)
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv(envName)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        ?: ""
}

fun readBuildProperty(propertyName: String, envName: String): String {
    return localProperties.getProperty(propertyName)
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv(envName)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        ?: ""
}

fun escapeBuildConfigString(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
}

val localOcrServiceUrl: String = (
    localProperties.getProperty("local.ocr.service.url")
        ?: System.getenv("LOCAL_OCR_SERVICE_URL")
        ?: ""
).replace("\"", "\\\"")
val defaultAiApiUrl = readSecretProperty("default.ai.api.url", "DEFAULT_AI_API_URL")
    .ifBlank { "https://oneapi.sxyq27.online/v1/chat/completions" }
val defaultAiModelId = readSecretProperty("default.ai.model.id", "DEFAULT_AI_MODEL_ID")
    .ifBlank { "gpt-5.6-luna" }
val updateCheckUrl = readBuildProperty("update.check.url", "UPDATE_CHECK_URL")
    .ifBlank { "http://101.132.250.38:80/android/stable/latest.json" }
val updateDownloadBaseUrl = readBuildProperty(
    "update.download.base.url",
    "UPDATE_DOWNLOAD_BASE_URL"
).ifBlank { "http://101.132.250.38:80" }
val directUpdateCheckUrl = "http://101.132.250.38:80/android/stable/latest.json"
val directUpdateDownloadBaseUrl = "http://101.132.250.38:80"
require(
    updateCheckUrl == directUpdateCheckUrl && updateDownloadBaseUrl == directUpdateDownloadBaseUrl
) {
    "Update check and download URLs must use http://101.132.250.38:80."
}
val bundledPaddleOcrRoot: String = (
    localProperties.getProperty("bundled.paddle.ocr.root")
        ?: System.getenv("BUNDLED_PADDLE_OCR_ROOT")
        ?: File(System.getProperty("user.home"), ".paddlex/official_models").absolutePath
).replace("\"", "\\\"")
val releaseKeystorePath = readSecretProperty("release.keystore.path", "RELEASE_KEYSTORE_PATH")
val releaseStorePassword = readSecretProperty("release.store.password", "RELEASE_STORE_PASSWORD")
val releaseKeyAlias = readSecretProperty("release.key.alias", "RELEASE_KEY_ALIAS")
val releaseKeyPassword = readSecretProperty("release.key.password", "RELEASE_KEY_PASSWORD")
val releaseKeyPasswordForSigning = run {
    if (releaseKeystorePath.isBlank() || releaseStorePassword.isBlank()) {
        releaseKeyPassword
    } else {
        runCatching {
            File(releaseKeystorePath).inputStream().use { input ->
                KeyStore.getInstance("PKCS12").apply {
                    load(input, releaseStorePassword.toCharArray())
                }
            }
            // PKCS12 stores use the store password for private-key entries.
            releaseStorePassword
        }.getOrDefault(releaseKeyPassword)
    }
}
val hasReleaseSigning = releaseKeystorePath.isNotBlank() &&
    releaseStorePassword.isNotBlank() &&
    releaseKeyAlias.isNotBlank() &&
    releaseKeyPasswordForSigning.isNotBlank()

android {
    namespace = "com.calorieai.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.calorieai.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 138
        versionName = "1.3.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // 构建时间
        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
        // 非 Play 版本更新检查接口（只读取公开版本元数据，不包含密钥）。
        buildConfigField(
            "String",
            "UPDATE_CHECK_URL",
            "\"${escapeBuildConfigString(updateCheckUrl)}\""
        )
        buildConfigField(
            "String",
            "UPDATE_DOWNLOAD_BASE_URL",
            "\"${escapeBuildConfigString(updateDownloadBaseUrl)}\""
        )
        buildConfigField("String", "LOCAL_OCR_SERVICE_URL", "\"$localOcrServiceUrl\"")
        buildConfigField("String", "DEFAULT_AI_API_URL", "\"${escapeBuildConfigString(defaultAiApiUrl)}\"")
        buildConfigField("String", "DEFAULT_AI_MODEL_ID", "\"${escapeBuildConfigString(defaultAiModelId)}\"")
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPasswordForSigning
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }
    
    
    // 打包选项
    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                this.outputFileName = "CalorieAI-v${versionName}.apk"
            }
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
    sourceSets {
        getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/assets/bundledOcr"))
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    androidResources {
        noCompress += setOf("pdiparams", "json", "yml", "txt", "onnx")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val bundledOcrGeneratedDir = layout.buildDirectory.dir("generated/assets/bundledOcr/ocr/paddle_nutrition")
val bundledOcrSourceRoot = file(bundledPaddleOcrRoot)
val bundledOcrModelSpecs = listOf(
    Triple("det", "PP-OCRv5_mobile_det", "PP-OCRv5 mobile detection"),
    Triple("rec", "PP-OCRv5_server_rec", "PP-OCRv5 server recognition"),
    Triple("cls", "PP-LCNet_x1_0_textline_ori", "PP-LCNet textline orientation")
)

val syncBundledOcrAssets by tasks.registering(Copy::class) {
    val outputRoot = bundledOcrGeneratedDir.get().asFile

    doFirst {
        delete(outputRoot)
        outputRoot.mkdirs()

        val manifestEntries = bundledOcrModelSpecs.map { (folder, modelDirName, displayName) ->
            val sourceDir = bundledOcrSourceRoot.resolve(modelDirName)
            if (!sourceDir.exists()) {
                throw GradleException(
                    "Missing bundled OCR model directory: ${sourceDir.absolutePath}. " +
                        "Set bundled.paddle.ocr.root in local.properties if needed."
                )
            }
            mapOf(
                "id" to folder,
                "name" to displayName,
                "sourceDir" to sourceDir.absolutePath.replace("\\", "\\\\"),
                "assetDir" to "ocr/paddle_nutrition/$folder"
            )
        }

        val manifestFile = outputRoot.resolve("manifest.json")
        val modelsJson = manifestEntries.joinToString(",\n") { entry ->
            """
            {
              "id": "${entry["id"]}",
              "name": "${entry["name"]}",
              "sourceDir": "${entry["sourceDir"]}",
              "assetDir": "${entry["assetDir"]}"
            }
            """.trimIndent()
        }
        manifestFile.writeText(
            """
            {
              "createdAtEpochMs": ${System.currentTimeMillis()},
              "bundledFrom": "${bundledOcrSourceRoot.absolutePath.replace("\\", "\\\\")}",
              "models": [
            $modelsJson
              ]
            }
            """.trimIndent(),
            Charsets.UTF_8
        )
    }

    bundledOcrModelSpecs.forEach { (folder, modelDirName, _) ->
        from(bundledOcrSourceRoot.resolve(modelDirName)) {
            include("**/*")
            into(folder)
        }
    }
    into(bundledOcrGeneratedDir)
    includeEmptyDirs = false
}

tasks.named("preBuild") {
    dependsOn(syncBundledOcrAssets)
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation:1.6.1")

    // Material3
    implementation("com.google.android.material:material:1.11.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // Coil (图片加载)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ML Kit (文字识别)
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Offline Speech Recognition (SenseVoiceSmall via sherpa-onnx)
    implementation("com.bihe0832.android:lib-sherpa-onnx:6.25.21")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // UI Chrome & Blur
    implementation("dev.chrisbanes.haze:haze:0.7.3")
    implementation("dev.chrisbanes.haze:haze-materials:0.7.3")
}
