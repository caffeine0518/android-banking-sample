// 모든 서브프로젝트·모듈에 공통으로 적용할 설정을 두는 최상위 빌드 파일.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.roborazzi) apply false
}

// README의 모듈 의존성 그래프(Mermaid)를 생성한다 — `./gradlew createModuleGraph`
moduleGraphConfig {
    readmePath.set("./README.md")
    heading.set("### 모듈 의존성")
    setStyleByModuleType.set(true)
    orientation.set(dev.iurysouza.modulegraph.Orientation.TOP_TO_BOTTOM)
    // :app 에서 도달하는 모듈만 그린다 — 테스트 전용 :app-e2e 는 모든 feature를 참조해 그래프를 복잡하게 한다
    rootModulesRegex.set(":app")
    // 경로별 subgraph는 :data 모듈과 :data:* 묶음의 id가 같아 렌더링이 깨지므로 전체 경로로 표시한다
    showFullPath.set(true)
}
