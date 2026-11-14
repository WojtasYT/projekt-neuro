package lab.neurotrino.app.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.MindState
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.Spark
import lab.neurotrino.app.domain.SparksCatalog
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.lastResumable
import lab.neurotrino.app.domain.relativeTimePl
import lab.neurotrino.app.ui.components.CaptureWell
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.LabMark
import lab.neurotrino.app.ui.components.SectionHeader
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.components.transparentTextFieldColors
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
    displayName: String,
    onMind: (MindState) -> Unit,
    onCapture: (String) -> Unit,
    onOpenCapture: () -> Unit,
    onResume: (String) -> Unit,
    onSpark: () -> Unit,
    onProject: (String) -> Unit,
    onOpenThought: (String) -> Unit,
    onSettings: () -> Unit,
    onLibrary: () -> Unit,
    onAssistant: () -> Unit,
) {
    val resume = lastResumable(thoughts)
    val project = projects.firstOrNull { it.status == ProjectStatus.active || it.status == ProjectStatus.incubating }
    val spark = remember(thoughts.size, reading.size) { SparksCatalog.pick(thoughts, reading, emptySet()) }
    var draft by remember { mutableStateOf("") }
    val recent = thoughts.filter { it.status != ThoughtStatus.archived }
        .sortedByDescending { it.updatedAt }
        .take(6)

    LabField {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── header: wordmark + actions ─────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabMark(size = 34.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("NEUROTRINO LAB", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.app_mark_subtitle), style = MaterialTheme.typography.labelSmall, color = Cyan)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onAssistant) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = stringResource(R.string.assistant_title))
                }
                IconButton(onClick = onLibrary) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = stringResource(R.string.nav_library))
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.nav_settings))
                }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    if (displayName.isNotBlank()) stringResource(R.string.lab_greeting_named, displayName)
                    else stringResource(R.string.lab_greeting),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.lab_title),
                    style = MaterialTheme.typography.displayLarge,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.lab_lede),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!online) {
                Row(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Amber.copy(alpha = 0.12f))
                        .border(1.dp, Amber.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.lab_offline), style = MaterialTheme.typography.bodySmall, color = Amber)
                }
            }

            // ── capture well ───────────────────────────────────────────────────
            CaptureWell(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Kicker(stringResource(R.string.lab_capture_label), Cyan)
                TextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    minLines = 4,
                    placeholder = {
                        Text(
                            stringResource(
                                if (mind == MindState.fog) R.string.lab_capture_hint_fog else R.string.lab_capture_hint,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = transparentTextFieldColors(),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                // mind chips — compact, optional, never a gate
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 12.dp),
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
                mind?.let {
                    Text(
                        stringResource(it.hintRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // ── resume hero ────────────────────────────────────────────────────
            if (resume != null) {
                InstrumentPanel(
                    Modifier.padding(horizontal = 20.dp),
                    accent = Cyan.copy(alpha = 0.5f),
                    strong = true,
                ) {
                    Kicker(stringResource(R.string.lab_thread_kicker), Cyan)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        resume.title.ifBlank { stringResource(R.string.thoughts_untitled) },
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        (resume.content.ifBlank { resume.rawText }).take(220),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            stringResource(R.string.lab_thread_touched, relativeTimePl(resume.lastOpenedAt)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = { onResume(resume.id) }) {
                            Text(stringResource(R.string.action_resume_short))
                            Icon(Icons.Outlined.NorthEast, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── active project ─────────────────────────────────────────────────
            if (project != null) {
                InstrumentPanel(Modifier.padding(horizontal = 20.dp), accent = Amber.copy(alpha = 0.45f)) {
                    Kicker(stringResource(R.string.lab_project_kicker), Amber)
                    Spacer(Modifier.height(6.dp))
                    Text(project.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        project.nextStep.ifBlank { project.purpose },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = { onProject(project.id) }, modifier = Modifier.padding(top = 2.dp)) {
                        Text(stringResource(R.string.action_open_project))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── spark ──────────────────────────────────────────────────────────
            SparkPanel(spark, onSpark, Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(18.dp))

            // ── recent thoughts ────────────────────────────────────────────────
            SectionHeader(
                stringResource(R.string.lab_recent),
                Modifier.padding(horizontal = 20.dp),
                kickerColor = Indigo,
            )
            Spacer(Modifier.height(8.dp))
            if (recent.isEmpty()) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        stringResource(R.string.lab_recent_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    recent.forEach { thought ->
                        RecentThoughtCard(thought, onOpenThought)
                    }
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun SparkPanel(spark: Spark, onSpark: () -> Unit, modifier: Modifier = Modifier) {
    InstrumentPanel(modifier, accent = Indigo.copy(alpha = 0.5f)) {
        Kicker(stringResource(R.string.lab_spark_kicker), Indigo)
        Spacer(Modifier.height(6.dp))
        Text(spark.title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(2.dp))
        Text(spark.hook, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (spark.why.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(spark.why, style = MaterialTheme.typography.bodySmall, color = Amber)
        }
        TextButton(onClick = onSpark, modifier = Modifier.padding(top = 2.dp)) { Text(stringResource(R.string.action_spark)) }
    }
}

@Composable
private fun RecentThoughtCard(thought: Thought, onOpen: (String) -> Unit) {
    Column(
        Modifier
            .width(190.dp)
            .height(128.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable { onOpen(thought.id) }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(thought.status.dotColor()),
            )
            Text(
                stringResource(thought.status.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            thought.title.ifBlank { stringResource(R.string.thoughts_untitled) },
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        Text(
            relativeTimePl(thought.updatedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun ThoughtStatus.dotColor(): androidx.compose.ui.graphics.Color = when (this) {
    ThoughtStatus.seed -> Amber
    ThoughtStatus.exploring -> Cyan
    ThoughtStatus.paused -> Amber.copy(alpha = 0.7f)
    ThoughtStatus.connected -> Indigo
    ThoughtStatus.checking -> Amber
    ThoughtStatus.developed -> androidx.compose.ui.graphics.Color(0xFF8BD9A9)
    ThoughtStatus.archived -> androidx.compose.ui.graphics.Color(0xFF8B8577)
}
