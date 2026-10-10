package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionRecord

/** `mock_transactions` 접근. 모든 조회는 KFTC `sort_order=D`에 맞춰 seq 내림차순(최신순)이다. */
@Dao
internal interface MockTransactionDao {

    /** 이번 세션의 이체로 기록된 원장. 앱 시작 시 적재한 시드 거래내역은 제외한다. */
    @Query(
        "SELECT * FROM mock_transactions WHERE fintech_use_num = :fintechUseNum AND seeded = 0 ORDER BY seq DESC",
    )
    fun sessionLedger(fintechUseNum: String): List<TransactionRecord>

    /** 계좌 거래내역 전체(시드 + 세션 이체). */
    @Query("SELECT * FROM mock_transactions WHERE fintech_use_num = :fintechUseNum ORDER BY seq DESC")
    fun statement(fintechUseNum: String): List<TransactionRecord>

    /**
     * 연속조회 한 페이지. [afterSeq]보다 **작은(오래된)** 거래를 [limit]건 조회한다. [afterSeq]가 null이면 첫 페이지다.
     *
     * 키셋 페이지네이션이라 (1) 조회 도중 새 거래가 맨 앞에 삽입돼도 페이지 경계가 바뀌지 않고,
     * (2) seq가 유일하므로 `<` 비교로 같은 초의 거래도 누락하지 않는다.
     */
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

    /** seq는 AUTOINCREMENT라 [record]의 seq(기본 0)는 무시되고 항상 기존 최댓값보다 큰 값이 부여된다. */
    @Insert
    fun insert(record: TransactionRecord)

    @Insert
    fun insertAll(records: List<TransactionRecord>)

    @Query("DELETE FROM mock_transactions")
    fun clear()
}
