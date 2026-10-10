package com.study.bank.domain.model

/** [exponent]는 ISO 4217 기준 최소 단위의 소수 자릿수다. */
enum class Currency(val code: String, val exponent: Int) {
    KRW("KRW", 0),
    USD("USD", 2),
    JPY("JPY", 0),
    EUR("EUR", 2),
    // KEXIM이 환율을 제공하지 않아 총자산 환산에서 제외된다.
    TWD("TWD", 2),
    VND("VND", 0),
    ;

    companion object {
        /** 통화를 결정할 수 없을 때의 기본값. 국제 결제의 기준 통화라 USD로 둔다. */
        val DEFAULT: Currency = USD

        fun byCode(code: String): Currency? = entries.firstOrNull { it.code == code }

        /** 저장·응답한 코드처럼 반드시 지원돼야 하는 경우에 쓴다. 미지원이면 스키마와 코드가 어긋난 것이므로 즉시 실패한다. */
        fun requireByCode(code: String): Currency =
            requireNotNull(byCode(code)) { "Unsupported currency: $code" }

        fun byCodeOrDefault(code: String?): Currency = code?.let(::byCode) ?: DEFAULT
    }
}
