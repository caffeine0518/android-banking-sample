// build-logic — convention plugin을 담는 독립 컴포지트 빌드.
// 루트 settings.gradle.kts의 includeBuild("build-logic")로 메인 빌드에 합류한다.
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // 메인 빌드와 동일한 버전 카탈로그를 재사용한다(별도 빌드라 명시 등록 필요).
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
