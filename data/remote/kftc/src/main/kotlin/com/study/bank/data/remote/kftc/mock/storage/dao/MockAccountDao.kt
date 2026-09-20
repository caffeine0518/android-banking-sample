package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount

/** `mock_accounts` 접근. 디스패처 스레드에서 동기 호출하므로 suspend가 아니다. */
@Dao
internal interface MockAccountDao {

    /** 적재 순서(=시드 선언 순서)를 유지한다 — list_finuse 응답 순서가 결정적이어야 한다. */
    @Query("SELECT * FROM mock_accounts ORDER BY rowid")
    fun findAll(): List<SeedAccount>

    @Query("SELECT * FROM mock_accounts WHERE fintech_use_num = :fintechUseNum")
    fun find(fintechUseNum: String): SeedAccount?

    /**
     * 수취계좌 판정용 조회. 앱은 list_finuse에서 마스킹 번호만 받으므로 내 계좌끼리 송금하면
     * 마스킹 번호로 들어온다 — 전체/마스킹 둘 다로 매칭한다("*"가 있어 외부 전체번호와 충돌하지 않는다).
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
