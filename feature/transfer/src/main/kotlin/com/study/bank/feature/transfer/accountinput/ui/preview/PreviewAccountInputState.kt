package com.study.bank.feature.transfer.accountinput.ui.preview

import com.study.bank.domain.model.BankCode
import com.study.bank.feature.transfer.accountinput.contract.AccountInputState

/** @Preview·테스트용 샘플 상태. 스크린샷의 "868369666 · 카카오뱅크" 입력 상태. */
internal val PreviewAccountInputState = AccountInputState(
    accountNumber = "868369666",
    selectedBank = BankCode.KAKAO,
)
