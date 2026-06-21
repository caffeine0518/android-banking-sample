plugins {
    id("bank.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.study.bank.data.local"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
