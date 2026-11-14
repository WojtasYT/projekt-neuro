package lab.neurotrino.app.ui.projects

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.relativeTimePl
import lab.neurotrino.app.ui.components.CaptureWell
import lab.neurotrino.app.ui.components.EmptyState
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabBadge
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.PressablePanel
import lab.neurotrino.app.ui.components.SectionHeader
import lab.neurotrino.app.ui.components.transparentTextFieldColors
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Amber

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    onOpen: (String) -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }

    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Kicker(stringResource(R.string.projects_kicker))
                    Text(stringResource(R.string.projects_title), style = MaterialTheme.typography.headlineLarge)
                }
                IconButton(onClick = { showCreate = !showCreate }) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.action_create_project))
                }
            }
            Text(stringResource(R.string.projects_lede), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (showCreate) {
                CaptureWell(Modifier.padding(top = 12.dp)) {
                    Kicker(stringResource(R.string.action_create_project), Amber)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.projects_name)) }, singleLine = true)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(purpose, { purpose = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.projects_purpose)) }, minLines = 2)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title.trim(), purpose.trim())
                                title = ""; purpose = ""; showCreate = false
                            }
                        }, enabled = title.isNotBlank()) { Text(stringResource(R.string.action_create_project)) }
                        TextButton(onClick = { showCreate = false; title = ""; purpose = "" }) { Text(stringResource(R.string.action_cancel)) }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            if (projects.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.projects_empty_title),
                    body = stringResource(R.string.projects_empty),
                    actionLabel = stringResource(R.string.action_create_project),
                    onAction = { showCreate = true },
                )
            } else {
                projects.sortedByDescending { it.updatedAt }.forEach { project ->
                    PressablePanel(onClick = { onOpen(project.id) }, Modifier.padding(vertical = 6.dp), accent = Amber.copy(alpha = 0.35f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                project.title,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(relativeTimePl(project.updatedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(project.status.labelRes()),
                            style = MaterialTheme.typography.labelMedium,
                            color = Amber,
                        )
                        if (project.purpose.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                project.purpose,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (project.isSample) {
                            Spacer(Modifier.height(6.dp))
                            LabBadge(stringResource(R.string.projects_sample), Amber)
                        }
                    }
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
fun ProjectDetailScreen(
    project: Project?,
    thoughts: List<Thought>,
    onBack: () -> Unit,
    onChange: (Project, String?) -> Unit,
    onAttach: (String) -> Unit,
    onDetach: (String) -> Unit,
    onOpenThought: (String) -> Unit,
    onDelete: () -> Unit,
    onCapture: () -> Unit,
) {
    if (project == null) {
        LabField {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.projects_missing), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }
        }
        return
    }
    var title by remember(project.id) { mutableStateOf(project.title) }
    var purpose by remember(project.id) { mutableStateOf(project.purpose) }
    var description by remember(project.id) { mutableStateOf(project.description) }
    var nextStep by remember(project.id) { mutableStateOf(project.nextStep) }
    var historyDraft by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    val linked = thoughts.filter { project.id in it.projectIds }
    val others = thoughts.filter { project.id !in it.projectIds && it.status != ThoughtStatus.archived }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.projects_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
                if (project.isSample) LabBadge(stringResource(R.string.projects_sample), Amber)
            }
            androidx.compose.material3.TextField(
                value = title,
                onValueChange = { title = it; onChange(project.copy(title = it), null) },
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = transparentTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.projects_name), style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))) },
            )
            OutlinedTextField(purpose, { purpose = it; onChange(project.copy(purpose = it), null) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.projects_purpose)) }, minLines = 2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(description, { description = it; onChange(project.copy(description = it), null) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.projects_notes)) }, minLines = 4)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(nextStep, { nextStep = it; onChange(project.copy(nextStep = it), null) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.projects_next)) })

            Spacer(Modifier.height(10.dp))
            Kicker(stringResource(R.string.studio_status))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProjectStatus.entries.forEach { st ->
                    val stLabel = stringResource(st.labelRes())
                    FilterChip(
                        selected = project.status == st,
                        onClick = { onChange(project.copy(status = st), "Zmiana stanu: $stLabel") },
                        label = { Text(stLabel) },
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Button(onClick = onCapture) {
                Icon(Icons.Outlined.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.action_capture))
            }

            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.projects_thoughts), kickerColor = Amber)
            if (linked.isEmpty()) {
                Text(stringResource(R.string.projects_thoughts_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                linked.forEach { t ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onOpenThought(t.id) }, modifier = Modifier.weight(1f)) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(t.title.ifBlank { stringResource(R.string.thoughts_untitled) }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text((t.content.ifBlank { t.rawText }).take(80), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        TextButton(onClick = { onDetach(t.id) }) { Text(stringResource(R.string.action_detach)) }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            SectionHeader(stringResource(R.string.projects_link_existing))
            if (others.isEmpty()) {
                Text(stringResource(R.string.projects_no_others), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                others.take(15).forEach { t ->
                    TextButton(onClick = { onAttach(t.id) }) {
                        Text("+  ${t.title.ifBlank { stringResource(R.string.thoughts_untitled) }}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            SectionHeader(stringResource(R.string.projects_history))
            if (project.history.isEmpty()) {
                Text(stringResource(R.string.projects_history_empty), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                project.history.takeLast(12).reversed().forEach { note ->
                    Text(
                        "${note.at.take(10)} — ${note.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 1.dp),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                OutlinedTextField(historyDraft, { historyDraft = it }, Modifier.weight(1f), placeholder = { Text(stringResource(R.string.projects_history_hint)) }, singleLine = true)
                OutlinedButton(onClick = {
                    if (historyDraft.isNotBlank()) {
                        onChange(project, historyDraft.trim())
                        historyDraft = ""
                    }
                }, enabled = historyDraft.isNotBlank()) { Text(stringResource(R.string.action_add_note)) }
            }

            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}
