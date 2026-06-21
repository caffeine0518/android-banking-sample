import com.android.build.api.dsl.ApplicationExtension
import com.study.bank.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Compose를 쓰는 Android 애플리케이션(`:app`) — 앱 베이스 + Compose 컴파일러 + Compose BOM. */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("bank.android.application")
            apply("org.jetbrains.kotlin.plugin.compose")
        }
        extensions.configure<ApplicationExtension> {
            configureAndroidCompose(this)
        }
    }
}
