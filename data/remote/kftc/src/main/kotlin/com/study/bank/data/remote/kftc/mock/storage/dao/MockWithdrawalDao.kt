package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.study.bank.data.remote.kftc.mock.storage.entity.SettledWithdrawal

/**
 * `mock_settled_withdrawals` 접근. 체결된 출금이체를 거래고유번호(bank_tran_id)로 기록한다.
 *
 * 응답이 유실돼 클라이언트가 같은 번호로 재시도하면 이 기록을 조회해 **원장을 다시 변경하지 않고**
 * 처음 체결한 응답을 그대로 반환한다(이중 출금 차단). 거절 건은 원장을 변경하지 않았으므로 기록하지 않는다.
 */
@Dao
internal interface MockWithdrawalDao {

    /** 이 거래고유번호로 이미 체결된 건이 있으면 그 기록을, 없으면 null을 반환한다. */
    @Query("SELECT * FROM mock_settled_withdrawals WHERE bank_tran_id = :bankTranId")
    fun findSettled(bankTranId: String): SettledWithdrawal?

    @Insert
    fun insertSettled(settled: SettledWithdrawal)

    @Query("DELETE FROM mock_settled_withdrawals")
    fun clear()
}
