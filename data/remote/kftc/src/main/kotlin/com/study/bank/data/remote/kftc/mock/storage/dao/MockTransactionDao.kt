package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionRecord

@Dao
internal interface MockTransactionDao {

    @Query(
        "SELECT * FROM mock_transactions WHERE fintech_use_num = :fintechUseNum AND seeded = 0 ORDER BY seq DESC",
    )
    fun sessionLedger(fintechUseNum: String): List<TransactionRecord>

    @Query("SELECT * FROM mock_transactions WHERE fintech_use_num = :fintechUseNum ORDER BY seq DESC")
    fun statement(fintechUseNum: String): List<TransactionRecord>

    /** 키셋 페이지네이션이라 조회 도중 새 거래가 추가돼도 페이지 경계가 바뀌지 않는다. */
    @Query(
        """
        SELECT * FROM mock_transactions
        WHERE fintech_use_num = :fintechUseNum
          AND (:afterSeq IS NULL OR seq < :afterSeq)
        ORDER BY seq DESC
        LIMIT :limit
        """,
    )
    fun page(fintechUseNum: String, afterSeq: Long?, limit: Int): List<TransactionRecord>

    @Insert
    fun insert(record: TransactionRecord)

    @Insert
    fun insertAll(records: List<TransactionRecord>)

    @Query("DELETE FROM mock_transactions")
    fun clear()
}
