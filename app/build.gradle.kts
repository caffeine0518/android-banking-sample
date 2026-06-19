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

        // @HiltAndroidTest가 HiltTestApplication 위에서 돌도록 커스텀 러너 사용.
        testInstrumentationRunner = "com.study.bank.HiltTestRunner"
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

    testOptions {
        // android.util.* 직접 호출(Log 등)이 JVM 단위 테스트에서 stub(0/false) 반환.
        unitTests.isReturnDefaultValues = true
        // Robolectric이 머지된 안드로이드 리소스/매니페스트를 읽을 수 있게 한다.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.navigation)
    implementation(projects.dataDi)
    implementation(projects.coreUi.designsystem)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    // 데이터 레이어 E2E(L3): 실제 Hilt 그래프(KFTC mock + Room SSOT + 레포)를 JVM(Robolectric)에서 런타임 통합 검증.
    testImplementation(projects.domain)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    // 실제 MainActivity+BankNavHost+Hilt 그래프를 띄우는 E2E용 Hilt 테스트.
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}