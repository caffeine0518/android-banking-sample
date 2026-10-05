package com.study.bank.data.remote.kftc.api

import java.net.HttpURLConnection
import retrofit2.HttpException

/** 이 bank_tran_id 가 다른 거래에 이미 체결돼 거절된 출금이체 응답(HTTP 409)인지. 이 요청은 체결되지 않았다. */
fun Throwable.isBankTranIdConflict(): Boolean =
    this is HttpException && code() == HttpURLConnection.HTTP_CONFLICT
