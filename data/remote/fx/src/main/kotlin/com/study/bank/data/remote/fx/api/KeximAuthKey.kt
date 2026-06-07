package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holder for the KEXIM API authentication key.
 *
 * Production wiring goes through the [Inject] constructor which sources
 * [BuildConfig.KEXIM_API_KEY]. Tests instantiate the primary constructor directly
 * with an arbitrary value to exercise auth-failure paths.
 */
@Singleton
class KeximAuthKey(val value: String) {

    @Inject
    constructor() : this(BuildConfig.KEXIM_API_KEY)
}
