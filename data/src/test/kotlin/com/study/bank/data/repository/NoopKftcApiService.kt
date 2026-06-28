package com.study.bank.data.repository

import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse

/**
 * 모든 엔드포인트가 호출되면 실패하는 [KftcApiService] 기본 구현(테스트 공용).
 *
 * 각 레포 테스트는 자신이 쓰는 메서드만 `by NoopKftcApiService`로 위임 후 override한다 — 안 쓰는 엔드포인트
 * no-op 보일러플레이트를 5개 테스트가 복붙하던 것을 한곳으로 모은다. 인터페이스에 메서드가 늘어도 여기만 고친다.
 */
internal object NoopKftcApiService : KftcApiService {
    override suspend fun getAccountList(userSeqNo: String, includeCancelYn: String, sortOrder: String): AccountListResponse =
        error("unused")

    override suspend fun getAccountBalance(bankTranId: String, fintechUseNum: String, tranDtime: String): AccountBalanceResponse =
        error("unused")

    override suspend fun getTransactionList(
        bankTranId: String,
        fintechUseNum: String,
        fromDate: String,
        toDate: String,
        tranDtime: String,
        inquiryType: String,
        inquiryBase: String,
        sortOrder: String,
        beforInquiryTraceInfo: String?,
    ): TransactionListResponse = error("unused")

    override suspend fun withdraw(request: WithdrawTransferRequest): WithdrawTransferResponse =
        error("unused")

    override suspend fun inquireRealName(request: RealNameInquiryRequest): RealNameInquiryResponse =
        error("unused")
}
