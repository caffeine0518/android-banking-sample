package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.HOLDER_TYPE_PERSONAL
import com.study.bank.data.remote.kftc.api.INOUT_DEPOSIT
import com.study.bank.data.remote.kftc.api.INOUT_WITHDRAW
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.api.TRAN_TYPE_TRANSFER
import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.account.FintechAccountDto
import com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.mock.http.response.KftcTranIds
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.TransactionDirection
import com.study.bank.data.remote.kftc.mock.storage.TransactionRecord

/** 저장 모델 → KFTC `/v2.0/account/…` 응답 DTO. */
internal class AccountResponseMapper(private val tranIds: KftcTranIds) {

    fun toListResponse(accounts: List<SeedAccount>): AccountListResponse = AccountListResponse(
        apiTranId = tranIds.newApiTranId(),
        apiTranDtm = tranIds.nowDtm(),
        rspCode = RSP_SUCCESS,
        rspMessage = "",
        userSeqNo = USER_SEQ_NO,
        resCnt = accounts.size.toString(),
        resList = accounts.map(::toListItem),
    )

    fun toBalanceResponse(account: SeedAccount): AccountBalanceResponse = AccountBalanceResponse(
        apiTranId = tranIds.newApiTranId(),
        apiTranDtm = tranIds.nowDtm(),
        rspCode = RSP_SUCCESS,
        rspMessage = "",
        bankTranId = tranIds.newBankTranId(),
        bankCodeTran = account.bankCodeStd,
        bankRspCode = BANK_RSP_OK,
        fintechUseNum = account.fintechUseNum,
        balanceAmt = account.balanceAmt,
        availableAmt = account.balanceAmt,
        accountType = account.accountType,
        productName = account.productName,
        currencyCode = account.currencyCode,
    )

    /** 방향 enum을 와이어 문자열("입금"/"출금")로, tran_type를 "이체"로 고정 변환한다. */
    fun toTransactionListResponse(
        account: SeedAccount,
        records: List<TransactionRecord>,
        hasNext: Boolean,
        nextCursor: String,
    ): TransactionListResponse = TransactionListResponse(
        apiTranId = tranIds.newApiTranId(),
        apiTranDtm = tranIds.nowDtm(),
        rspCode = RSP_SUCCESS,
        rspMessage = "",
        bankTranId = tranIds.newBankTranId(),
        fintechUseNum = account.fintechUseNum,
        balanceAmt = account.balanceAmt,
        currencyCode = account.currencyCode,
        resCnt = records.size.toString(),
        resList = records.map(::toItemDto),
        nextPageYn = if (hasNext) "Y" else "N",
        beforInquiryTraceInfo = nextCursor,
    )

    private fun toListItem(account: SeedAccount): FintechAccountDto = FintechAccountDto(
        fintechUseNum = account.fintechUseNum,
        accountAlias = account.accountAlias,
        bankCodeStd = account.bankCodeStd,
        bankName = account.bankName,
        accountNumMasked = account.accountNumMasked,
        accountHolderName = account.accountHolderName,
        accountHolderType = HOLDER_TYPE_PERSONAL,
        accountType = account.accountType,
    )

    private fun toItemDto(record: TransactionRecord): TransactionItemDto = TransactionItemDto(
        tranSeq = record.seq,
        tranDate = record.tranDate,
        tranTime = record.tranTime,
        inoutType = when (record.direction) {
            TransactionDirection.DEPOSIT -> INOUT_DEPOSIT
            TransactionDirection.WITHDRAWAL -> INOUT_WITHDRAW
        },
        tranType = TRAN_TYPE_TRANSFER,
        printContent = record.printContent,
        tranAmt = record.tranAmt,
        afterBalanceAmt = record.afterBalanceAmt,
    )

    private companion object {
        // 사용자 일련번호. 실서비스에선 OAuth 토큰에서 유도되지만 mock은 고정값을 응답에 넣는다.
        const val USER_SEQ_NO = "1100000001"
    }
}
