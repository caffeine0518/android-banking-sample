plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

/**
 * L3 UI E2E 전용 모듈(`com.android.test`).
 *
 * 별도 test APK로 빌드되어 [targetProjectPath]=`:app`의 APK를 instrument한다. 실제 MainActivity →
 * BankNavHost → Hilt 그래프 → KFTC mock 서버까지 앱 전체를 디바이스/에뮬레이터에서 부팅해 검증한다.
 * androidTest 의존성(compose-ui-test / espresso / hilt-testing)이 :app이 아닌 이 모듈에 모인다.
 *
 * com.android.test 모듈은 **main 소스셋이 곧 테스트 코드**다(별도 androidTest 없음).
 * 실행: ./gradlew :app-e2e:connectedDebugAndroidTest (디바이스/에뮬레이터 필요)
 */
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
        // @HiltAndroidTest가 HiltTestApplication 위에서 돌도록 커스텀 러너 사용.
        testInstrumentationRunner = "com.study.bank.HiltTestRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // instrument 대상 앱.
    targetProjectPath = ":app"
}

dependencies {
    // :app은 implementation으로 하위 모듈을 가리므로 그 타입들은 여기서 다시 노출해야 한다.
    implementation(projects.dataDi)        // NetworkFaultController(장애 주입 seam) + DispatchersModule 교체
    implementation(projects.domain)        // DispatcherProvider(E2E 디스패처 교체)
    implementation(projects.coreUi.model)  // BankTestTags(동적 리스트 항목을 id 기반 testTag로 지목)
    implementation(projects.data.remote.kftc) // KftcSeedAccountIds(시드 계좌 id 단일 출처)
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
