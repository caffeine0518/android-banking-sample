# LazyColumn / LazyRow 사용 규칙

Compose 지연 목록(LazyColumn·LazyRow·LazyVerticalGrid)을 쓸 때 지켜야 할 규칙.
출처는 문서 하단 참고. 각 규칙 끝의 `[상태]`는 현재 코드베이스 준수 현황.

## 하드 룰 (반드시)

1. **`items()`에 안정적인 key를 준다.** `key = { it.id }`
   - key가 없으면 위치 기반으로 상태를 추적해, 목록이 바뀌면 아이템의 `remember` 상태가
     엉뚱한 위치로 따라간다. 재정렬·삭제·삽입이 가능한 목록에서 특히 필수.
   - key 타입은 `Bundle` 지원 타입(primitive·enum·Parcelable)이어야 한다.
   - `[상태]` Home·Account·Recipient 모두 `key = { it.id }` 적용됨. ✅

2. **같은 방향 스크롤을 중첩하지 않는다.** `Column(verticalScroll)` 안에 높이 미지정
   `LazyColumn`을 넣으면 `IllegalStateException`(무한 높이 제약). 헤더·푸터가 필요하면
   **하나의 LazyColumn**에 `item { }`(헤더) + `items()`(목록)로 합친다.
   - `[상태]` Home·Recipient는 헤더를 `item { }`으로 합쳐 단일 LazyColumn 사용. Account는
     `Column`(verticalScroll 없음) + `weight(1f)` LazyColumn이라 중첩 아님. ✅

3. **무거운 연산을 아이템 본문/composition에서 하지 않는다.** 정렬·필터·매핑은 목록 진입 전
   (ViewModel 등)에서 끝내고, 아이템 본문에서 반복 계산하면 `remember`로 캐싱한다.
   - `[상태]` 필터링은 `RecipientViewModel`·`AccountInputViewModel` 등 도메인/뷰모델에 위치. ✅

## 권장 (성능·정확성)

4. **이종 아이템 목록엔 `contentType`을 준다.** 헤더·구분선·서로 다른 행이 섞인 목록은
   `item(contentType = ...)` / `items(..., contentType = { ... })`로 타입을 알려줘야
   Compose가 **같은 타입끼리만** composition을 재사용한다(스크롤 재사용 효율↑).
   - `[상태]` Home(헤더+계좌), Recipient(타이틀·입력·섹션헤더+계좌)에 적용. 단일 타입인
     Account 거래내역은 불필요(생략). ✅

5. **엣지 여백은 `contentPadding`으로 준다.** LazyColumn 자체에 padding을 주면 콘텐츠가
   여백 영역으로 스크롤되지 못한다. 첫/마지막 아이템 여백은 `contentPadding`이 담당.
   - `[상태]` 세 화면 모두 `contentPadding = PaddingValues(...)` 사용. ✅

6. **아이템 간격은 `verticalArrangement = Arrangement.spacedBy(...)`로 준다.**
   수동 `Spacer`/아이템 padding 누적보다 명확하고 첫/끝 간격이 깔끔하다.

7. **아이템 하나에 여러 독립 요소를 담지 않는다.** 한 `item {}`에 여러 행을 넣으면 한 덩어리로
   취급돼 지연 로딩 이점이 사라지고 `scrollToItem` 인덱스가 어긋난다. (구분선처럼 행에
   종속된 작은 요소는 예외)

8. **0px 아이템을 피한다.** 비동기 이미지 등은 기본 size를 지정해 최초 측정 시 모든 아이템이
   한꺼번에 composition되는 것을 막는다.

9. **스크롤 상태를 composition에서 직접 읽지 않는다.** `firstVisibleItemIndex` 등을 본문에서
   바로 읽으면 스크롤마다 재구성된다. 파생 상태는 `derivedStateOf`, 사이드이펙트(애널리틱스
   등)는 `snapshotFlow`로 처리한다.

## 참고
- [Lists and grids — Android Developers](https://developer.android.com/develop/ui/compose/lists)
- [Follow best practices — Android Developers](https://developer.android.com/develop/ui/compose/performance/bestpractices)
