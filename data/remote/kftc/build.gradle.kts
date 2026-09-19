plugins {
    id("bank.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("bank.android.hilt")
}

android {
    namespace = "com.study.bank.data.remote.kftc"

    testOptions {
        unitTests.isReturnDefaultValues = true
        // Room이 Android Context를 요구해 mock 상태 테스트가 Robolectric에서 실행된다.
        unitTests.isIncludeAndroidResources = true
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

    // mock 은행 상태(잔액·거래원장·멱등 기록)를 인메모리 SQLite 테이블로 보관한다.
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
