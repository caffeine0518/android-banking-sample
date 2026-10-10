package com.study.bank.domain.model.account

import com.study.bank.domain.model.Money

data class AssetTotals(
    val converted: Money,
    /** 환율이 없어 환산하지 못한 잔액. 원본 통화 그대로다. */
    val unconverted: List<Money>,
)
