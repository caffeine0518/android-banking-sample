package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.corebanking.dto.CoreAccountDto
import com.study.bank.data.remote.kftc.corebanking.dto.CoreAccountListResponse
import com.study.bank.data.remote.kftc.mock.http.response.ok
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import okhttp3.mockwebserver.MockResponse

/**
 * 자행 계정계를 모사한다. 계정계에는 자행 계좌만 있다.
 * 앱의 계좌 id 가 핀테크이용번호라 mock 은 핀테크이용번호를 계좌 식별자로 쓴다.
 */
internal class CoreBankingRequestHandler(private val accountDao: MockAccountDao) {

    fun accounts(): MockResponse = CoreAccountListResponse(
        accounts = accountDao.findAll()
            .filter { it.bankCodeStd == OWN_BANK_CODE }
            .map { CoreAccountDto(fintechUseNum = it.fintechUseNum, accountNum = it.accountNum) },
    ).ok()

    private companion object {
        const val OWN_BANK_CODE = "092"
    }
}
