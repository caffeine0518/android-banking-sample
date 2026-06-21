plugins {
    id("bank.android.library.compose")
}

android {
    namespace = "com.study.bank.core.ui.model"
}

dependencies {
    implementation(libs.androidx.compose.ui)
}
