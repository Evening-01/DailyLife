import com.android.build.api.dsl.ApplicationExtension
import org.w3c.dom.Element
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

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

    val storeFile = propertyOrEnv("SIGNING_STORE_FILE")
    val storePassword = propertyOrEnv("SIGNING_STORE_PASSWORD")
    val keyAlias = propertyOrEnv("SIGNING_KEY_ALIAS")
    val keyPassword = propertyOrEnv("SIGNING_KEY_PASSWORD")

    return if (storeFile != null && storePassword != null && keyAlias != null && keyPassword != null) {
        SigningCredentials(storeFile, storePassword, keyAlias, keyPassword)
    } else {
        null
    }
}

fun Project.readDefaultAppName(): String {
    val stringsFile = file("src/main/res/values/strings.xml")
    if (!stringsFile.exists()) {
        return findProperty("project.app.packageName")?.toString() ?: name
    }
    return runCatching {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(stringsFile)
        document.documentElement.normalize()
        val nodes = document.getElementsByTagName("string")
        (0 until nodes.length)
            .mapNotNull { nodes.item(it) as? Element }
            .firstOrNull { it.getAttribute("name") == "app_name" }
            ?.textContent
    }.getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: findProperty("project.app.packageName")?.toString()
        ?: name
}

fun sanitizeFileNameCandidate(input: String): String {
    return input
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .replace(Regex("\\s+"), "_")
        .takeIf { it.isNotBlank() }
        ?: "artifact"
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

val androidExtension = extensions.getByType<ApplicationExtension>()
val appNameValue = readDefaultAppName()
val versionNameValue = androidExtension.defaultConfig.versionName
    ?: findProperty("project.app.versionName")?.toString()
    ?: "0.0.0"
val versionCodeValue = androidExtension.defaultConfig.versionCode
    ?.toString()
    ?: findProperty("project.app.versionCode")?.toString()
    ?: "0"

tasks.register("printAppName") {
    group = "ci"
    description = "Prints the application display name for CI workflows."
    doLast {
        println(appNameValue)
    }
}

tasks.register("printVersionName") {
    group = "ci"
    description = "Prints the versionName for CI workflows."
    doLast {
        println(versionNameValue)
    }
}

tasks.register("renameReleaseBundle") {
    group = "distribution"
    description = "Renames release APK/AAB artifacts to a unified naming convention."
    dependsOn("assembleRelease", "bundleRelease")
    doLast {
        val baseName = sanitizeFileNameCandidate(
            "$appNameValue-$versionNameValue($versionCodeValue)"
        )

        fun renameArtifacts(directory: File, extension: String) {
            if (!directory.exists()) return
            val files = directory.listFiles { file -> file.extension.equals(extension, ignoreCase = true) }
                ?.sortedBy { it.name }
                ?: return
            files.forEachIndexed { index, file ->
                val suffix = if (index == 0) "" else "-${index + 1}"
                val target = File(directory, "$baseName$suffix.$extension")
                if (file != target) {
                    if (target.exists()) {
                        target.delete()
                    }
                    file.renameTo(target)
                }
            }
        }

        val outputsDir = layout.buildDirectory.dir("outputs").get().asFile
        renameArtifacts(outputsDir.resolve("apk/release"), "apk")
        renameArtifacts(outputsDir.resolve("bundle/release"), "aab")
    }
}
