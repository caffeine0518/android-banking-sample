plugins {
    id("bank.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.study.bank.data.remote.fx"

    defaultConfig {
        // 일 1,000회 한도의 무료 키라 저장소에 그대로 둔다.
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
