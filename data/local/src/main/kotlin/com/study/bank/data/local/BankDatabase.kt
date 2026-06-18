package com.study.bank.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.study.bank.data.local.dao.AccountDao
import com.study.bank.data.local.dao.TransactionDao
import com.study.bank.data.local.entity.AccountEntity
import com.study.bank.data.local.entity.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class BankDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
}
