plugins {
    id("bank.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.study.bank.data.remote.kftc"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.core)

    // in-process KFTC mock — 실행 중인 앱이 부팅할 수 있도록 :data-di에 노출한다.
    implementation(libs.okhttp.mockwebserver)
    implementation(libs.okhttp.tls)

    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
