import java.util.Properties

data class SigningCredentials(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

fun Project.loadReleaseSigning(): SigningCredentials? {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(localProperties::load)
    }

    fun propertyOrEnv(name: String): String? =
        localProperties.getProperty(name)?.takeIf { it.isNotBlank() } ?: System.getenv(name)?.takeIf { it.isNotBlank() }

    val storeFile = propertyOrEnv("dailylife.signing.storeFile")
    val storePassword = propertyOrEnv("dailylife.signing.storePassword")
    val keyAlias = propertyOrEnv("dailylife.signing.keyAlias")
    val keyPassword = propertyOrEnv("dailylife.signing.keyPassword")

    return if (storeFile != null && storePassword != null && keyAlias != null && keyPassword != null) {
        SigningCredentials(storeFile, storePassword, keyAlias, keyPassword)
    } else {
        null
    }
}

plugins {
    autowire(libs.plugins.android.application)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.kotlin.compose)
    autowire(libs.plugins.kotlin.ksp)
    autowire(libs.plugins.hilt.android)
}

android {
    val releaseSigning = project.loadReleaseSigning()
    namespace = property.project.app.packageName
    compileSdk = property.project.android.compileSdk

    defaultConfig {
        applicationId = property.project.app.packageName
        minSdk = property.project.android.minSdk
        targetSdk = property.project.android.targetSdk
        versionName = property.project.app.versionName
        versionCode = property.project.app.versionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        releaseSigning?.let { credentials ->
            create("release") {
                storeFile = file(credentials.storeFile)
                storePassword = credentials.storePassword
                keyAlias = credentials.keyAlias
                keyPassword = credentials.keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (releaseSigning != null) {
                signingConfig = signingConfigs.getByName("release")
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
}

dependencies {


    implementation(androidx.core.core.ktx)
    implementation(androidx.startup.startup.runtime)
    implementation(androidx.tracing.tracing.ktx)
    implementation(androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(androidx.lifecycle.lifecycle.process)
    implementation(androidx.activity.activity.compose)
    implementation(platform(androidx.compose.compose.bom))
    implementation(androidx.appcompat.appcompat)
    implementation(androidx.navigation.navigation.compose)
    implementation(androidx.biometric.biometric)
    implementation(androidx.compose.material.material.icons.extended)
    implementation(androidx.compose.ui.ui)
    implementation(androidx.compose.ui.ui.graphics)

    implementation(androidx.compose.ui.ui.tooling.preview)
    implementation(androidx.compose.material3.material3)
    implementation(com.google.android.material.material)
    implementation(com.materialkolor.material.kolor)
    implementation(io.github.moriafly.salt.ui)
    implementation(com.google.code.gson.gson)
    implementation(androidx.core.core.splashscreen)
    implementation(io.github.billywei01.fastkv)
    implementation(androidx.glance.glance.appwidget)
    implementation(androidx.glance.glance.material3)

    implementation(com.google.dagger.hilt.android)
    implementation(androidx.hilt.hilt.navigation.compose)
    ksp(com.google.dagger.hilt.android.compiler)

    implementation(androidx.room.room.runtime)
    implementation(androidx.room.room.ktx)
    ksp(androidx.room.room.compiler)

    testImplementation(junit.junit)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.espresso.espresso.core)
    androidTestImplementation(platform(androidx.compose.compose.bom))
    androidTestImplementation(androidx.compose.ui.ui.test.junit4)
    debugImplementation(androidx.compose.ui.ui.tooling)
    debugImplementation(androidx.compose.ui.ui.test.manifest)
}
