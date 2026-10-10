plugins {
    id("bank.android.library")
    id("bank.android.hilt")
}

android {
    namespace = "com.study.bank.data.di"

    testOptions {
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
