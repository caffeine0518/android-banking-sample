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

/**
 * 프로덕션(Hilt)과 디스패처 단위 테스트가 공유하는 조립.
 *
 * [KftcTranIds]는 여기서 한 번만 생성해 매퍼 전체가 공유한다 — api_tran_id 시퀀스가 엔드포인트 전역으로
 * 1씩 증가해야 하기 때문.
 */
internal fun kftcMockDispatcher(
    accountDao: MockAccountDao,
    transactionDao: MockTransactionDao,
    withdrawalService: KftcWithdrawalService,
    accountSeed: List<SeedAccount>,
    json: Json,
    responseDelayMillis: Long = 0,
): KftcMockDispatcher {
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
