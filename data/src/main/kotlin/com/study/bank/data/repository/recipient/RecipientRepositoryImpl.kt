package com.study.bank.data.repository.recipient

import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.RecipientLookup
import com.study.bank.domain.repository.RecipientRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 수취인 실명조회 = KFTC inquiry/real_name 직접 호출 (원격 전용, SSOT/캐시 없음).
 *
 * 남의 계좌에 대한 일회성 질의라 Room에 캐시하지 않는다 —
 * [com.study.bank.data.repository.account.AccountRepositoryImpl]의 SSOT 패턴과 의도적으로 다른 결.
 * 응답→도메인 매핑은 [mapper]에 위임.
 */
@Singleton
class RecipientRepositoryImpl @Inject constructor(
    private val api: KftcApiService,
    private val mapper: RecipientMapper,
) : RecipientRepository {

    override suspend fun lookup(accountNumber: AccountNumber, bankCode: BankCode): RecipientLookup =
        mapper.map(
            api.inquireRealName(
                RealNameInquiryRequest(
                    bankTranId = bankTranIdFor(accountNumber.value),
                    bankCodeStd = bankCode.code,
                    accountNum = accountNumber.value,
                    tranDtime = TRAN_DTIME,
                ),
            ),
        )

    private companion object {
        // 데모 고정값. 실서비스는 요청 추적자(bank_tran_id, tran_dtime)를 동적으로 구성한다.
        const val TRAN_DTIME = "20260603120000"

        fun bankTranIdFor(accountNum: String): String =
            "M202300001U%06d".format(accountNum.hashCode() and 0xFFFFF)
    }
}
