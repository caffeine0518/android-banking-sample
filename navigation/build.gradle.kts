plugins {
    id("bank.android.library.compose")
}

android {
    namespace = "com.study.bank.navigation"
}

dependencies {
    implementation(projects.domain)
    implementation(projects.feature.home)
    implementation(projects.feature.account)
    implementation(projects.feature.transfer)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
