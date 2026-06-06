package com.study.bank.data.remote.mock.dispatcher

import com.study.bank.data.remote.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.dto.account.AccountListResponse
import com.study.bank.data.remote.dto.account.FintechAccountDto
import com.study.bank.data.remote.mock.SeedAccount

/**
 * [SeedAccount] → KFTC DTO 순수 매퍼.
 *
 * tran_id, dtm 등 envelope 추적 필드는 호출 측이 채워 넣는다(생성기 의존성을 매퍼에서 분리하기 위함).
 */

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
    apiTranId: String,
    apiTranDtm: String,
    userSeqNo: String,
): AccountListResponse = AccountListResponse(
    apiTranId = apiTranId,
    apiTranDtm = apiTranDtm,
    rspCode = RSP_SUCCESS,
    rspMessage = "",
    userSeqNo = userSeqNo,
    resCnt = size.toString(),
    resList = map { it.toListItem() },
)

internal fun SeedAccount.toBalanceResponse(
    apiTranId: String,
    apiTranDtm: String,
    bankTranId: String,
): AccountBalanceResponse = AccountBalanceResponse(
    apiTranId = apiTranId,
    apiTranDtm = apiTranDtm,
    rspCode = RSP_SUCCESS,
    rspMessage = "",
    bankTranId = bankTranId,
    bankCodeTran = bankCodeStd,
    bankRspCode = BANK_RSP_OK,
    fintechUseNum = fintechUseNum,
    balanceAmt = balanceAmt,
    availableAmt = balanceAmt,
    accountType = accountType,
    productName = productName,
    currencyCode = currencyCode,
)
