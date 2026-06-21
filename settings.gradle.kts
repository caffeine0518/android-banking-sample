pluginManagement {
    // convention plugin을 제공하는 컴포지트 빌드. 모듈이 bank.* 플러그인을 적용할 수 있게 한다.
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "bank"
include(":app")
include(":domain")
include(":data")
include(":data:remote:kftc")
include(":data:remote:fx")
include(":data:local")
include(":data-di")
include(":core-ui:mvi")
include(":core-ui:designsystem")
include(":core-ui:model")
include(":core-ui:mapper")
include(":feature:account")
include(":feature:transfer")
include(":feature:home")
include(":navigation")
// L3 UI E2E(계기 테스트) 전용 모듈 — :app을 instrument하며 androidTest 의존성을 여기 가둔다.
// (데이터 레이어 E2E는 그래프 집결점인 :data-di의 src/test가 소유)
include(":app-e2e")
