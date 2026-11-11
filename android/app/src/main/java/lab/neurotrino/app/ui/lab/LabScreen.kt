package lab.neurotrino.app.ui.lab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.MindState
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.Spark
import lab.neurotrino.app.domain.SparksCatalog
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.lastResumable
import lab.neurotrino.app.ui.components.CaptureWell
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Indigo

@Composable
fun LabScreen(
    thoughts: List<Thought>,
    projects: List<Project>,
    reading: List<ReadingItem>,
    mind: MindState?,
    online: Boolean,
    onMind: (MindState) -> Unit,
    onCapture: (String) -> Unit,
    onOpenCapture: () -> Unit,
    onResume: (String) -> Unit,
    onSpark: () -> Unit,
    onProject: (String) -> Unit,
    onOpenThought: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val resume = lastResumable(thoughts)
    val project = projects.firstOrNull { it.status == ProjectStatus.active || it.status == ProjectStatus.incubating }
    val spark = remember(thoughts.size, reading.size) { SparksCatalog.pick(thoughts, reading, emptySet()) }
    var draft by remember { mutableStateOf("") }
    val recent = thoughts.filter { it.status != lab.neurotrino.app.domain.ThoughtStatus.archived }
        .sortedByDescending { it.updatedAt }
        .take(4)

    LabField {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Kicker(stringResource(R.string.lab_kicker))
                TextButton(onClick = onSettings) { Text(stringResource(R.string.nav_settings)) }
            }
            Text(stringResource(R.string.lab_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.lab_lede), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!online) {
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.lab_offline), color = Amber, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindState.entries.forEach { state ->
                    FilterChip(
                        selected = mind == state,
                        onClick = { onMind(state) },
                        label = { Text(stringResource(state.labelRes())) },
                    )
                }
            }
            mind?.let {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(it.hintRes()), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(18.dp))
            CaptureWell {
                Kicker(stringResource(R.string.lab_capture_label), Cyan)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    placeholder = {
                        Text(
                            stringResource(
                                if (mind == MindState.fog) R.string.lab_capture_hint_fog else R.string.lab_capture_hint,
                            ),
                        )
                    },
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (draft.isBlank()) onOpenCapture() else {
                            onCapture(draft)
                            draft = ""
                        }
                    }) { Text(stringResource(R.string.action_capture)) }
                    OutlinedButton(onClick = { resume?.id?.let(onResume) }, enabled = resume != null) {
                        Text(stringResource(R.string.action_resume_short))
                    }
                    OutlinedButton(onClick = onSpark) { Text(stringResource(R.string.action_spark)) }
                }
            }
            Spacer(Modifier.height(18.dp))
            ThreadPanel(resume, onResume)
            Spacer(Modifier.height(12.dp))
            if (project != null) {
                InstrumentPanel(accent = Amber.copy(alpha = 0.4f)) {
                    Kicker(stringResource(R.string.lab_project_kicker), Amber)
                    Text(project.title, style = MaterialTheme.typography.titleLarge)
                    Text(project.nextStep.ifBlank { project.purpose }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { onProject(project.id) }) { Text(stringResource(R.string.action_open_project)) }
                }
                Spacer(Modifier.height(12.dp))
            }
            SparkPanel(spark, onSpark)
            Spacer(Modifier.height(20.dp))
            Kicker(stringResource(R.string.lab_recent), Indigo)
            Spacer(Modifier.height(8.dp))
            recent.forEach { thought ->
                TextButton(onClick = { onOpenThought(thought.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(thought.title.ifBlank { stringResource(R.string.thoughts_untitled) }, style = MaterialTheme.typography.titleMedium)
                        Text(
                            (thought.content.ifBlank { thought.rawText }).take(120),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
            }
            Spacer(Modifier.height(88.dp))
        }
    }
}

@Composable
private fun ThreadPanel(resume: Thought?, onResume: (String) -> Unit) {
    InstrumentPanel {
        Kicker(stringResource(R.string.lab_thread_kicker))
        if (resume == null) {
            Text(stringResource(R.string.lab_thread_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(resume.title.ifBlank { stringResource(R.string.thoughts_untitled) }, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                (resume.content.ifBlank { resume.rawText }).take(180),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { onResume(resume.id) }) { Text(stringResource(R.string.thoughts_open)) }
        }
    }
}

@Composable
private fun SparkPanel(spark: Spark, onSpark: () -> Unit) {
    InstrumentPanel(accent = Indigo.copy(alpha = 0.45f)) {
        Kicker(stringResource(R.string.lab_spark_kicker), Indigo)
        Text(spark.title, style = MaterialTheme.typography.titleLarge)
        Text(spark.hook, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (spark.why.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(spark.why, style = MaterialTheme.typography.bodyMedium, color = Amber)
        }
        TextButton(onClick = onSpark) { Text(stringResource(R.string.action_spark)) }
    }
}
