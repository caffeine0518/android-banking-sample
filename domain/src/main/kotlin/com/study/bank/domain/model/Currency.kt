package com.study.bank.domain.model

/**
 * Currencies the bank domain explicitly supports.
 *
 * [exponent] is the number of fractional digits in the currency's minor unit
 * (ISO 4217). It governs how [Money] amounts are normalized and rendered.
 */
enum class Currency(val code: String, val exponent: Int) {
    KRW("KRW", 0),
    USD("USD", 2),
    JPY("JPY", 0),
    EUR("EUR", 2),
    // KEXIM API 미커버 통화 — 사용자는 외화통장으로 보유 가능하지만 환산 불가 경로로 흐름.
    TWD("TWD", 2),
    VND("VND", 0),
    ;

    companion object {
        /**
         * 통화를 결정할 수 없을 때(로케일 해석 실패, 미지원 코드 등) 모든 피쳐가 따를 기본값.
         * USD는 국제 결제·환율의 사실상 기준 통화라 안전한 fallback.
         */
        val DEFAULT: Currency = USD

        fun byCode(code: String): Currency? = entries.firstOrNull { it.code == code }

        /** [code]가 우리가 저장/응답한 값이라 반드시 지원돼야 할 때. 미지원이면 스키마-코드 불일치이므로 fail-fast. */
        fun requireByCode(code: String): Currency =
            requireNotNull(byCode(code)) { "Unsupported currency: $code" }

        /** [code]가 null이거나 미지원이면 [DEFAULT]로 폴백. */
        fun byCodeOrDefault(code: String?): Currency = code?.let(::byCode) ?: DEFAULT
    }
}
