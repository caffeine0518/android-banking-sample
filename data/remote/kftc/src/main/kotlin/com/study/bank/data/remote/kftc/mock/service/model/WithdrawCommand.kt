package com.study.bank.data.remote.kftc.mock.service.model

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
