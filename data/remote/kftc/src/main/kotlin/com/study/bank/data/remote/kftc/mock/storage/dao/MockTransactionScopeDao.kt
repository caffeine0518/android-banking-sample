package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.RoomDatabase

/**
 * 여러 테이블에 걸친 작업의 DB 트랜잭션 경계. 거래내역을 다루는 [MockTransactionDao]와는 관계없다.
 *
 * 출금이체와 시드 재적재는 `mock_accounts`·`mock_transactions`·`mock_settled_withdrawals` 세 테이블을
 * 함께 변경하므로 한 테이블 DAO의 `@Transaction`으로는 경계를 정할 수 없다. 그렇다고 호출 측에 DB 전체를
 * 넘기면 `clearAllTables`·`close`까지 노출되므로, 트랜잭션 실행 기능만 DAO로 노출한다.
 *
 * Room은 DAO abstract class에 [RoomDatabase]를 받는 생성자가 있으면 DB 인스턴스를 전달한다.
 * `@Transaction` 생성 코드는 제네릭 반환 타입을 지원하지 않으므로(타입 파라미터 선언을 누락한다)
 * [RoomDatabase.runInTransaction]을 직접 호출한다.
 */
@Dao
internal abstract class MockTransactionScopeDao(private val database: RoomDatabase) {

    fun <T> inTransaction(block: () -> T): T = database.runInTransaction<T>(block)
}
