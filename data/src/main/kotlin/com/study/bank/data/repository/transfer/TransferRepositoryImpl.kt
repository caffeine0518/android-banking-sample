package com.study.bank.data.repository.transfer

import android.util.Log
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.repository.TRAN_DTIME
import com.study.bank.data.repository.bankTranIdFor
import com.study.bank.domain.coroutine.cancellableCatching
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.transaction.TransactionId
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest
import com.study.bank.domain.model.transfer.TransferResult
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.TransactionRepository
import com.study.bank.domain.repository.TransferRepository
import java.io.IOException
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 출금이체 실행 = KFTC withdraw 호출 후 SSOT(Room) 재동기화.
 *
 * 업무 거절(잔액부족 등)은 KFTC가 HTTP 200 + rsp_code A0001 + bank_rsp_code로 알리고, 전송/입력 오류는
 * 예외(IOException=네트워크, 그 외=Unknown)로 매핑한다.
 */
@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val api: KftcApiService,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val clock: Clock,
) : TransferRepository {

    override suspend fun execute(request: TransferRequest): TransferOutcome {
        val response = try {
            api.withdraw(request.toWithdrawRequest())
        } catch (e: IOException) {
            return TransferOutcome.Failure.Network(e)
        } catch (e: Exception) {
            return TransferOutcome.Failure.Unknown(e)
        }

        if (response.rspCode != RSP_SUCCESS) {
            return toFailure(response.bankRspCode)
        }

        val afterBalanceAmt = response.afterBalanceAmt
            ?: return TransferOutcome.Failure.Unknown(
                IllegalStateException("성공 응답에 after_balance_amt가 없다"),
            )

        // KFTC mock 상태가 변했으니 SSOT(Room)를 재동기화. 실패해도 이체 성공은 유지(best-effort).
        cancellableCatching {
            accountRepository.refresh()
            transactionRepository.refresh(request.fromAccountId)
        }.onFailure { Log.w(TAG, "이체 후 SSOT 갱신 실패", it) }

        return TransferOutcome.Success(
            TransferResult(
                transactionId = TransactionId(response.bankTranId.orEmpty()),
                status = TransactionStatus.COMPLETED,
                balanceAfter = Money.of(afterBalanceAmt, request.amount.currency),
                completedAt = clock.instant(),
            ),
        )
    }

    private fun TransferRequest.toWithdrawRequest() = WithdrawTransferRequest(
        bankTranId = bankTranIdFor(idempotencyKey),
        fintechUseNum = fromAccountId.value,
        tranAmt = amount.amount.toPlainString(),
        tranDtime = TRAN_DTIME,
        reqClientName = senderName,
        recvClientName = recipientName,
        recvClientBankCodeStd = toBankCode.code,
        recvClientAccountNum = toAccountNumber.value,
        wdPrintContent = memo,
        dpsPrintContent = memo,
    )

    private fun toFailure(bankRspCode: String?): TransferOutcome.Failure = when (bankRspCode) {
        BANK_RSP_INSUFFICIENT_FUNDS -> TransferOutcome.Failure.InsufficientFunds
        BANK_RSP_CURRENCY_MISMATCH -> TransferOutcome.Failure.CurrencyMismatch
        else -> TransferOutcome.Failure.Unknown(
            IllegalStateException("이체 거절: bank_rsp_code=$bankRspCode"),
        )
    }

    private companion object {
        const val TAG = "TransferRepository"
        // KFTC 와이어 계약값(:data:remote:kftc의 KftcProtocol과 동일).
        const val RSP_SUCCESS = "A0000"
        const val BANK_RSP_INSUFFICIENT_FUNDS = "311"
        const val BANK_RSP_CURRENCY_MISMATCH = "320"
    }
}
