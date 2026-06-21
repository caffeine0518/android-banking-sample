plugins {
    id("bank.android.application.compose")
    id("bank.android.hilt")
}

android {
    namespace = "com.study.bank"

    defaultConfig {
        applicationId = "com.study.bank"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // ── Production: 모듈 그래프 ──────────────────────────────────────────
    implementation(projects.navigation)
    implementation(projects.dataDi)
    implementation(projects.coreUi.designsystem)

    // ── Production: Compose / AndroidX ──────────────────────────────────
    implementation(libs.androidx.activity.compose)

    // Hilt(DI), Compose 컴파일러/BOM, compileSdk/minSdk/Java 11은
    // bank.android.application.compose + bank.android.hilt convention plugin이 제공.
    //
    // 테스트 의존성은 전용 모듈로 분리:
    //  · L3 데이터 E2E(JVM/Robolectric) → :data-di src/test
    //  · L3 UI E2E(계기) → :app-e2e (com.android.test, :app을 instrument)
}
