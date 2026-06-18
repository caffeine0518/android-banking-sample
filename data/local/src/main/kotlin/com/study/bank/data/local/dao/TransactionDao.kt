package com.study.bank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.study.bank.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE account_id = :accountId ORDER BY occurred_at DESC, id")
    fun observeByAccountId(accountId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE account_id = :accountId")
    suspend fun clearByAccountId(accountId: String)

    @Transaction
    suspend fun replaceForAccount(accountId: String, entities: List<TransactionEntity>) {
        clearByAccountId(accountId)
        insertAll(entities)
    }
}
