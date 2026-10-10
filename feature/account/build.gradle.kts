plugins {
    id("bank.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.study.bank.feature.account"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    testImplementation(libs.androidx.paging.testing)
}
