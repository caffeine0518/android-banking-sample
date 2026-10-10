package com.study.bank.data.remote.fx.api

import java.time.LocalDate

interface KeximApiService {

    suspend fun getRates(date: LocalDate): KeximRates
}
