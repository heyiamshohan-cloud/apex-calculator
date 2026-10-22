import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/**
 * Release signing is driven by environment variables so that credentials never live in Git:
 *   APEX_KEYSTORE_FILE, APEX_KEYSTORE_PASSWORD, APEX_KEY_ALIAS, APEX_KEY_PASSWORD
 * When they are absent (for example on a fork without secrets) the release variant falls back
 * to the debug signing identity so that the produced APK is still installable and verifiable.
 * See docs/RELEASE.md for the production signing procedure.
 */
val releaseKeystore: String? = System.getenv("APEX_KEYSTORE_FILE")
val hasReleaseSigning: Boolean =
    !releaseKeystore.isNullOrBlank() && file(releaseKeystore).exists()

android {
    namespace = "com.shohankhan.apexcalculator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shohankhan.apexcalculator"
        minSdk = 26
        targetSdk = 35
        versionCode = (project.findProperty("apexVersionCode") as String?)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("apexVersionName") as String?) ?: "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                if (hasReleaseSigning) {
                    signingConfigs.create("release") {
                        storeFile = file(releaseKeystore!!)
                        storePassword = System.getenv("APEX_KEYSTORE_PASSWORD")
                        keyAlias = System.getenv("APEX_KEY_ALIAS")
                        keyPassword = System.getenv("APEX_KEY_PASSWORD")
                    }
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        warningsAsErrors = false
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStackTraces = true
        showExceptions = true
        showCauses = true
    }
}
