package com.study.bank.feature.account.ui.preview

import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.core.ui.preview.PREVIEW_LIST_SIZE
import com.study.bank.feature.account.contract.AccountDetailState
import com.study.bank.feature.account.ui.model.AccountTypeUi
import com.study.bank.feature.account.ui.model.AccountUi
import com.study.bank.feature.account.ui.model.TransactionTypeUi
import com.study.bank.feature.account.ui.model.TransactionUi
import java.math.BigDecimal

internal val PreviewAccountDetailState = AccountDetailState(
    account = AccountUi(
        id = "acc-1",
        bankDisplayName = "토스뱅크",
        type = AccountTypeUi.CHECKING,
        nickname = "월급통장",
        numberMasked = "1000-12-***6789",
        balance = MoneyUi(BigDecimal("2797320"), CurrencyUi.KRW),
    ),
    transactions = listOf(
        TransactionUi(
            id = "tx-1",
            type = TransactionTypeUi.TRANSFER_OUT,
            counterpartyName = "세이프박스",
            amount = MoneyUi(BigDecimal("50000"), CurrencyUi.KRW),
            occurredAtLabel = "2026.06.18",
        ),
        TransactionUi(
            id = "tx-2",
            type = TransactionTypeUi.TRANSFER_IN,
            counterpartyName = "김토스",
            amount = MoneyUi(BigDecimal("120000"), CurrencyUi.KRW),
            occurredAtLabel = "2026.06.15",
        ),
        TransactionUi(
            id = "tx-3",
            type = TransactionTypeUi.WITHDRAWAL,
            counterpartyName = null,
            amount = MoneyUi(BigDecimal("8500"), CurrencyUi.KRW),
            occurredAtLabel = "2026.06.14",
        ),
    ),
)

/** LazyColumn 스크롤이 실제로 동작하는지 확인하기 위한 다건 거래내역 프리뷰 상태. */
internal val PreviewAccountDetailStateLongList = PreviewAccountDetailState.copy(
    transactions = List(PREVIEW_LIST_SIZE) { index ->
        val type = TransactionTypeUi.entries[index % TransactionTypeUi.entries.size]
        TransactionUi(
            id = "tx-${index + 1}",
            type = type,
            counterpartyName = if (type == TransactionTypeUi.WITHDRAWAL) null else "거래상대 ${index + 1}",
            amount = MoneyUi(BigDecimal((index + 1) * 1_000L), CurrencyUi.KRW),
            occurredAtLabel = "2026.06.%02d".format((index % 28) + 1),
        )
    },
)
