plugins {
    id("bank.android.application.compose")
    id("bank.android.hilt")
}

android {
    namespace = "com.study.bank"

    defaultConfig {
        applicationId = "com.study.bank"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.navigation)
    implementation(projects.dataDi)
    implementation(projects.coreUi.designsystem)

    implementation(libs.androidx.activity.compose)
}
