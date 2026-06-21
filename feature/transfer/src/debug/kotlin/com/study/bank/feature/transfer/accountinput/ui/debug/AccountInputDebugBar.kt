package com.study.bank.feature.transfer.accountinput.ui.debug

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.study.bank.domain.model.BankCode

/**
 * 디버그 빌드 전용 해피패스 프리셋.
 *
 * 수동 입력으로도 실명조회는 되지만(숫자 정규화 매칭), 앱이 보여주는 건 마스킹 번호뿐이라
 * 매칭될 전체 계좌번호를 사용자가 알 길이 없다. 디버그에서 월급통장 → 세이프박스(토스뱅크 KRW,
 * 본인 다른 계좌 = SelfTransfer 아님·통화 일치) 해피패스를 한 번에 채워 성공 화면까지 흐르게 한다.
 *
 * 같은 시그니처의 release 소스셋 stub은 아무것도 렌더링하지 않아 프리셋이 릴리스에 새지 않는다.
 */
@Composable
internal fun AccountInputDebugBar(
    onApplyPreset: (accountNumber: String, bank: BankCode) -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = { onApplyPreset(HAPPY_PATH_ACCOUNT_NUMBER, HAPPY_PATH_BANK) },
        label = { Text("🐞 해피패스 계좌 채우기") },
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

// 세이프박스(토스뱅크 KRW). 월급통장 등 KRW 계좌에서 송금을 시작하면 결과 성공까지 이어진다.
// 입력 필드가 숫자만 받으므로 프리셋도 숫자만 — mock이 숫자 정규화로 하이픈 시드와 매칭한다.
private const val HAPPY_PATH_ACCOUNT_NUMBER = "1000551114443"
private val HAPPY_PATH_BANK = BankCode.TOSS
