package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.account.AccountId

sealed interface RecipientLookup {
    data class Active(val accountId: AccountId, val holderName: String) : RecipientLookup
    data class Inactive(val accountId: AccountId, val holderName: String) : RecipientLookup
    data object NotFound : RecipientLookup
}
