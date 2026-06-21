package com.study.bank

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * 계측 테스트에서 [BankApplication] 대신 [HiltTestApplication]을 띄우는 러너.
 *
 * @HiltAndroidTest가 테스트 전용 Hilt 컴포넌트를 주입하려면 Application이 HiltTestApplication이어야 한다.
 * app/build.gradle.kts의 testInstrumentationRunner가 이 클래스를 가리킨다.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
