package com.study.bank.data.remote.kftc.mock.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao
import com.study.bank.data.remote.kftc.mock.storage.entity.MockDirectionConverter
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.entity.SettledWithdrawal
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionRecord

/**
 * 서버 상태와 클라이언트 캐시가 달라지는 상황을 재현하려고 `:data:local`의 BankDatabase와 분리한다.
 *
 * androidx.sqlite 번들 드라이버는 Robolectric에서 네이티브 로딩에 실패하므로(UnsatisfiedLinkError)
 * 프레임워크 SQLite를 쓴다.
 */
@Database(
    entities = [SeedAccount::class, TransactionRecord::class, SettledWithdrawal::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(MockDirectionConverter::class)
internal abstract class MockKftcDatabase : RoomDatabase() {

    abstract fun transactionScopeDao(): MockTransactionScopeDao
    abstract fun accountDao(): MockAccountDao
    abstract fun transactionDao(): MockTransactionDao
    abstract fun withdrawalDao(): MockWithdrawalDao

    companion object {
        /**
         * ponytail: Hilt가 메인 스레드에서 생성하면 시드 적재도 메인 스레드에서 실행되므로 메인 스레드 쿼리를 허용한다.
         * 시드 적재가 체감될 만큼 느려지면 백그라운드 초기화로 옮긴다.
         */
        fun inMemory(context: Context): MockKftcDatabase =
            Room.inMemoryDatabaseBuilder(context, MockKftcDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
