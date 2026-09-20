package com.study.bank.data.remote.kftc.mock.http

import com.study.bank.data.remote.kftc.mock.http.handler.AccountRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.InquiryRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.TransferRequestHandler
import com.study.bank.data.remote.kftc.mock.http.routing.Route
import com.study.bank.data.remote.kftc.mock.http.routing.routing

/**
 * mock 서버가 노출하는 KFTC 오픈뱅킹 v2.0 엔드포인트 전체.
 *
 * 경로·메서드·처리 주체가 한 줄에 모여 있어 [com.study.bank.data.remote.kftc.api.KftcApiService]의
 * Retrofit 선언과 나란히 대조할 수 있다. 쿼리/본문을 꺼내는 것까지만 여기서 하고, 그 뒤 판단은 핸들러 몫.
 */
internal fun kftcRoutes(
    account: AccountRequestHandler,
    transfer: TransferRequestHandler,
    inquiry: InquiryRequestHandler,
): List<Route> = routing {
    get(PATH_LIST_FINUSE) { account.list() }

    get(PATH_BALANCE_FIN_NUM) { request ->
        account.balance(request.query(QUERY_FINTECH_USE_NUM))
    }

    get(PATH_TRANSACTION_LIST_FIN_NUM) { request ->
        account.transactionList(
            fintechUseNum = request.query(QUERY_FINTECH_USE_NUM),
            beforInquiryTraceInfo = request.query(QUERY_BEFOR_INQUIRY_TRACE_INFO),
        )
    }

    post(PATH_TRANSFER_WITHDRAW_FIN_NUM) { request -> transfer.withdraw(request.body()) }

    post(PATH_INQUIRY_REAL_NAME) { request -> inquiry.realName(request.body()) }
}
