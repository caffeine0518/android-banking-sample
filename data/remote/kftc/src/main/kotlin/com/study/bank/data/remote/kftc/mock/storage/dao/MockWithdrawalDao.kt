package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.study.bank.data.remote.kftc.mock.storage.entity.SettledWithdrawal

/** 응답이 유실돼 같은 bank_tran_id로 재시도하면 원장을 다시 변경하지 않고 처음 응답을 반환하기 위한 기록. */
@Dao
internal interface MockWithdrawalDao {

    @Query("SELECT * FROM mock_settled_withdrawals WHERE bank_tran_id = :bankTranId")
    fun findSettled(bankTranId: String): SettledWithdrawal?

    @Insert
    fun insertSettled(settled: SettledWithdrawal)

    @Query("DELETE FROM mock_settled_withdrawals")
    fun clear()
}
