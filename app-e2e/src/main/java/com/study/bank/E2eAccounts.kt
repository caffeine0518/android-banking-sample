package com.study.bank

import com.study.bank.data.remote.kftc.mock.KftcSeedAccountIds
import com.study.bank.domain.model.Currency

/**
 * E2E가 계좌를 **특정 id가 아니라 통화 의도로** 고른다.
 *
 * 송금 플로우의 본질은 "같은 통화끼리는 성공 / 다른 통화는 거절"이라는 행위다. "001 계좌"가 아니라 통화
 * 쌍으로 표현하면 시드의 어떤 계좌가 무슨 통화인지 바뀌어도 그 의도가 유지된다. 통화→id 매핑은
 * [KftcSeedAccountIds.idsOf]가 시드에서 파생한다.
 */
internal object E2eAccounts {

    /** [currency] 계좌 하나(시드 등록 순 첫 번째). */
    fun firstOf(currency: Currency): String =
        idsOf(currency).firstOrNull()
            ?: error("시드에 ${currency.code} 계좌가 없다 — E2E 픽스처 가정 위반")

    /** 통화가 [currency]로 같은 서로 다른 두 계좌 (출금, 수취). */
    fun sameCurrencyPair(currency: Currency): Pair<String, String> {
        val ids = idsOf(currency)
        check(ids.size >= 2) { "시드에 ${currency.code} 계좌가 2개 미만 — 동일통화 송금 불가" }
        return ids[0] to ids[1]
    }

    /** 출금 [from] · 수취 [to]가 서로 다른 통화인 (출금, 수취). */
    fun crossCurrencyPair(from: Currency, to: Currency): Pair<String, String> {
        require(from != to) { "cross-currency인데 통화가 같다: $from" }
        return firstOf(from) to firstOf(to)
    }

    private fun idsOf(currency: Currency): List<String> = KftcSeedAccountIds.idsOf(currency.code)
}
