plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// L3 UI E2E. 실행: ./gradlew :app-e2e:connectedDebugAndroidTest
android {
    namespace = "com.study.bank.e2e"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        testInstrumentationRunner = "com.study.bank.e2e.harness.HiltTestRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    targetProjectPath = ":app"
}

dependencies {
    // :app은 implementation으로 하위 모듈을 가리므로 그 타입들은 여기서 다시 노출해야 한다.
    implementation(projects.dataDi)        // Hilt 테스트 루트가 data 계층 모듈을 집계하려면 필요
    implementation(projects.domain)        // Currency(통화 의도로 시드 계좌 선택)
    implementation(projects.coreUi.model)  // BankTestTags(동적 리스트 항목을 id 기반 testTag로 지목)
    implementation(projects.data.remote.kftc) // KftcSeedAccountIds(시드 id 단일 출처) + KftcMockServer(연결 차단)
    // @TestInstallIn이 테스트 Hilt 루트를 새로 생성하므로, E2E에 등장하는 @HiltViewModel 바인딩(home/account/
    // transfer)이 이 모듈 클래스패스에서 집계되도록 feature 모듈을 명시 의존한다(home.R도 여기서 옴).
    implementation(projects.feature.home)
    implementation(projects.feature.account)
    implementation(projects.feature.transfer)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.test.junit4)
    implementation(libs.androidx.test.runner)

    implementation(libs.hilt.android)
    implementation(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
