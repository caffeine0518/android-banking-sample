plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.study.bank.core.ui.mapper"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(projects.domain)
    api(projects.coreUi.model)

    implementation(libs.javax.inject)
}
