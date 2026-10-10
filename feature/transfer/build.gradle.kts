plugins {
    id("bank.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.study.bank.feature.transfer"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.activity.compose)
}
