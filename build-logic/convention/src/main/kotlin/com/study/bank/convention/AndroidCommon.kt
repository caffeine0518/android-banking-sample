package com.study.bank.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 모든 Android 모듈에 공통인 AGP 설정.
 *
 * compileSdk 36.1 / minSdk 26 / Java 11 — 기존 모든 모듈의 build.gradle.kts에 복붙돼 있던 블록을 한곳에 모은다.
 * Kotlin 타깃은 AGP 9 built-in Kotlin이 compileOptions를 따라가므로 별도로 건드리지 않는다.
 */
internal fun Project.configureAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        // compileSdk만 블록 DSL(Function1) 멤버를 가진다 — 36.1 고정.
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        // 나머지는 AGP 9 CommonExtension에서 getter만 노출 → 프로퍼티 접근으로 설정.
        defaultConfig.minSdk = 26
        compileOptions.sourceCompatibility = JavaVersion.VERSION_11
        compileOptions.targetCompatibility = JavaVersion.VERSION_11
    }
}

/** Compose를 켜고 Compose BOM을 묶어준다. (compose 컴파일러 플러그인 적용은 호출 측에서.) */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.buildFeatures.compose = true
    dependencies {
        add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
    }
}
