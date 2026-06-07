package com.study.bank.data.remote.kftc.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter

@Singleton
class NetworkJson @Inject constructor() {

    val value: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    val converterFactory: Converter.Factory =
        value.asConverterFactory("application/json".toMediaType())
}
