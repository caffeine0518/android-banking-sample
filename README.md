# Bank

다통화 계좌 조회·환율 환산·송금을 다루는 Android 샘플 앱

<p align="center">
  <img src="docs/assets/e2e-transfer.gif" width="280" alt="송금 플로우 E2E" />
</p>

## 기능

- 홈: 다통화 계좌 목록, 표시 통화 기준 총자산 환산 (환율이 없는 통화는 따로 표시)
- 계좌 상세: 거래내역
- 송금: 수취인 → 금액 → 확인 → 결과. 통화가 다른 계좌 간 송금은 거절

## 실행

```bash
./gradlew :app:assembleDebug
./gradlew test                              # JVM 테스트
./gradlew :app-e2e:connectedAndroidTest     # 계기 E2E (에뮬레이터 필요)
```

## 구조

Clean Architecture 멀티모듈 + MVI.

<!-- 아래 트리는 main 머지 시 CI(.github/workflows/module-graph.yml)가 `./gradlew updateModuleGraph`로 생성한다. 직접 수정하지 않는다. -->
<!-- module-graph:start -->
```
:app
├── :navigation
│   ├── :feature:home
│   │   ├── :core-ui:mvi
│   │   └── :core-ui:mapper
│   │       ├── :domain
│   │       └── :core-ui:model
│   ├── :feature:account
│   │   ├── :core-ui:mvi
│   │   └── :core-ui:mapper (*)
│   └── :feature:transfer
│       ├── :core-ui:mvi
│       └── :core-ui:mapper (*)
├── :data-di
│   └── :data
│       ├── :domain
│       ├── :data:remote:kftc
│       ├── :data:remote:fx
│       └── :data:local
└── :core-ui:designsystem
```
<!-- module-graph:end -->

## 라이선스

[MIT](LICENSE)
