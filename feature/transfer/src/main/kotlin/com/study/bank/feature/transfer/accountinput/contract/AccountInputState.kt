package com.study.bank.feature.transfer.accountinput.contract

import com.study.bank.domain.model.BankCode

/**
 * 계좌번호 입력 화면 상태.
 *
 * [accountNumber]는 숫자만 담는 원시 입력(키패드), [selectedBank]는 송금 가능한 은행 중 선택값.
 * 확인 시 (계좌번호, 은행)으로 실명조회를 돌려 [error]로 결과를 노출하거나 금액 화면으로 넘어간다.
 */
data class AccountInputState(
    val accountNumber: String = "",
    val selectedBank: BankCode = BankCode.KAKAO,
    val isBankPickerVisible: Boolean = false,
    val isResolving: Boolean = false,
    val error: AccountInputError? = null,
) {
    val isConfirmEnabled: Boolean get() = accountNumber.isNotBlank() && !isResolving
}
