package com.study.bank.data.remote.kftc.mock.storage

import com.study.bank.data.remote.kftc.mock.seed.KftcTransactionSeed

/**
 * 세 테이블을 비우고 시드(계좌 + 과거 거래내역)를 적재한다.
 *
 * 부팅 시 DB 프로바이더가 한 번 호출하고, 테스트가 초기 상태로 되돌릴 때 다시 호출한다.
 * 거래내역은 **오래된 순**으로 들어가므로 SQLite가 부여하는 seq가 시간순과 일치한다.
 */
internal fun MockKftcDatabase.seed(accounts: List<SeedAccount>) = transactionScopeDao().inTransaction {
    withdrawalDao().clear()
    transactionDao().clear()
    accountDao().clear()
    accountDao().insertAll(accounts)
    transactionDao().insertAll(KftcTransactionSeed.rows(accounts))
}
