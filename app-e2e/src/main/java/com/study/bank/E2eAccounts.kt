package com.study.bank

import com.study.bank.data.remote.kftc.mock.KftcSeedAccountIds
import com.study.bank.domain.model.Currency

/**
 * E2E가 계좌를 **특정 id가 아니라 통화 의도로** 고른다.
 *
 * 송금 플로우의 본질은 "같은 통화끼리는 성공 / 다른 통화는 거절"이라는 행위다. 그래서 "001 계좌"가 아니라
 * "같은 통화 쌍" / "다른 통화 쌍"을 표현한다 — 시드의 어떤 계좌가 무슨 통화인지 바뀌어도 그 *의도*는
 * 구조적으로 유지된다(예: 누가 시드 통화를 바꿔도 동일통화 성공 테스트는 여전히 동일통화 쌍을 고른다).
 *
 * 통화→id 매핑은 [KftcSeedAccountIds.idsOf]가 시드에서 파생하므로 별도 하드코딩 중복이 없다. 실서버
 * 대상이라면 여기를 "런타임에 목록을 통화로 조회"로 바꾸면 된다 — 테스트 본문(통화 의도)은 그대로 둔 채.
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
