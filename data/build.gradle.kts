plugins {
    id("bank.android.library")
}

android {
    namespace = "com.study.bank.data"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data.remote.kftc)
    implementation(projects.data.remote.fx)
    implementation(projects.data.local)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
