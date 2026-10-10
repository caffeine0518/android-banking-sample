package com.study.bank.data.remote.kftc.mock.storage

import com.study.bank.data.remote.kftc.mock.seed.KftcTransactionSeed
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount

internal fun MockKftcDatabase.seed(accounts: List<SeedAccount>) = transactionScopeDao().inTransaction {
    withdrawalDao().clear()
    transactionDao().clear()
    accountDao().clear()
    accountDao().insertAll(accounts)
    transactionDao().insertAll(KftcTransactionSeed.rows(accounts))
}
