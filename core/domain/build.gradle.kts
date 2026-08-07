plugins {
    alias(libs.plugins.android.library)
    id("jacoco")
}

android {
    namespace = "com.omie.desafio.core.domain"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
    }
}
