import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.Properties

abstract class GenerateGeminiConfigTask : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val escapedApiKey = apiKey.get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val outputFile = outputDirectory.file(
            "com/example/fitvive1/network/GeminiConfig.kt"
        ).get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """package com.example.fitvive1.network

internal object GeminiConfig {
    const val API_KEY = "$escapedApiKey"
}
"""
        )
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.kotlinSerialization)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}
val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY", "").trim()
val generatedGeminiConfigDir = layout.buildDirectory.dir("generated/geminiConfig/commonMain/kotlin")
val generateGeminiConfig by tasks.registering(GenerateGeminiConfigTask::class) {
    apiKey.set(geminiApiKey)
    outputDirectory.set(generatedGeminiConfigDir)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.firebase.firestore)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
        commonMain {
            kotlin.srcDir(generateGeminiConfig)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.fitvive1"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.fitvive1"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("FitviveDatabase") {
            packageName.set("com.example.fitvive1.database")
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
