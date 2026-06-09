package com.study.bank.domain.model.account

import com.study.bank.domain.model.Money

/**
 * 사용자 자산을 환산 가능/불가 두 그룹으로 나눈 도메인 결과.
 *
 * [converted]는 target 통화로 합산된 값. [unconverted]는 FX 환율 시트에 해당 통화가
 * 없어 환산하지 못한 계좌 잔액으로, 각 항목은 원본 통화 그대로 유지된다.
 */
data class AssetTotals(
    val converted: Money,
    val unconverted: List<Money>,
)
