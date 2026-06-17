package com.study.bank.data.di.kftc

/**
 * 테스트가 네트워크 응답을 정상↔장애로 토글하는 seam.
 *
 * 실 구현은 KFTC mock 서버 싱글톤([com.study.bank.data.remote.kftc.mock.KftcMockServer])을 감싸고,
 * data-di가 production 그래프에 바인딩한다. 이 추상화 덕분에 :app(과 그 E2E 테스트)은 mock 구현 모듈
 * (:data:remote:kftc)을 직접 의존하지 않고도 새로고침 실패 같은 장애 경로를 재현할 수 있다.
 */
interface NetworkFaultController {

    /** 이후 모든 요청을 서버 장애(5xx)로 응답하게 한다. */
    fun enableFault()

    /** [enableFault]로 켠 장애 주입을 해제해 정상 응답으로 되돌린다. */
    fun disableFault()
}
