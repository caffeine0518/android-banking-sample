package com.study.bank.data.remote.kftc.mock.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao

/**
 * Mock 서버가 들고 있는 인메모리 은행 DB — 잔액·거래원장·멱등 기록.
 *
 * 앱 캐시인 `:data:local`의 BankDatabase와 **별개 인스턴스**다. 서버 상태와 클라 캐시가 갈라진 상황
 * (새로고침 실패·동기화 지연)을 재현하려면 분리돼 있어야 한다.
 *
 * 프레임워크 SQLite를 그대로 쓴다 — androidx.sqlite 번들 드라이버는 Robolectric 클래스로더에서
 * 네이티브 로딩이 실패하고(UnsatisfiedLinkError), Android 빌더는 어차피 Context를 요구해 이득이 없다.
 */
@Database(
    entities = [SeedAccount::class, TransactionRecord::class, WithdrawResult.Success::class],
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
         * mock 전용 인메모리 DB. 디스패처는 자기 스레드에서 동기 호출하지만, 부팅 시드 적재는 Hilt가
         * 메인 스레드에서 생성할 수 있어 메인 스레드 질의를 허용한다.
         *
         * ponytail: 테스트·데모용 mock이라 메인 스레드 질의를 허용한다. 실제 앱 DB(:data:local)에는
         * 적용하지 않으며, 시드 적재가 체감될 만큼 커지면 그때 백그라운드 초기화로 옮긴다.
         */
        fun inMemory(context: Context): MockKftcDatabase =
            Room.inMemoryDatabaseBuilder(context, MockKftcDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
