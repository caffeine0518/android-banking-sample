package com.study.bank.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** 메인 빌드와 공유하는 `libs` 버전 카탈로그 핸들(클래스 기반 플러그인에선 `libs.` 표기를 쓸 수 없다). */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
