package com.study.bank.data.remote.kftc.mock.storage.entity

import androidx.room.Embedded
import androidx.room.Entity
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawFingerprint
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult

@Entity(tableName = "mock_settled_withdrawals", primaryKeys = ["bank_tran_id"])
internal data class SettledWithdrawal(
    @Embedded
    val response: WithdrawResult.Success,
    @Embedded(prefix = "req_")
    val fingerprint: WithdrawFingerprint,
)
