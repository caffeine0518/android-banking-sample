package com.study.bank.core.ui.testing

/**
 * UI/E2E 테스트가 **동적 리스트 항목을 안정적으로 지목**하기 위한 testTag 모음.
 *
 * 계좌명·잔액·마스킹번호 같은 표시 문자열은 서버(KFTC)가 주는 값이라 테스트 시점에 알 수 없고 바뀔 수
 * 있다. 그래서 그런 항목은 표시 텍스트가 아니라 **변하지 않는 식별자(AccountId = fintechUseNum)** 로
 * 찾는다.
 *
 * 프로덕션 컴포저블과 테스트가 **같은 포맷을 공유하는 단일 출처**다(여기 한 곳에서만 포맷이 정의됨).
 * 반면 버튼·제목·에러 문구처럼 앱이 통제하는 카피는 그대로 시맨틱 텍스트로 단언한다 — testTag를 달지 않는다.
 */
object BankTestTags {

    /** 홈/수취인 목록의 계좌 행. [accountId]는 AccountId.value(fintechUseNum). */
    fun accountItem(accountId: String): String = "account_item_$accountId"

    /** 계좌 상세 화면(특정 계좌의 상세에 도착·복귀했는지 식별). */
    fun accountDetail(accountId: String): String = "account_detail_$accountId"

    /** 계좌 상세의 거래내역 한 줄. [id]는 TransactionUi.id(거래 PK). */
    fun transactionItem(id: String): String = "detail_tx_item_$id"

    // --- 정적 화면/컨트롤 앵커 ---
    // 화면 도착·버튼 클릭처럼 "텍스트가 무엇인지는 중요치 않은" locator. 표시 문구나 문자열 리소싱 전략
    // (키 rename·i18n 교체 등)이 바뀌어도 안 깨지도록, copy가 아니라 이 안정 태그로 식별한다.

    // home
    const val SCREEN_HOME = "screen_home"
    const val HOME_TOTAL_BALANCE = "home_total_balance"
    const val HOME_REFRESH = "home_refresh"
    const val HOME_SNACKBAR = "home_snackbar"

    // account detail
    const val DETAIL_BACK = "detail_back"
    const val DETAIL_SEND = "detail_send"
    const val DETAIL_TX_LABEL = "detail_tx_label"
    const val DETAIL_TX_EMPTY = "detail_tx_empty"
    const val DETAIL_TX_ERROR = "detail_tx_error"
    const val DETAIL_TX_RETRY = "detail_tx_retry"
    const val DETAIL_TX_FOOTER_LOADING = "detail_tx_footer_loading"
    const val DETAIL_TX_FOOTER_RETRY = "detail_tx_footer_retry"

    // transfer
    const val SCREEN_RECIPIENT = "screen_recipient"
    const val SCREEN_AMOUNT = "screen_amount"
    const val AMOUNT_NEXT = "amount_next"
    const val SCREEN_CONFIRM = "screen_confirm"
    const val CONFIRM_SEND = "confirm_send"
    const val RESULT_SUCCESS = "result_success"
    const val RESULT_FAILURE = "result_failure"
    const val RESULT_CONFIRM = "result_confirm"
}
