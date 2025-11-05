
import com.android.build.api.dsl.ApplicationExtension
import org.w3c.dom.Element
import java.io.File
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

data class SigningCredentials(
    val storeFile: File,
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

    val resolvedStoreFile = mutableListOf<File>().apply {
        storeFile?.let { add(file(it)) }
        add(file("keystore.jks"))
        add(file("release/keystore.jks"))
        add(rootProject.file("keystore.jks"))
        add(rootProject.file("app/keystore.jks"))
    }.firstOrNull { it.exists() && it.isFile }

    return if (resolvedStoreFile != null && storePassword != null && keyAlias != null && keyPassword != null) {
        SigningCredentials(resolvedStoreFile, storePassword, keyAlias, keyPassword)
    } else {
        if (System.getenv().containsKey("GITHUB_ACTIONS") && resolvedStoreFile == null) {
            logger.warn("[Signing] Release signing credentials detected but keystore file is missing; release artifact will be unsigned.")
        }
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

fun Project.computeGitCommitCount(): Int? {
    return runCatching {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .directory(rootProject.projectDir)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use { reader ->
            reader.readText().trim()
        }.takeIf { it.isNotBlank() }?.toInt().also {
            process.waitFor()
        }
    }.getOrNull()
}

plugins {
    autowire(libs.plugins.android.application)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.kotlin.compose)
    autowire(libs.plugins.kotlin.ksp)
    autowire(libs.plugins.hilt.android)
}

val gitCommitCount = project.computeGitCommitCount()

android {
    val releaseSigning = project.loadReleaseSigning()
    namespace = property.project.app.packageName
    compileSdk = property.project.android.compileSdk

    defaultConfig {
        applicationId = property.project.app.packageName
        minSdk = property.project.android.minSdk
        targetSdk = property.project.android.targetSdk
        versionName = property.project.app.versionName
        val fallbackVersionCode = property.project.app.versionCode
        versionCode = gitCommitCount ?: fallbackVersionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        releaseSigning?.let { credentials ->
            create("release") {
                storeFile = credentials.storeFile
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
val commitCountValue = gitCommitCount?.takeIf { it > 0 }?.toString()
val versionCodeValue = androidExtension.defaultConfig.versionCode
    ?.toString()
    ?: commitCountValue
    ?: findProperty("project.app.versionCode")?.toString()
    ?: "0"
val versionSuffix = commitCountValue ?: versionCodeValue

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

tasks.register("printCommitCount") {
    group = "ci"
    description = "Prints the git commit count used as versionCode for CI workflows."
    doLast {
        println(versionSuffix)
    }
}

tasks.register("renameReleaseBundle") {
    group = "distribution"
    description = "Renames release APK/AAB artifacts to a unified naming convention."
    dependsOn("assembleRelease", "bundleRelease")
    doLast {
        val baseName = sanitizeFileNameCandidate(
            "$appNameValue-$versionNameValue($versionSuffix)"
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
