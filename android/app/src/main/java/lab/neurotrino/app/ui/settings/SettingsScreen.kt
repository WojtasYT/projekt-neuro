package lab.neurotrino.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.AiMode
import lab.neurotrino.app.domain.UserProfile
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.SectionHeader
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Indigo
import kotlinx.coroutines.launch
import java.time.LocalDate

private val INTERESTS = listOf(
    "neurobiologia", "neurochemia", "percepcja", "świadomość", "fizyka kwantowa",
    "kosmologia", "biologia", "ewolucja", "inżynieria", "wynalazczość",
    "filozofia", "epistemologia", "nauka i wiara", "psychologia poznawcza",
)

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
    var message by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var importReplace by remember { mutableStateOf(true) }
    var showWipeConfirm by remember { mutableStateOf(false) }
    var showImportMode by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            runCatching {
                val json = onExport()
                onWriteExport(uri, json)
            }.onSuccess {
                message = true to "Zapis wyeksportowany."
            }.onFailure {
                message = false to "Nie udało się wyeksportować: ${it.message ?: "błąd zapisu"}"
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportMode = true
        }
    }

    if (showImportMode && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportMode = false },
            title = { Text(stringResource(R.string.settings_import_mode)) },
            text = {
                Text(
                    stringResource(
                        R.string.settings_import_confirm,
                        if (importReplace) stringResource(R.string.settings_import_replace_short) else stringResource(R.string.settings_import_merge),
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportMode = false
                    val uri = pendingImportUri
                    if (uri != null) scope.launch {
                        runCatching {
                            val raw = onReadImport(uri)
                            onImport(raw, importReplace)
                        }.onSuccess { (t, p) ->
                            message = true to "Wczytano $t myśli i $p projektów."
                        }.onFailure {
                            message = false to "Nie udało się wczytać: ${it.message ?: "nieprawidłowy plik"}"
                        }
                    }
                }) { Text(stringResource(R.string.action_import)) }
            },
            dismissButton = { TextButton(onClick = { showImportMode = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text(stringResource(R.string.action_wipe)) },
            text = { Text(stringResource(R.string.settings_wipe_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showWipeConfirm = false
                    onWipe()
                    message = true to "Laboratorium wyczyszczone."
                }) { Text(stringResource(R.string.action_wipe), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showWipeConfirm = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Kicker(stringResource(R.string.settings_kicker))
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.settings_lede), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // ── profile ───────────────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.settings_about), kickerColor = Cyan, action = {
                Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            })
            OutlinedTextField(profile.displayName, { onChange(profile.copy(displayName = it)) }, Modifier.fillMaxWidth().padding(top = 6.dp), label = { Text(stringResource(R.string.settings_name)) }, singleLine = true)
            OutlinedTextField(profile.currentFocus, { onChange(profile.copy(currentFocus = it)) }, Modifier.fillMaxWidth().padding(top = 6.dp), label = { Text(stringResource(R.string.settings_focus)) }, minLines = 2)
            OutlinedTextField(profile.notesToSelf, { onChange(profile.copy(notesToSelf = it)) }, Modifier.fillMaxWidth().padding(top = 6.dp), label = { Text(stringResource(R.string.settings_note)) }, minLines = 2)
            Text(stringResource(R.string.settings_interests), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                INTERESTS.forEach { interest ->
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

            // ── appearance ────────────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.settings_look), kickerColor = Indigo, action = {
                Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                FilterChip(selected = profile.theme == "dark", onClick = { onChange(profile.copy(theme = "dark")) }, label = { Text(stringResource(R.string.action_theme_dark)) })
                FilterChip(selected = profile.theme == "light", onClick = { onChange(profile.copy(theme = "light")) }, label = { Text(stringResource(R.string.action_theme_light)) })
            }

            // ── assistant ─────────────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.settings_ai), kickerColor = Indigo, action = {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = Indigo)
            })
            InstrumentPanel(Modifier.padding(top = 6.dp)) {
                Text(stringResource(R.string.settings_ai_help), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(profile.gatewayUrl, { onChange(profile.copy(gatewayUrl = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.settings_gateway)) }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(profile.model, { onChange(profile.copy(model = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.settings_model)) }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (profile.gatewayUrl.isNotBlank()) stringResource(R.string.settings_ai_set) else stringResource(R.string.ai_unconfigured_short),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (profile.gatewayUrl.isNotBlank()) Cyan else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.settings_ai_mode), style = MaterialTheme.typography.labelMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiMode.entries.forEach { mode ->
                        FilterChip(selected = profile.aiDefaultMode == mode, onClick = { onChange(profile.copy(aiDefaultMode = mode)) }, label = { Text(stringResource(mode.labelRes())) })
                    }
                }
            }

            // ── data ──────────────────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.settings_data), kickerColor = Amber)
            InstrumentPanel(Modifier.padding(top = 6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { exportLauncher.launch("neurotrino-lab-${LocalDate.now()}.json") }) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.action_export))
                    }
                    OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.action_import))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = importReplace, onClick = { importReplace = true }, label = { Text(stringResource(R.string.settings_import_replace_short)) })
                    FilterChip(selected = !importReplace, onClick = { importReplace = false }, label = { Text(stringResource(R.string.settings_import_merge)) })
                }
                message?.let { (ok, text) ->
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (ok) Cyan else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.settings_export_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── library shortcut ──────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onLibrary) {
                Icon(Icons.Outlined.LibraryBooks, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.nav_library))
            }

            // ── privacy & misc ────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            InstrumentPanel(accent = Amber.copy(alpha = 0.4f)) {
                Kicker(stringResource(R.string.settings_privacy), Amber)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.CloudOff, contentDescription = null, modifier = Modifier.size(18.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.settings_backup), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(stringResource(R.string.settings_notifications), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                Text(stringResource(R.string.settings_pwa), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRemoveSamples) { Text(stringResource(R.string.action_remove_samples)) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showWipeConfirm = true }) {
                Text(stringResource(R.string.action_wipe), color = MaterialTheme.colorScheme.error)
            }

            // ── about ─────────────────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.settings_about_app), kickerColor = MaterialTheme.colorScheme.onSurfaceVariant, action = {
                Icon(Icons.Outlined.Article, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            })
            Text(
                stringResource(R.string.settings_about_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(110.dp))
        }
    }
}
