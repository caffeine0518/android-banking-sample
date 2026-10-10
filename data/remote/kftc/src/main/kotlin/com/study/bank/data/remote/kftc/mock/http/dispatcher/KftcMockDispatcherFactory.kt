package com.study.bank.data.remote.kftc.mock.http.dispatcher

import com.study.bank.data.remote.kftc.mock.http.handler.AccountRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.InquiryRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.TransferRequestHandler
import com.study.bank.data.remote.kftc.mock.http.response.KftcTranIds
import com.study.bank.data.remote.kftc.mock.http.routing.kftcRoutes
import com.study.bank.data.remote.kftc.mock.mapper.AccountResponseMapper
import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper
import com.study.bank.data.remote.kftc.mock.mapper.InquiryResponseMapper
import com.study.bank.data.remote.kftc.mock.mapper.TransferResponseMapper
import com.study.bank.data.remote.kftc.mock.seed.KftcRecipientSeed
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import kotlinx.serialization.json.Json

internal fun kftcMockDispatcher(
    accountDao: MockAccountDao,
    transactionDao: MockTransactionDao,
    withdrawalService: KftcWithdrawalService,
    accountSeed: List<SeedAccount>,
    json: Json,
    responseDelayMillis: Long = 0,
): KftcMockDispatcher {
    // api_tran_id가 엔드포인트와 관계없이 1씩 증가하도록 모든 매퍼가 공유한다.
    val tranIds = KftcTranIds()
    val errors = ErrorResponseMapper(tranIds)
    return KftcMockDispatcher(
        routes = kftcRoutes(
            account = AccountRequestHandler(
                accountDao,
                transactionDao,
                AccountResponseMapper(tranIds),
                errors,
            ),
            transfer = TransferRequestHandler(
                withdrawalService,
                TransferResponseMapper(tranIds),
                errors,
                json,
                responseDelayMillis = responseDelayMillis,
            ),
            inquiry = InquiryRequestHandler(
                KftcRecipientSeed.directory(accountSeed),
                InquiryResponseMapper(tranIds),
                errors,
                json,
            ),
        ),
        errors = errors,
    )
}
