plugins {
    id("bank.android.feature")
    // NavKey 타입세이프 라우트의 @Serializable용.
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
