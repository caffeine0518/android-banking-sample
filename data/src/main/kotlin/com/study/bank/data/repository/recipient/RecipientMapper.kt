package com.study.bank.data.repository.recipient

import com.study.bank.data.remote.kftc.api.ACCOUNT_STATUS_INACTIVE
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transfer.RecipientLookup
import javax.inject.Inject
import javax.inject.Singleton

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
