package com.study.bank.data.remote.kftc.mock.http.routing

import com.study.bank.data.remote.kftc.mock.http.handler.AccountRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.InquiryRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.TransferRequestHandler

/**
 * mock 서버가 제공하는 KFTC 오픈뱅킹 v2.0 엔드포인트 목록.
 *
 * 경로·메서드·핸들러가 엔드포인트마다 한 줄로 정리돼 있어 [com.study.bank.data.remote.kftc.api.KftcApiService]의
 * Retrofit 선언과 대조할 수 있다. 여기서는 쿼리와 본문 추출까지만 하고, 이후 처리는 핸들러가 담당한다.
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
