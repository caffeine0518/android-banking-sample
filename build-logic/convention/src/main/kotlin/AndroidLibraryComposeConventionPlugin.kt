import com.android.build.api.dsl.LibraryExtension
import com.study.bank.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Compose를 쓰는 Android 라이브러리 — 베이스 + Compose 컴파일러 + Compose BOM. */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("bank.android.library")
            apply("org.jetbrains.kotlin.plugin.compose")
        }
        extensions.configure<LibraryExtension> {
            configureAndroidCompose(this)
        }
    }
}
