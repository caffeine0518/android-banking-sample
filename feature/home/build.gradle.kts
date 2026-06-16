plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.study.bank.feature.home"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        // ViewModel이 android.util.Log를 직접 호출 → JVM 단위 테스트에서 stub(0/false) 반환.
        unitTests.isReturnDefaultValues = true
        // Robolectric이 stringResource 등 머지된 안드로이드 리소스를 읽을 수 있게 한다.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.coreUi.mvi)
    implementation(projects.coreUi.model)
    implementation(projects.coreUi.mapper)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Compose UI 테스트를 JVM(src/test)에서 Robolectric으로 구동.
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    // createComposeRule()가 띄우는 호스트 ComponentActivity 제공.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
