import java.util.Properties

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

val releasePropertiesFile = rootProject.file("release/upload-key.properties")
val releaseProperties = Properties().apply {
    if (releasePropertiesFile.isFile) releasePropertiesFile.inputStream().use { load(it) }
}

android {
    namespace = "com.rainbow.priceperuse"
    compileSdk = 36
    defaultConfig { applicationId = "com.rainbow.priceperuse"; minSdk = 21; targetSdk = 36; versionCode = 10; versionName = "1.5"
        ndk { abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64") } }
    signingConfigs {
        if (releasePropertiesFile.isFile) {
            create("upload") {
                storeFile = rootProject.file(requireNotNull(releaseProperties.getProperty("storeFile")))
                storePassword = requireNotNull(releaseProperties.getProperty("storePassword"))
                keyAlias = requireNotNull(releaseProperties.getProperty("keyAlias"))
                keyPassword = requireNotNull(releaseProperties.getProperty("keyPassword"))
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (releasePropertiesFile.isFile) signingConfig = signingConfigs.getByName("upload")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
dependencies {
    constraints { implementation("org.jetbrains:annotations:26.1.0") }
    testImplementation("junit:junit:4.13.2")
}

// Release output must never silently fall back to an unsigned bundle.
gradle.taskGraph.whenReady {
    if (allTasks.any { it.project == project && it.name in setOf("bundleRelease", "assembleRelease") }) {
        check(releasePropertiesFile.isFile) { "Release signing is missing: run docs/codemagic/prepare_signing.py" }
    }
}
