package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount

/** 디스패처 스레드에서 동기 호출하므로 suspend가 아니다. */
@Dao
internal interface MockAccountDao {

    /** list_finuse 응답 순서가 시드 선언 순서와 같도록 rowid로 정렬한다. */
    @Query("SELECT * FROM mock_accounts ORDER BY rowid")
    fun findAll(): List<SeedAccount>

    @Query("SELECT * FROM mock_accounts WHERE fintech_use_num = :fintechUseNum")
    fun find(fintechUseNum: String): SeedAccount?

    /**
     * 앱은 list_finuse에서 마스킹된 번호만 받으므로 내 계좌끼리 송금하면 마스킹된 번호로 요청한다.
     * 마스킹 번호에는 "*"가 있어 외부 계좌의 전체 번호와 겹치지 않는다.
     */
    @Query(
        """
        SELECT * FROM mock_accounts
        WHERE bank_code_std = :bankCodeStd
          AND (account_num = :accountNum OR account_num_masked = :accountNum)
        """,
    )
    fun findByAccountNum(bankCodeStd: String, accountNum: String): SeedAccount?

    @Query("UPDATE mock_accounts SET balance_amt = :balanceAmt WHERE fintech_use_num = :fintechUseNum")
    fun updateBalance(fintechUseNum: String, balanceAmt: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(accounts: List<SeedAccount>)

    @Query("DELETE FROM mock_accounts")
    fun clear()
}
