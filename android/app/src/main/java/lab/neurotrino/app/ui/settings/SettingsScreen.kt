package lab.neurotrino.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.AiMode
import lab.neurotrino.app.domain.UserProfile
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.labelRes
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun SettingsScreen(
    profile: UserProfile,
    onChange: (UserProfile) -> Unit,
    onExport: suspend () -> String,
    onImport: suspend (String, Boolean) -> Pair<Int, Int>,
    onWriteExport: (Uri, String) -> Unit,
    onReadImport: suspend (Uri) -> String,
    onRemoveSamples: () -> Unit,
    onWipe: () -> Unit,
    onLibrary: () -> Unit,
) {
    val interests = listOf(
        "neurobiologia", "neurochemia", "percepcja", "świadomość", "fizyka kwantowa",
        "kosmologia", "biologia", "inżynieria", "filozofia", "epistemologia", "nauka i wiara",
    )
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val json = onExport()
            onWriteExport(uri, json)
            message = "Zapis wyeksportowany."
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            runCatching {
                val raw = onReadImport(uri)
                onImport(raw, true)
            }.onSuccess { (t, p) -> message = "Wczytano $t myśli i $p projektów." }
                .onFailure { message = it.message }
        }
    }

    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Kicker(stringResource(R.string.settings_kicker))
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.settings_lede), color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onLibrary) { Text(stringResource(R.string.nav_library)) }

            InstrumentPanel {
                Kicker(stringResource(R.string.settings_about))
                OutlinedTextField(profile.displayName, { onChange(profile.copy(displayName = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.settings_name)) })
                OutlinedTextField(profile.currentFocus, { onChange(profile.copy(currentFocus = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.settings_focus)) })
                OutlinedTextField(profile.notesToSelf, { onChange(profile.copy(notesToSelf = it)) }, Modifier.fillMaxWidth(), minLines = 3, label = { Text(stringResource(R.string.settings_note)) })
                Text(stringResource(R.string.settings_interests))
                interests.forEach { interest ->
                    val on = interest in profile.interests
                    FilterChip(
                        selected = on,
                        onClick = {
                            onChange(profile.copy(interests = if (on) profile.interests - interest else profile.interests + interest))
                        },
                        label = { Text(interest) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            InstrumentPanel {
                Kicker(stringResource(R.string.settings_look))
                Row {
                    FilterChip(selected = profile.theme == "dark", onClick = { onChange(profile.copy(theme = "dark")) }, label = { Text(stringResource(R.string.action_theme_dark)) })
                    FilterChip(selected = profile.theme == "light", onClick = { onChange(profile.copy(theme = "light")) }, label = { Text(stringResource(R.string.action_theme_light)) })
                }
            }
            Spacer(Modifier.height(12.dp))
            InstrumentPanel {
                Kicker(stringResource(R.string.settings_ai))
                Text(stringResource(R.string.settings_ai_help), color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(profile.gatewayUrl, { onChange(profile.copy(gatewayUrl = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.settings_gateway)) })
                OutlinedTextField(profile.model, { onChange(profile.copy(model = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.settings_model)) })
                AiMode.entries.forEach { mode ->
                    FilterChip(selected = profile.aiDefaultMode == mode, onClick = { onChange(profile.copy(aiDefaultMode = mode)) }, label = { Text(stringResource(mode.labelRes())) })
                }
            }
            Spacer(Modifier.height(12.dp))
            InstrumentPanel {
                Kicker(stringResource(R.string.settings_data))
                Text(stringResource(R.string.settings_export_help), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { exportLauncher.launch("neurotrino-lab-${LocalDate.now()}.json") }) {
                    Text(stringResource(R.string.action_export))
                }
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }) {
                    Text(stringResource(R.string.action_import))
                }
                message?.let { Text(it) }
                Text(stringResource(R.string.settings_backup), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.settings_notifications), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.settings_pwa), color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onRemoveSamples) { Text(stringResource(R.string.action_remove_samples)) }
                OutlinedButton(onClick = onWipe) { Text(stringResource(R.string.action_wipe), color = MaterialTheme.colorScheme.error) }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
