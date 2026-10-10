package com.study.bank.core.ui.testing

/** 계좌명·잔액은 KFTC가 주는 값이고 문구는 리소스 전략에 따라 바뀌므로, 테스트는 표시 문자열 대신 이 태그로 노드를 식별한다. */
object BankTestTags {

    /** 홈/수취인 목록의 계좌 행. [accountId]는 AccountId.value(fintechUseNum). */
    fun accountItem(accountId: String): String = "account_item_$accountId"

    fun accountDetail(accountId: String): String = "account_detail_$accountId"

    /** 계좌 상세의 거래내역 한 줄. [id]는 TransactionUi.id(거래 PK). */
    fun transactionItem(id: String): String = "detail_tx_item_$id"

    const val SCREEN_HOME = "screen_home"
    const val HOME_TOTAL_BALANCE = "home_total_balance"
    const val HOME_REFRESH = "home_refresh"
    const val HOME_SNACKBAR = "home_snackbar"

    const val DETAIL_BACK = "detail_back"
    const val DETAIL_SEND = "detail_send"
    const val DETAIL_TX_LABEL = "detail_tx_label"
    const val DETAIL_TX_EMPTY = "detail_tx_empty"
    const val DETAIL_TX_ERROR = "detail_tx_error"
    const val DETAIL_TX_RETRY = "detail_tx_retry"
    const val DETAIL_TX_FOOTER_LOADING = "detail_tx_footer_loading"
    const val DETAIL_TX_FOOTER_RETRY = "detail_tx_footer_retry"

    const val SCREEN_RECIPIENT = "screen_recipient"
    const val SCREEN_AMOUNT = "screen_amount"
    const val AMOUNT_NEXT = "amount_next"
    const val SCREEN_CONFIRM = "screen_confirm"
    const val CONFIRM_SEND = "confirm_send"
    const val RESULT_SUCCESS = "result_success"
    const val RESULT_FAILURE = "result_failure"
    const val RESULT_RETRY = "result_retry"
    const val RESULT_CONFIRM = "result_confirm"
}
