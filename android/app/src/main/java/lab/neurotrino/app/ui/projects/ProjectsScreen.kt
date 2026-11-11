package lab.neurotrino.app.ui.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
// TextButton used in detail screen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.ui.components.CaptureWell
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.labelRes

@Composable
fun ProjectsScreen(projects: List<Project>, onOpen: (String) -> Unit, onCreate: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Kicker(stringResource(R.string.projects_kicker))
            Text(stringResource(R.string.projects_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.projects_lede), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            CaptureWell {
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.projects_name)) })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(purpose, { purpose = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.projects_purpose)) })
                Spacer(Modifier.height(8.dp))
                Button(onClick = { onCreate(title, purpose); title = ""; purpose = "" }, enabled = title.isNotBlank()) {
                    Text(stringResource(R.string.action_create_project))
                }
            }
            Spacer(Modifier.height(16.dp))
            if (projects.isEmpty()) {
                Text(stringResource(R.string.projects_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            projects.sortedByDescending { it.updatedAt }.forEach { project ->
                InstrumentPanel(Modifier.padding(vertical = 6.dp).clickable { onOpen(project.id) }) {
                    Text(project.title, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(project.status.labelRes()), color = MaterialTheme.colorScheme.secondary)
                    Text(project.purpose.ifBlank { project.nextStep }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (project.isSample) Text(stringResource(R.string.projects_sample), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(80.dp))
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
    onOpenThought: (String) -> Unit,
    onDelete: () -> Unit,
    onCapture: () -> Unit,
) {
    if (project == null) {
        LabField { Text(stringResource(R.string.projects_missing), Modifier.padding(24.dp)) }
        return
    }
    val linked = thoughts.filter { project.id in it.projectIds }
    val others = thoughts.filter { project.id !in it.projectIds }
    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            if (project.isSample) Kicker(stringResource(R.string.projects_sample))
            OutlinedTextField(project.title, { onChange(project.copy(title = it), "Zmiana nazwy") }, Modifier.fillMaxWidth())
            OutlinedTextField(project.purpose, { onChange(project.copy(purpose = it), null) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.projects_purpose)) })
            OutlinedTextField(project.description, { onChange(project.copy(description = it), null) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.projects_notes)) }, minLines = 3)
            OutlinedTextField(project.nextStep, { onChange(project.copy(nextStep = it), "Nowy mały krok") }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.projects_next)) })
            ProjectStatus.entries.forEach { st ->
                FilterChip(selected = project.status == st, onClick = { onChange(project.copy(status = st), "Zmiana stanu") }, label = { Text(stringResource(st.labelRes())) })
            }
            Button(onClick = onCapture) { Text(stringResource(R.string.action_capture)) }
            Kicker(stringResource(R.string.projects_thoughts))
            linked.forEach { t ->
                TextButton(onClick = { onOpenThought(t.id) }) { Text(t.title.ifBlank { stringResource(R.string.thoughts_untitled) }) }
            }
            Kicker(stringResource(R.string.projects_link_existing))
            others.take(20).forEach { t ->
                TextButton(onClick = { onAttach(t.id) }) { Text(t.title.ifBlank { t.id }) }
            }
            Kicker(stringResource(R.string.projects_history))
            project.history.forEach { Text("${it.at.take(16)} — ${it.note}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(80.dp))
        }
    }
}
