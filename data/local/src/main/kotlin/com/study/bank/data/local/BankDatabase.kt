package com.study.bank.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.study.bank.data.local.dao.AccountDao
import com.study.bank.data.local.entity.AccountEntity

@Database(
    entities = [AccountEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BankDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}
