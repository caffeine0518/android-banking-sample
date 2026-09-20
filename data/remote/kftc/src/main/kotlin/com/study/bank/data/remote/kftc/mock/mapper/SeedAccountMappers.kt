package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.HOLDER_TYPE_PERSONAL
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.account.FintechAccountDto
import com.study.bank.data.remote.kftc.mock.model.KftcEnvelope
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount

/** [SeedAccount] → KFTC DTO 순수 매퍼. */

internal fun SeedAccount.toListItem(): FintechAccountDto = FintechAccountDto(
    fintechUseNum = fintechUseNum,
    accountAlias = accountAlias,
    bankCodeStd = bankCodeStd,
    bankName = bankName,
    accountNumMasked = accountNumMasked,
    accountHolderName = accountHolderName,
    accountHolderType = HOLDER_TYPE_PERSONAL,
    accountType = accountType,
)

internal fun List<SeedAccount>.toListResponse(
    envelope: KftcEnvelope,
    userSeqNo: String,
): AccountListResponse = AccountListResponse(
    apiTranId = envelope.apiTranId,
    apiTranDtm = envelope.apiTranDtm,
    rspCode = RSP_SUCCESS,
    rspMessage = "",
    userSeqNo = userSeqNo,
    resCnt = size.toString(),
    resList = map { it.toListItem() },
)

internal fun SeedAccount.toBalanceResponse(envelope: KftcEnvelope): AccountBalanceResponse =
    AccountBalanceResponse(
        apiTranId = envelope.apiTranId,
        apiTranDtm = envelope.apiTranDtm,
        rspCode = RSP_SUCCESS,
        rspMessage = "",
        bankTranId = envelope.bankTranId,
        bankCodeTran = bankCodeStd,
        bankRspCode = BANK_RSP_OK,
        fintechUseNum = fintechUseNum,
        balanceAmt = balanceAmt,
        availableAmt = balanceAmt,
        accountType = accountType,
        productName = productName,
        currencyCode = currencyCode,
    )
