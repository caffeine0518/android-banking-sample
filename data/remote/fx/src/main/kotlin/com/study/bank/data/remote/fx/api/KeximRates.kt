package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem

sealed interface KeximRates {

    /** result가 1인 항목만 포함한다. */
    data class Published(val items: List<KeximRateItem>) : KeximRates

    /** 비영업일이거나 아직 게시 전이다. 다른 날짜는 조회될 수 있다. */
    data object NotPublished : KeximRates

    data object InvalidKey : KeximRates

    data object LimitExceeded : KeximRates
}
