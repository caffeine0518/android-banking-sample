plugins {
    id("bank.android.feature")
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.study.bank.feature.home"
}

dependencies {
    // 공통 feature 스택은 bank.android.feature convention plugin이 제공. 아래는 이 모듈 고유 의존성.
    // Robolectric 렌더를 PNG로 캡처하는 스크린샷(골든) 테스트.
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
