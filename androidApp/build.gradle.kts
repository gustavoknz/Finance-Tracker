import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.detekt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun getSigningProperty(key: String): String? {
    return (findProperty(key) as? String)
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv(key)?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(key)?.takeIf { it.isNotBlank() }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "dev.gustavo.finance"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.gustavo.finance"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = getSigningProperty("RELEASE_STORE_FILE")
            val storePasswordProp = getSigningProperty("RELEASE_STORE_PASSWORD")
            val keyAliasProp = getSigningProperty("RELEASE_KEY_ALIAS")
            val keyPasswordProp = getSigningProperty("RELEASE_KEY_PASSWORD")

            if ((storeFilePath != null) &&
                !storePasswordProp.isNullOrBlank() &&
                !keyAliasProp.isNullOrBlank() &&
                !keyPasswordProp.isNullOrBlank()
            ) {
                val keystoreFile = file(storeFilePath)
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = storePasswordProp
                    keyAlias = keyAliasProp
                    keyPassword = keyPasswordProp
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if ((releaseSigning.storeFile != null) && (releaseSigning.storeFile?.exists() == true)) {
                releaseSigning
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    debugImplementation(libs.compose.test.manifest)
    androidTestImplementation(libs.compose.test.junit4)
}
