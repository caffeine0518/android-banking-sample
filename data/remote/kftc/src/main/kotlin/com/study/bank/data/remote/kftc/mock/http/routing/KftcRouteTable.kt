package com.study.bank.data.remote.kftc.mock.http.routing

import com.study.bank.data.remote.kftc.mock.http.handler.AccountRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.InquiryRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.TransferRequestHandler

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
