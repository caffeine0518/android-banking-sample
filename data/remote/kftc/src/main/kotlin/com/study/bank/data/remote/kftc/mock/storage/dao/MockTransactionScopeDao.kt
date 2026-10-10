package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.RoomDatabase

/**
 * 여러 테이블에 걸친 DB 트랜잭션 경계. 호출 측에 DB 전체를 넘기면 `clearAllTables`·`close`까지 노출되므로
 * 트랜잭션 실행 기능만 DAO로 노출한다.
 *
 * `@Transaction` 생성 코드는 제네릭 반환 타입을 지원하지 않아 [RoomDatabase.runInTransaction]을 직접 호출한다.
 */
@Dao
internal abstract class MockTransactionScopeDao(private val database: RoomDatabase) {

    fun <T> inTransaction(block: () -> T): T = database.runInTransaction<T>(block)
}
