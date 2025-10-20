plugins {
    autowire(libs.plugins.android.application)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.kotlin.compose)
    autowire(libs.plugins.kotlin.ksp)
    autowire(libs.plugins.hilt.android)
}

android {
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
}

dependencies {


    implementation(androidx.core.core.ktx)
    implementation(androidx.startup.startup.runtime)
    implementation(androidx.tracing.tracing.ktx)
    implementation(androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(androidx.activity.activity.compose)
    implementation(platform(androidx.compose.compose.bom))
    implementation(androidx.appcompat.appcompat)
    implementation(androidx.navigation.navigation.compose)
    implementation(androidx.compose.material.material.icons.extended)
    implementation(androidx.compose.ui.ui)
    implementation(androidx.compose.ui.ui.graphics)

    implementation(androidx.compose.ui.ui.tooling.preview)
    implementation(androidx.compose.material3.material3)
    implementation(com.google.android.material.material)


    implementation(com.kizitonwose.calendar.compose)


    implementation(io.github.moriafly.salt.ui)
    implementation(com.google.code.gson.gson)
    implementation(androidx.core.core.splashscreen)
    implementation(io.github.billywei01.fastkv)

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
