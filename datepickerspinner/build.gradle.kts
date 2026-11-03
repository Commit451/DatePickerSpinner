import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.publish)
}

group = findProperty("GROUP") as String
version = findProperty("VERSION_NAME") as String

kotlin {
    androidLibrary {
        namespace = "com.commit451.datepickerspinner"
        compileSdk = 37
        minSdk = 21

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    js {
        browser()
        // Compose UI's Skiko runtime is only loaded when the JS tests run through a
        // webpack bundle, which requires an executable binary (CMP-4906).
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // Same requirement as js: the Skiko runtime only loads from a
        // webpack-bundled executable binary (CMP-4906).
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeUi)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            // api: LocalDate appears in DatePickerSpinner's public signature.
            api(libs.kotlinxDatetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
    publishToMavenCentral(automaticRelease = true)
    if (System.getenv("RELEASE_SIGNING_ENABLED") == "true") {
        signAllPublications()
    }
}
