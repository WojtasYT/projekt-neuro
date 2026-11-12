package lab.neurotrino.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.MindState
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.LabMark
import lab.neurotrino.app.ui.components.transparentTextFieldColors
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.SheetShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(
    draft: String,
    mind: MindState?,
    onDraft: (String) -> Unit,
    onMind: (MindState) -> Unit,
    onSaveOpen: () -> Unit,
    onSaveLeave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Kicker(stringResource(R.string.capture_kicker))
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.capture_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                mind?.let { stringResource(it.hintRes()) } ?: stringResource(R.string.capture_lede),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MindState.entries.forEach { state ->
                    FilterChip(
                        selected = mind == state,
                        onClick = { onMind(state) },
                        label = { Text(stringResource(state.labelRes())) },
                    )
                }
            }
            TextField(
                value = draft,
                onValueChange = onDraft,
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                placeholder = {
                    Text(
                        stringResource(
                            when (mind) {
                                MindState.fog -> R.string.lab_capture_hint_fog
                                MindState.tooMany -> R.string.capture_too_many_hint
                                else -> R.string.lab_capture_hint
                            },
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = transparentTextFieldColors(),
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSaveOpen, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save_and_open))
            }
            OutlinedButton(onClick = onSaveLeave, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(stringResource(R.string.action_save_and_leave))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}

@Composable
fun Onboarding(onFinish: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val pages = listOf(
        Triple(R.string.onboard_1_kicker, R.string.onboard_1_title, R.string.onboard_1_body),
        Triple(R.string.onboard_2_kicker, R.string.onboard_2_title, R.string.onboard_2_body),
        Triple(R.string.onboard_3_kicker, R.string.onboard_3_title, R.string.onboard_3_body),
    )
    val (kicker, title, body) = pages[index]

    LabField {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 40.dp),
        ) {
            Spacer(Modifier.height(28.dp))
            LabMark(size = 52.dp)
            Spacer(Modifier.height(26.dp))
            Kicker(stringResource(kicker))
            Spacer(Modifier.height(10.dp))
            Text(stringResource(title), style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(30.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { i ->
                    val selected = i == index
                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .size(if (selected) 22.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (selected) Cyan else MaterialTheme.colorScheme.outlineVariant),
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
            if (index < pages.lastIndex) {
                Button(onClick = { index++ }) {
                    Text(stringResource(R.string.action_next))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            } else {
                Button(onClick = onFinish) { Text(stringResource(R.string.action_enter_lab)) }
            }
            TextButton(onClick = onFinish, modifier = Modifier.padding(top = 4.dp)) {
                Text(stringResource(R.string.action_skip))
            }
        }
    }
}
