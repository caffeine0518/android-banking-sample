import com.android.build.api.dsl.ApplicationExtension
import com.study.bank.convention.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Android 애플리케이션 베이스. applicationId/version/buildTypes 등 앱 고유 설정은 `:app`이 직접 지정한다. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            configureAndroid(this)
            defaultConfig.targetSdk = 36
        }
    }
}
