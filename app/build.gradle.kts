plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.study.bank"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.study.bank"
        minSdk = 26
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ── Production: 모듈 그래프 ──────────────────────────────────────────
    implementation(projects.navigation)
    implementation(projects.dataDi)
    implementation(projects.coreUi.designsystem)

    // ── Production: DI(Hilt) ────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ── Production: Compose / AndroidX ──────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)

    // 테스트 의존성은 전용 모듈로 분리:
    //  · L3 데이터 E2E(JVM/Robolectric) → :integration-test
    //  · L3 UI E2E(계기) → :app-e2e (com.android.test, :app을 instrument)
}