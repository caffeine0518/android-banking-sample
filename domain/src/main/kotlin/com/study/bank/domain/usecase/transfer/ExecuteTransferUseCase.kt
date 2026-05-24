package com.study.bank.domain.usecase.transfer

import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest
import com.study.bank.domain.repository.TransferRepository

class ExecuteTransferUseCase(
    private val transferRepository: TransferRepository,
) {
    suspend operator fun invoke(request: TransferRequest): TransferOutcome =
        transferRepository.execute(request)
}
