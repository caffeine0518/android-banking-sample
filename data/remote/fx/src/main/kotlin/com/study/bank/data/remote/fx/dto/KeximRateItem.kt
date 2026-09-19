package com.study.bank.data.remote.fx.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 한국수출입은행(KEXIM) 환율 API `exchangeJSON` 응답 배열의 한 항목.
 *
 * envelope 없이 배열 그대로 온다. [result] != 1이면 나머지 필드가 전부 null이다.
 * 숫자형 필드는 천단위 쉼표 포함 문자열("1,538.29")이고, JPY는 [curUnit]이 `"JPY(100)"`(100엔당)이라
 * 매퍼에서 파싱·정규화한다.
 */
@Serializable
data class KeximRateItem(
    /**
     * 결과 코드. 1=성공, 2=비영업일/조회 데이터 없음, 3=인증키 실패, 4=일일 1,000회 한도 초과.
     */
    @SerialName("result") val result: Int,

    /** 통화코드. "USD", "EUR", "JPY(100)", "CNH" 등. */
    @SerialName("cur_unit") val curUnit: String? = null,

    /** 통화명. "미국 달러", "일본 옌" 등. */
    @SerialName("cur_nm") val curNm: String? = null,

    /** 전신환 매입율 (은행이 고객에게서 외화를 살 때 = 고객이 외화를 팔 때). */
    @SerialName("ttb") val ttb: String? = null,

    /** 전신환 매도율 (은행이 고객에게 외화를 팔 때 = 고객이 외화를 살 때). */
    @SerialName("tts") val tts: String? = null,

    /** 매매기준율. */
    @SerialName("deal_bas_r") val dealBasR: String? = null,

    /** 장부가격 (회계 처리용). */
    @SerialName("bkpr") val bkpr: String? = null,

    /** 년환가료율. */
    @SerialName("yy_efee_r") val yyEfeeR: String? = null,

    /** 10일환가료율. */
    @SerialName("ten_dd_efee_r") val tenDdEfeeR: String? = null,

    /** 한국은행(KFTC) 기준 장부가격. */
    @SerialName("kftc_bkpr") val kftcBkpr: String? = null,

    /** 한국은행(KFTC) 기준 매매기준율. [dealBasR]과 비교/대조용. */
    @SerialName("kftc_deal_bas_r") val kftcDealBasR: String? = null,
)
