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

    @Query("UPDATE mock_accounts SET balance_amt = :balanceAmt WHERE fintech_use_num = :fintechUseNum")
    fun updateBalance(fintechUseNum: String, balanceAmt: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(accounts: List<SeedAccount>)

    @Query("DELETE FROM mock_accounts")
    fun clear()
}
