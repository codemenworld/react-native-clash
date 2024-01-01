import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.library")
    id("maven-publish")
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven("https://maven.kr328.app/releases")
    maven("https://jitpack.io")
}

android {
    ndkVersion = "23.0.7599858"

    compileSdk = 31

    defaultConfig {
        minSdk = 21

        consumerProguardFiles("consumer-rules.pro")

    }

    packagingOptions {
        resources {
            excludes.add("DebugProbesKt.bin")
        }
    }
}


val coroutine = "1.6.3"
val coreKtx = "1.8.0"
val activity = "1.5.0"
val fragment = "1.5.0"
val appcompat = "1.4.2"
val coordinator = "1.2.0"
val recyclerview = "1.2.1"
val material = "1.6.1"

dependencies {
    implementation("com.facebook.react:react-android:0.72.8")
    compileOnly("com.github.codemenworld:react-native-clash-hideapi:37cd4d58de")

    implementation("com.github.codemenworld:react-native-clash-core:1.0.0")
    implementation("com.github.codemenworld:react-native-clash-service:1.0.0")
    implementation("com.github.codemenworld:react-native-clash-design:1.0.0")
    implementation("com.github.codemenworld:react-native-clash-common:2c2b543ac5")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine")
    implementation("androidx.core:core-ktx:$coreKtx")
    implementation("androidx.activity:activity:$activity")
    implementation("androidx.fragment:fragment:$fragment")
    implementation("androidx.appcompat:appcompat:$appcompat")
    implementation("androidx.coordinatorlayout:coordinatorlayout:$coordinator")
    implementation("androidx.recyclerview:recyclerview:$recyclerview")
    implementation("com.google.android.material:material:$material")
}

tasks.getByName("clean", type = Delete::class) {
    delete(file("release"))
}

val geoFilesDownloadDir = "src/main/assets"

task("downloadGeoFiles") {

    val geoFilesUrls = mapOf(
        "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geoip.metadb" to "geoip.metadb",
        "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geosite.dat" to "geosite.dat",
        // "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/country.mmdb" to "country.mmdb",
    )

    doLast {
        geoFilesUrls.forEach { (downloadUrl, outputFileName) ->
            val url = URL(downloadUrl)
            val outputPath = file("$geoFilesDownloadDir/$outputFileName")
            outputPath.parentFile.mkdirs()
            url.openStream().use { input ->
                Files.copy(input, outputPath.toPath(), StandardCopyOption.REPLACE_EXISTING)
                println("$outputFileName downloaded to $outputPath")
            }
        }
    }
}

afterEvaluate {
    val downloadGeoFilesTask = tasks["downloadGeoFiles"]

    tasks.forEach {
        if (it.name.startsWith("assemble")) {
            it.dependsOn(downloadGeoFilesTask)
        }
    }
}

tasks.getByName("clean", type = Delete::class) {
    delete(file(geoFilesDownloadDir))
}

publishing {
  publications {
    register<MavenPublication>("release") {
      groupId = "com.github.codemenworld"
      artifactId = "react-native-clash"
      version = "1.0.0"

      afterEvaluate {
        from(components["release"])
      }
    }
  }
}
