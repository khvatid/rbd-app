import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("org.jetbrains.compose")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    val decompose_version: String by project
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.litote.kmongo:kmongo-coroutine:4.7.2")

                implementation ("org.litote.kmongo:kmongo-id:4.7.2")
                implementation ("com.google.code.gson:gson:2.10")
                implementation("com.arkivanov.decompose:decompose:$decompose_version")
                implementation("com.arkivanov.decompose:extensions-compose-jetbrains:$decompose_version")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "rbd-app"
            packageVersion = "1.0.0"
        }
    }
}
