package com.study.bank.data.remote.kftc.mock.storage.dao

import androidx.room.Dao
import androidx.room.RoomDatabase

/**
 * 여러 테이블에 걸친 조작의 트랜잭션 경계. (거래내역을 다루는 [MockTransactionDao]와는 별개다 —
 * 여기서 말하는 트랜잭션은 DB 트랜잭션이다.)
 *
 * 출금이체와 시드 재적재는 `mock_accounts`·`mock_transactions`·`mock_settled_withdrawals` 세 테이블을
 * 함께 바꾸므로 한 테이블 DAO의 `@Transaction`으로는 경계를 잡을 수 없다. 그렇다고 상태 보유자에게
 * DB 전체를 넘기면 `clearAllTables`·`close`까지 딸려오므로, 경계를 여는 능력만 DAO로 노출한다.
 *
 * Room은 [RoomDatabase]를 받는 생성자가 있으면 DAO abstract class에 DB 인스턴스를 넘겨준다.
 * `@Transaction` 생성 코드는 제네릭 반환 타입을 지원하지 않으므로(타입 파라미터 선언을 빠뜨린다)
 * 직접 [RoomDatabase.runInTransaction]을 호출한다.
 */
@Dao
internal abstract class MockTransactionScopeDao(private val database: RoomDatabase) {

    fun <T> inTransaction(block: () -> T): T = database.runInTransaction<T>(block)
}
