plugins {
    id("bank.android.feature")
    // 타입세이프 네비게이션의 @Serializable 라우트 클래스용.
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.study.bank.feature.transfer"
}

dependencies {
    // 공통 feature 스택은 bank.android.feature convention plugin이 제공. 아래는 이 모듈 고유 의존성.
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui.graphics)
    // 송금 결과 화면에서 시스템 뒤로가기를 가로채는 BackHandler용(navigation-compose 경유로도 오지만 명시).
    implementation(libs.androidx.activity.compose)
}
