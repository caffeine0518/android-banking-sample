package com.study.bank.feature.transfer.amount.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.study.bank.core.ui.model.format
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.amount.ui.model.AmountRecipientUi
import com.study.bank.feature.transfer.amount.ui.model.AmountSourceUi
import com.study.bank.feature.transfer.recipient.ui.model.label

/**
 * 출금계좌(FROM)와 수취계좌(TO)를 위·아래로 보여주는 헤더.
 * 조사("에서"/"로") 대신 언어 중립적인 FROM/TO 캡션으로 방향을 표시한다.
 */
@Composable
internal fun TransferPartyHeader(
    source: AmountSourceUi?,
    recipient: AmountRecipientUi?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        source?.let {
            SourceParty(it)
            Spacer(Modifier.height(20.dp))
        }
        recipient?.let { RecipientParty(it) }
    }
}

@Composable
private fun SourceParty(source: AmountSourceUi) {
    PartyLabel(stringResource(R.string.transfer_amount_from_label))
    PartyName(source.nickname ?: source.type.label())
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.transfer_amount_balance_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(text = source.balance.format(), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RecipientParty(recipient: AmountRecipientUi) {
    PartyLabel(stringResource(R.string.transfer_amount_to_label))
    PartyName(recipient.holderName)
    Text(
        text = stringResource(
            R.string.transfer_account_subtitle,
            recipient.bankDisplayName,
            recipient.accountNumber,
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** FROM/TO 캡션. 작고 흐린 대문자 라벨로 본문 위에 얹는다. */
@Composable
private fun PartyLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(2.dp))
}

@Composable
private fun PartyName(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
