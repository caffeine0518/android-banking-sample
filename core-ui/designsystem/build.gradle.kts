plugins {
    id("bank.android.library.compose")
}

android {
    namespace = "com.study.bank.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
}
