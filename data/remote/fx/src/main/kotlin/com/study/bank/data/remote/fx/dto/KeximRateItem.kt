package com.study.bank.data.remote.fx.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 숫자 필드는 천 단위 쉼표가 포함된 문자열이다("1,538.29"). */
@Serializable
data class KeximRateItem(
    /** 1=성공, 2=비영업일·데이터 없음, 3=인증키 오류, 4=일일 한도 초과. 1이 아니면 나머지 필드는 null이다. */
    @SerialName("result") val result: Int,

    /** 통화코드. JPY는 100엔당 환율이라 "JPY(100)"이다. */
    @SerialName("cur_unit") val curUnit: String? = null,

    @SerialName("cur_nm") val curNm: String? = null,

    /** 전신환 매입률. 고객이 외화를 팔 때 적용된다. */
    @SerialName("ttb") val ttb: String? = null,

    /** 전신환 매도율. 고객이 외화를 살 때 적용된다. */
    @SerialName("tts") val tts: String? = null,

    /** 매매기준율. */
    @SerialName("deal_bas_r") val dealBasR: String? = null,

    /** 장부가격. */
    @SerialName("bkpr") val bkpr: String? = null,

    /** 연 환가료율. */
    @SerialName("yy_efee_r") val yyEfeeR: String? = null,

    /** 10일환가료율. */
    @SerialName("ten_dd_efee_r") val tenDdEfeeR: String? = null,

    /** KFTC 기준 장부가격. */
    @SerialName("kftc_bkpr") val kftcBkpr: String? = null,

    /** KFTC 기준 매매기준율. */
    @SerialName("kftc_deal_bas_r") val kftcDealBasR: String? = null,
)
