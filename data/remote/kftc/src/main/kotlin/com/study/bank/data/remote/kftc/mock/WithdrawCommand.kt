package com.study.bank.data.remote.kftc.mock

/**
 * 출금이체 요청을 상태 계층이 이해하는 형태로 정규화한 명령.
 *
 * KFTC `WithdrawTransferRequest` DTO에서 dispatcher가 추출한다. 수취계좌가 시드에 있으면
 * (recvAccountNum, recvBankCode)로 매칭돼 복식부기 입금까지 일어난다.
 */
internal data class WithdrawCommand(
    val fintechUseNum: String,
    val tranAmt: String,
    val recvAccountNum: String,
    val recvBankCode: String,
    val recvName: String,
    val reqName: String,
    val wdPrintContent: String?,
    val dpsPrintContent: String?,
)
