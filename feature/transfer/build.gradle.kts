plugins {
    id("bank.android.feature")
}

android {
    namespace = "com.study.bank.feature.transfer"
}

dependencies {
    // 공통 feature 스택은 bank.android.feature convention plugin이 제공. 아래는 이 모듈 고유 의존성.
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui.graphics)
}
