plugins {
    id("bank.android.library")
    id("bank.android.hilt")
}

android {
    namespace = "com.study.bank.data.di"

    testOptions {
        // android.util.* 직접 호출(Log 등)이 JVM 단위 테스트에서 stub(0/false) 반환.
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data)
    implementation(projects.data.remote.kftc)
    implementation(projects.data.remote.fx)
    implementation(projects.data.local)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.room.runtime)

    // L3 데이터 E2E
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
}
