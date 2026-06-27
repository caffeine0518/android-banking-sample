package com.study.bank.feature.transfer.recipient.ui.preview

import com.study.bank.core.ui.preview.PREVIEW_LIST_SIZE
import com.study.bank.feature.transfer.recipient.contract.RecipientState
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountUi

internal val PreviewRecipientState = RecipientState(
    myAccounts = listOf(
        AccountUi(
            id = "acc-2",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.CHECKING,
            nickname = "외화통장 USD",
            numberMasked = "1000-98-***4321",
        ),
        AccountUi(
            id = "acc-3",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.SAVINGS,
            nickname = "세이프박스",
            numberMasked = "1000-55-***4443",
        ),
        AccountUi(
            id = "acc-4",
            bankDisplayName = "신한은행",
            type = AccountTypeUi.CHECKING,
            nickname = null,
            numberMasked = "110-23-***7890",
        ),
    ),
)

/** LazyColumn 스크롤이 실제로 동작하는지 확인하기 위한 다건 내 계좌 프리뷰 상태. */
internal val PreviewRecipientStateLongList = RecipientState(
    myAccounts = List(PREVIEW_LIST_SIZE) { index ->
        AccountUi(
            id = "acc-${index + 1}",
            bankDisplayName = if (index % 4 == 0) "신한은행" else "토스뱅크",
            type = AccountTypeUi.entries[index % AccountTypeUi.entries.size],
            nickname = if (index % 3 == 0) null else "통장 ${index + 1}",
            numberMasked = "1000-%02d-***%04d".format(index % 100, (index + 1) * 11 % 10000),
        )
    },
)
