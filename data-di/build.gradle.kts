plugins {
    id("bank.android.library")
    id("bank.android.hilt")
}

android {
    namespace = "com.study.bank.data.di"

    testOptions {
        // android.util.* 직접 호출(Log 등)이 JVM 단위 테스트에서 stub(0/false) 반환.
        unitTests.isReturnDefaultValues = true
        // Robolectric이 머지된 안드로이드 리소스/매니페스트를 읽을 수 있게 한다.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data)
    implementation(projects.data.remote.kftc)
    implementation(projects.data.remote.fx)
    implementation(projects.data.local)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.room.runtime)

    // L3 데이터 E2E: 이 모듈이 조립하는 실제 Hilt 그래프(KFTC mock + Room SSOT + 레포)를
    // Robolectric(JVM)에서 부팅해 끝까지 흐르는지 검증한다. domain 등 피검 타입은 main의 implementation으로 노출됨.
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
}
