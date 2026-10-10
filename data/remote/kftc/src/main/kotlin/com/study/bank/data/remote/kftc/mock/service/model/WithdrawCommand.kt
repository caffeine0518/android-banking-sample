package com.study.bank.data.remote.kftc.mock.service.model

/**
 * 출금이체 요청을 mock 서비스가 쓰는 형태로 변환한 명령.
 *
 * KFTC `WithdrawTransferRequest` DTO에서 `TransferRequestHandler`가 추출한다. 수취계좌가 시드에 있으면
 * (recvAccountNum, recvBankCode)로 매칭해 복식부기 입금까지 처리한다.
 */
internal data class WithdrawCommand(
    val bankTranId: String,
    val fintechUseNum: String,
    val tranAmt: String,
    val recvAccountNum: String,
    val recvBankCode: String,
    val recvName: String,
    val reqName: String,
    val wdPrintContent: String?,
    val dpsPrintContent: String?,
) {
    fun fingerprint() = WithdrawFingerprint(
        fintechUseNum = fintechUseNum,
        recvBankCode = recvBankCode,
        recvAccountNum = recvAccountNum,
        tranAmt = tranAmt,
    )
}
