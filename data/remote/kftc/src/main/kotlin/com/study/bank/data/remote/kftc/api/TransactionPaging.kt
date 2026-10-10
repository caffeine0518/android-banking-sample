package com.study.bank.data.remote.kftc.api

/**
 * KFTC 거래내역조회의 페이지 크기. 실제 KFTC에서는 서버가 정하고 클라이언트는 지정할 수 없다.
 *
 * mock 서버가 이 크기로 페이지를 나누고, 클라이언트의 PagingConfig도 같은 값(pageSize = initialLoadSize)을 써서
 * 첫 화면을 요청 한 번으로 불러온다. 서버와 클라이언트의 페이지 크기가 어긋나지 않도록 이 상수 하나로 관리한다.
 */
const val KFTC_TRANSACTION_PAGE_SIZE = 20
