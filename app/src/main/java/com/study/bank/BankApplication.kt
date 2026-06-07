package com.study.bank

import android.app.Application
import com.study.bank.data.remote.kftc.network.KftcRetrofit
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BankApplication : Application() {

    @Inject
    lateinit var retrofit: Lazy<KftcRetrofit>

    override fun onCreate() {
        super.onCreate()
        // Both MockWebServer.start() (socket bind + DNS) and MockWebServer.url() (reverse DNS via
        // getCanonicalHostName) hit network I/O. KftcRetrofit's <init> calls baseUrl(), so warming
        // up the Retrofit @Singleton on a worker thread forces the whole chain — mockServer.start
        // + baseUrl resolution — off the main thread.
        Thread { retrofit.get() }
            .apply {
                start()
                join()
            }
    }
}
