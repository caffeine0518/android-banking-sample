package com.study.bank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.study.bank.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY id")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AccountEntity>)

    @Query("DELETE FROM accounts")
    suspend fun clear()

    /**
     * 원격 fetch 결과로 테이블을 통째 갈아끼움. 닫힌 계좌가 응답에서 사라졌을 때 stale entry가
     * 남지 않도록 트랜잭션으로 clear → insert.
     */
    @Transaction
    suspend fun replaceAll(entities: List<AccountEntity>) {
        clear()
        insertAll(entities)
    }
}
