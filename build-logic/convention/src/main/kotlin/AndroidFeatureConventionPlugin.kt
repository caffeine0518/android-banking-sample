import com.android.build.api.dsl.LibraryExtension
import com.study.bank.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * feature 모듈(`:feature:account` / `:feature:home` / `:feature:transfer`) 공통 묶음.
 *
 * Compose 라이브러리 + Hilt + 공통 모듈 그래프(mvi/model/mapper) + Compose/Lifecycle/Navigation 의존성 +
 * Robolectric 기반 JVM Compose UI 테스트 스택까지 한 번에 적용한다. 세 모듈에 거의 동일하게 복붙돼 있던 블록.
 * material-icons / ui-graphics / roborazzi 같은 모듈별 차이는 각 모듈에서 추가한다.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("bank.android.library.compose")
            apply("bank.android.hilt")
        }

        extensions.configure<LibraryExtension> {
            testOptions {
                // ViewModel이 android.util.Log를 직접 호출 → JVM 단위 테스트에서 stub(0/false) 반환.
                unitTests.isReturnDefaultValues = true
                // Robolectric이 stringResource 등 머지된 안드로이드 리소스를 읽을 수 있게 한다.
                unitTests.isIncludeAndroidResources = true
            }
        }

        dependencies {
            add("implementation", project(":domain"))
            add("implementation", project(":core-ui:mvi"))
            add("implementation", project(":core-ui:model"))
            add("implementation", project(":core-ui:mapper"))

            add("implementation", libs.findLibrary("androidx-compose-material3").get())
            add("implementation", libs.findLibrary("androidx-compose-ui").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("androidx-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())

            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            add("testImplementation", libs.findLibrary("turbine").get())
            // Compose UI 테스트를 JVM(src/test)에서 Robolectric으로 구동.
            add("testImplementation", platform(libs.findLibrary("androidx-compose-bom").get()))
            add("testImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
            add("testImplementation", libs.findLibrary("robolectric").get())
            // createComposeRule()가 띄우는 호스트 ComponentActivity 제공.
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
        }
    }
}
