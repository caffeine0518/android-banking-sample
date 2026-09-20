package com.study.bank.data.repository.recipient

import com.study.bank.data.remote.kftc.api.ACCOUNT_STATUS_INACTIVE
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transfer.RecipientLookup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * KFTC 계좌실명조회 응답 → 도메인 [RecipientLookup] 매퍼.
 *
 * rsp_code가 성공이 아니거나 식별 정보(예금주명/식별자)가 비면 NotFound, 상태가 INACTIVE면 Inactive,
 * 그 외 Active. rsp_code/상태 문자열은 [RSP_SUCCESS] 등 KFTC 와이어 계약값을 그대로 쓴다.
 */
@Singleton
class RecipientMapper @Inject constructor() {

    fun map(response: RealNameInquiryResponse): RecipientLookup {
        if (response.rspCode != RSP_SUCCESS) return RecipientLookup.NotFound
        val holderName = response.accountHolderName ?: return RecipientLookup.NotFound
        val accountId = response.accountId ?: return RecipientLookup.NotFound
        return if (response.accountStatus == ACCOUNT_STATUS_INACTIVE) {
            RecipientLookup.Inactive(AccountId(accountId), holderName)
        } else {
            RecipientLookup.Active(AccountId(accountId), holderName)
        }
    }
}
