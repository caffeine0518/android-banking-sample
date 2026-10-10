package com.study.bank.data.repository.recipient

import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.repository.tranDtime
import com.study.bank.data.repository.bankTranIdFor
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.RecipientLookup
import com.study.bank.domain.repository.RecipientRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** 다른 사람 계좌에 대한 일회성 조회라 Room에 캐시하지 않는다. */
@Singleton
class RecipientRepositoryImpl @Inject constructor(
    private val api: KftcApiService,
    private val mapper: RecipientMapper,
    private val clock: Clock,
) : RecipientRepository {

    override suspend fun lookup(accountNumber: AccountNumber, bankCode: BankCode): RecipientLookup =
        mapper.map(
            api.inquireRealName(
                RealNameInquiryRequest(
                    bankTranId = bankTranIdFor(accountNumber.value),
                    bankCodeStd = bankCode.code,
                    accountNum = accountNumber.value,
                    tranDtime = tranDtime(clock),
                ),
            ),
        )
}
