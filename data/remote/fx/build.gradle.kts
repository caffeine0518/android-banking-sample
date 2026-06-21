plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.study.bank.data.remote.fx"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
        // KEXIM 환율 API 인증키. 오픈소스로 하드코딩 — 일 1,000회 한도의 무료 키
        buildConfigField("String", "KEXIM_API_KEY", "\"0FSe6rlaniiSnwd5nR55bCMqp5ZyVjx3\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.javax.inject)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
