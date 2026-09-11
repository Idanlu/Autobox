plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun getGitOutput(vararg args: String): String {
    return try {
        val process = ProcessBuilder(*args).directory(rootDir).redirectErrorStream(true).start()
        val text = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0) text else ""
    } catch (_: Exception) {
        ""
    }
}

val appVersionName: String = run {
    if (project.hasProperty("appVersion")) {
        project.property("appVersion").toString().trim()
    } else if (project.hasProperty("versionName")) {
        project.property("versionName").toString().trim()
    } else if (!System.getenv("APP_VERSION").isNullOrBlank()) {
        System.getenv("APP_VERSION").trim()
    } else if (!System.getenv("TAG").isNullOrBlank()) {
        System.getenv("TAG").trim()
    } else if (!System.getenv("GITHUB_REF_NAME").isNullOrBlank() && System.getenv("GITHUB_REF_NAME").startsWith("v")) {
        System.getenv("GITHUB_REF_NAME").trim()
    } else {
        val tag = getGitOutput("git", "describe", "--tags", "--abbrev=0")
        if (tag.isNotEmpty()) tag else "v1.0.0"
    }
}

val appVersionCode: Int = run {
    if (project.hasProperty("appVersionCode")) {
        project.property("appVersionCode").toString().toIntOrNull()?.let { return@run it }
    }
    System.getenv("APP_VERSION_CODE")?.toIntOrNull()?.let { return@run it }

    val match = Regex("""(\d+)\.(\d+)(?:\.(\d+))?""").find(appVersionName)
    if (match != null) {
        val major = match.groupValues[1].toIntOrNull() ?: 1
        val minor = match.groupValues[2].toIntOrNull() ?: 0
        val patch = match.groupValues.getOrNull(3)?.toIntOrNull() ?: 0
        major * 10000 + minor * 100 + patch
    } else {
        val count = getGitOutput("git", "rev-list", "--count", "HEAD").toIntOrNull()
        count ?: 1
    }
}

android {
    namespace = "com.autobox.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.autobox.app"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Background & Security
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.security.crypto)

    // Network & JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
