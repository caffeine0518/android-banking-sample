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
 * Mock 서버가 보유한 인메모리 은행 DB. 잔액·거래원장·멱등 기록을 저장한다.
 *
 * 앱 캐시인 `:data:local`의 BankDatabase와 **별개 인스턴스**다. 서버 상태와 클라이언트 캐시가 달라지는 상황
 * (새로고침 실패·동기화 지연)을 재현하려면 분리돼 있어야 한다.
 *
 * 프레임워크 SQLite를 그대로 쓴다. androidx.sqlite 번들 드라이버는 Robolectric 클래스로더에서 네이티브
 * 라이브러리 로딩에 실패하고(UnsatisfiedLinkError), Android 빌더는 어차피 Context가 필요해 이점이 없다.
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
         * mock 전용 인메모리 DB. 디스패처는 자체 스레드에서 동기 호출하지만, Hilt가 메인 스레드에서 DB를
         * 생성하면 시드 적재도 메인 스레드에서 실행되므로 메인 스레드 쿼리를 허용한다.
         *
         * ponytail: 테스트·데모용 mock이라 메인 스레드 쿼리를 허용한다. 실제 앱 DB(:data:local)에는
         * 적용하지 않으며, 시드 적재가 체감될 만큼 느려지면 백그라운드 초기화로 옮긴다.
         */
        fun inMemory(context: Context): MockKftcDatabase =
            Room.inMemoryDatabaseBuilder(context, MockKftcDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
