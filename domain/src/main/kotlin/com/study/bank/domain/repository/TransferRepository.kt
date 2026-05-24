package com.study.bank.domain.repository

import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest

interface TransferRepository {
    suspend fun execute(request: TransferRequest): TransferOutcome
}
