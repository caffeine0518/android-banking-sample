plugins {
    id("bank.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.study.bank.data.remote.fx"

    defaultConfig {
        // KEXIM 환율 API 인증키. 오픈소스로 하드코딩 — 일 1,000회 한도의 무료 키
        buildConfigField("String", "KEXIM_API_KEY", "\"0FSe6rlaniiSnwd5nR55bCMqp5ZyVjx3\"")
    }

    buildFeatures {
        buildConfig = true
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
