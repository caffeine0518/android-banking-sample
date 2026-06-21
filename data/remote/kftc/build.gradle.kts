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

    // In-process KFTC mock — exposed to :data-di so the running app can boot it.
    implementation(libs.okhttp.mockwebserver)
    implementation(libs.okhttp.tls)

    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
