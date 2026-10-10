package com.study.bank.data.remote.kftc.mock.storage

import com.study.bank.data.remote.kftc.mock.seed.KftcTransactionSeed
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount

/** 거래내역을 오래된 순으로 삽입해 seq가 시간순과 일치하게 한다. 테스트가 초기 상태로 되돌릴 때도 호출한다. */
internal fun MockKftcDatabase.seed(accounts: List<SeedAccount>) = transactionScopeDao().inTransaction {
    withdrawalDao().clear()
    transactionDao().clear()
    accountDao().clear()
    accountDao().insertAll(accounts)
    transactionDao().insertAll(KftcTransactionSeed.rows(accounts))
}
