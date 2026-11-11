package lab.neurotrino.app.ui.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.MindState
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes

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
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(20.dp)) {
            Kicker(stringResource(R.string.capture_title))
            Text(stringResource(R.string.capture_title), style = MaterialTheme.typography.headlineMedium)
            Text(mind?.let { stringResource(it.hintRes()) } ?: stringResource(R.string.capture_lede), color = MaterialTheme.colorScheme.onSurfaceVariant)
            MindState.entries.forEach { state ->
                FilterChip(selected = mind == state, onClick = { onMind(state) }, label = { Text(stringResource(state.labelRes())) })
            }
            OutlinedTextField(
                draft,
                onDraft,
                Modifier.fillMaxWidth(),
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
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSaveOpen, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save_and_open))
            }
            OutlinedButton(onClick = onSaveLeave, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save_and_leave))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_cancel)) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun Onboarding(onFinish: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val titles = listOf(R.string.onboard_1_title, R.string.onboard_2_title, R.string.onboard_3_title)
    val bodies = listOf(R.string.onboard_1_body, R.string.onboard_2_body, R.string.onboard_3_body)
    val kickers = listOf(R.string.onboard_1_kicker, R.string.onboard_2_kicker, R.string.onboard_3_kicker)
    LabField {
        Column(Modifier.padding(28.dp)) {
            Spacer(Modifier.height(48.dp))
            Kicker(stringResource(kickers[index]))
            Text(stringResource(titles[index]), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(bodies[index]), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            if (index < 2) {
                Button(onClick = { index++ }) { Text(stringResource(R.string.action_next)) }
            } else {
                Button(onClick = onFinish) { Text(stringResource(R.string.action_enter_lab)) }
            }
            TextButton(onClick = onFinish) { Text(stringResource(R.string.action_skip)) }
        }
    }
}
