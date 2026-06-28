package com.study.bank.data.remote.kftc.api

/**
 * KFTC 거래내역조회 한 페이지 크기(서버가 정함 — 클라가 못 정함). 단일 소유처.
 *
 * mock 서버가 이 크기로 페이지를 끊고, 클라의 PagingConfig도 이 값에 맞춰(pageSize=initialLoadSize) 첫 화면을
 * 1왕복으로 채운다. 여러 곳에 흩어진 매직넘버(20)를 한 곳에 모아 서버·클라 페이지 크기가 조용히 어긋나지 않게 한다.
 */
const val KFTC_TRANSACTION_PAGE_SIZE = 20
