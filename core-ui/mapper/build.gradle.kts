plugins {
    id("bank.android.library")
}

android {
    namespace = "com.study.bank.core.ui.mapper"
}

dependencies {
    api(projects.domain)
    api(projects.coreUi.model)

    implementation(libs.javax.inject)
}
