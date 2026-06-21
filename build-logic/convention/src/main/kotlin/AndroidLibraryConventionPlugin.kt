import com.android.build.api.dsl.LibraryExtension
import com.study.bank.convention.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Android 라이브러리 베이스(compileSdk/minSdk/Java 11). namespace는 각 모듈이 직접 지정한다. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureAndroid(this)
        }
    }
}
