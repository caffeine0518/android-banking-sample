package com.study.bank.feature.account.ui.preview

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.core.ui.preview.PREVIEW_LIST_SIZE
import com.study.bank.feature.account.contract.AccountDetailState
import com.study.bank.feature.account.ui.model.AccountTypeUi
import com.study.bank.feature.account.ui.model.AccountUi
import com.study.bank.feature.account.ui.model.TransactionTypeUi
import com.study.bank.feature.account.ui.model.TransactionUi
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal val PreviewAccountDetailState = AccountDetailState(
    account = AccountUi(
        id = "acc-1",
        bankDisplayName = "토스뱅크",
        type = AccountTypeUi.CHECKING,
        nickname = "월급통장",
        numberMasked = "1000-12-***6789",
        balance = MoneyUi(BigDecimal("2797320"), CurrencyUi.KRW),
    ),
)

/** 프리뷰용 거래내역 표본 3건. */
internal val previewTransactionItems = listOf(
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
)

// 프리뷰는 androidx 공식 샘플(PagingPreviewSample)대로 MutableStateFlow를 쓴다.
// flowOf는 1회 emit 후 완료되는 cold flow라 정적 @Preview에서 presenter가 데이터를 그리지 못하고
// 초기 프레임(빈/로딩)에 머문다 — 모든 프리뷰가 똑같이 보이는 원인. MutableStateFlow는 완료되지
// 않는 hot flow라 즉시 present 되어 정적 프리뷰에서도 상태별로 렌더된다.

/** 거래내역 페이징 프리뷰. */
internal val previewTransactions: Flow<PagingData<TransactionUi>> =
    MutableStateFlow(PagingData.from(previewTransactionItems))

/** LazyColumn 스크롤 확인용 다건 페이징 프리뷰. */
internal val previewTransactionsLong: Flow<PagingData<TransactionUi>> = MutableStateFlow(
    PagingData.from(
        List(PREVIEW_LIST_SIZE) { index ->
            val type = TransactionTypeUi.entries[index % TransactionTypeUi.entries.size]
            TransactionUi(
                id = "tx-${index + 1}",
                type = type,
                counterpartyName = if (type == TransactionTypeUi.WITHDRAWAL) null else "거래상대 ${index + 1}",
                amount = MoneyUi(BigDecimal((index + 1) * 1_000L), CurrencyUi.KRW),
                occurredAtLabel = "2026.06.%02d".format((index % 28) + 1),
            )
        },
    ),
)

/** 거래내역 없음 — 빈 안내 프리뷰. (IDLE = refresh NotLoading) */
internal val previewTransactionsEmpty: Flow<PagingData<TransactionUi>> =
    MutableStateFlow(PagingData.from(emptyList()))

private val previewLoadError = IllegalStateException("미리보기용 에러")

/** 첫 페이지 로드 실패 — 화면 전체 에러+재시도 프리뷰. */
internal val previewTransactionsError: Flow<PagingData<TransactionUi>> = MutableStateFlow(
    PagingData.from(
        emptyList(),
        sourceLoadStates = LoadStates(
            refresh = LoadState.Error(previewLoadError),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        ),
    ),
)

/** 아이템은 있으나 다음 페이지(append) 적재가 실패 — 하단 재시도 푸터 프리뷰. */
internal val previewTransactionsFooterError: Flow<PagingData<TransactionUi>> = MutableStateFlow(
    PagingData.from(
        previewTransactionItems,
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.Error(previewLoadError),
        ),
    ),
)

/** 아이템이 있고 다음 페이지(append)를 당겨오는 중 — 하단 진행 표시 푸터 프리뷰. */
internal val previewTransactionsFooterLoading: Flow<PagingData<TransactionUi>> = MutableStateFlow(
    PagingData.from(
        previewTransactionItems,
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.Loading,
        ),
    ),
)
