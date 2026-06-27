package com.study.bank.data.remote.kftc.mock.dispatcher

// KFTC 오픈뱅킹 v2.0이 노출하는 URL 경로/쿼리키. 디스패처 라우팅의 키.
internal const val PATH_LIST_FINUSE = "/v2.0/account/list_finuse"
internal const val PATH_BALANCE_FIN_NUM = "/v2.0/account/balance/fin_num"
internal const val PATH_TRANSACTION_LIST_FIN_NUM = "/v2.0/account/transaction_list/fin_num"
internal const val PATH_TRANSFER_WITHDRAW_FIN_NUM = "/v2.0/transfer/withdraw/fin_num"
internal const val PATH_INQUIRY_REAL_NAME = "/v2.0/inquiry/real_name"
internal const val QUERY_FINTECH_USE_NUM = "fintech_use_num"
internal const val QUERY_BEFOR_INQUIRY_TRACE_INFO = "befor_inquiry_trace_info"

internal const val HTTP_OK = 200
internal const val HTTP_BAD_REQUEST = 400
internal const val HTTP_NOT_FOUND = 404
internal const val HTTP_SERVER_ERROR = 500
