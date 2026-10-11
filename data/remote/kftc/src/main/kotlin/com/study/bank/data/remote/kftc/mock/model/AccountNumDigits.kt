package com.study.bank.data.remote.kftc.mock.model

/** 계좌번호의 숫자 부분. 숫자와 구분용 하이픈으로만 이루어진 값만 계좌번호로 본다. */
@JvmInline
internal value class AccountNumDigits private constructor(val value: String) {

    companion object {
        private val FORMAT = Regex("""\d+(-\d+)*""")

        /** 요청으로 들어온 값처럼 형식이 보장되지 않는 값에 쓴다. 마스킹 번호도 여기서 걸러진다. */
        fun parseOrNull(accountNum: String): AccountNumDigits? =
            if (FORMAT.matches(accountNum)) AccountNumDigits(accountNum.replace("-", "")) else null

        /** 시드처럼 형식이 보장된 값에 쓴다. */
        fun of(accountNum: String): AccountNumDigits =
            requireNotNull(parseOrNull(accountNum)) { "계좌번호 형식이 아니다: $accountNum" }
    }
}
