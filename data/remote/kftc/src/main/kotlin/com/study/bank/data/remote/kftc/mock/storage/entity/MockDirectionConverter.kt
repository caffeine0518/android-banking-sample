package com.study.bank.data.remote.kftc.mock.storage.entity

import androidx.room.TypeConverter

internal class MockDirectionConverter {

    @TypeConverter
    fun toColumn(direction: TransactionDirection): String = direction.name

    @TypeConverter
    fun fromColumn(value: String): TransactionDirection = TransactionDirection.valueOf(value)
}
