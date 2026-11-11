package lab.neurotrino.app.ui.studio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.data.ai.AiGateway
import lab.neurotrino.app.domain.AiMode
import lab.neurotrino.app.domain.AttachmentMeta
import lab.neurotrino.app.domain.GraphKind
import lab.neurotrino.app.domain.HypothesisFields
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.Relationship
import lab.neurotrino.app.domain.SparksCatalog
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Indigo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudioScreen(
    thought: Thought?,
    missing: Boolean,
    projects: List<Project>,
    allThoughts: List<Thought>,
    relationships: List<Relationship>,
    attachments: List<AttachmentMeta>,
    gatewayConfigured: Boolean,
    online: Boolean,
    onBack: () -> Unit,
    onChange: (Thought) -> Unit,
    onTouch: () -> Unit,
    onDelete: () -> Unit,
    onExpand: (String, Provenance) -> Unit,
    onLink: (String, RelationType, String) -> Unit,
    onUnlink: (String) -> Unit,
    onPickImage: (Uri, String, String, Long) -> Unit,
    onAskAi: suspend (AiMode, Thought) -> Result<String>,
    onOpenThought: (String) -> Unit,
) {
    if (thought == null && !missing) {
        LabField { Text(stringResource(R.string.loading), Modifier.padding(24.dp)) }
        return
    }
    if (thought == null) {
        LabField {
            Column(Modifier.padding(24.dp)) {
                Text(stringResource(R.string.thoughts_missing))
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }
        }
        return
    }
    val current = thought
    var title by remember(current.id) { mutableStateOf(current.title) }
    var content by remember(current.id) { mutableStateOf(current.content) }
    var saveLabel by remember { mutableStateOf(R.string.studio_save_idle) }
    var showRaw by remember { mutableStateOf(false) }
    var showHypo by remember { mutableStateOf(current.hypothesis != null) }
    var expansion by remember { mutableStateOf("") }
    var tagDraft by remember { mutableStateOf("") }
    var relTarget by remember { mutableStateOf("") }
    var relType by remember { mutableStateOf(RelationType.extends) }
    var relWhy by remember { mutableStateOf("") }
    var aiMode by remember { mutableStateOf(AiMode.iskra) }
    var consent by remember { mutableStateOf(false) }
    var aiText by remember { mutableStateOf<String?>(null) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var hypo by remember(current.id) { mutableStateOf(current.hypothesis ?: HypothesisFields()) }
    val scope = rememberCoroutineScope()
    var saveJob by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(current.id) { onTouch() }

    fun scheduleSave(nextTitle: String, nextContent: String) {
        saveJob?.cancel()
        saveJob = scope.launch {
            saveLabel = R.string.studio_saving
            delay(450)
            onChange(current.copy(title = nextTitle, content = nextContent))
            saveLabel = R.string.studio_saved
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPickImage(uri, uri.lastPathSegment ?: "obraz", "image/*", 0L)
    }
    val linked = relationships.filter { it.fromId == current.id || it.toId == current.id }

    LabField {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Text(stringResource(saveLabel), style = MaterialTheme.typography.labelMedium)
            }
            OutlinedTextField(title, {
                title = it
                scheduleSave(it, content)
            }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.studio_title_hint)) })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(content, {
                content = it
                scheduleSave(title, it)
            }, Modifier.fillMaxWidth(), minLines = 8, placeholder = { Text(stringResource(R.string.studio_body_hint)) })
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ThoughtType.entries.take(6).forEach { type ->
                    FilterChip(
                        selected = type in current.types,
                        onClick = {
                            val next = if (type in current.types) current.types - type else current.types + type
                            onChange(current.copy(types = next))
                        },
                        label = { Text(stringResource(type.labelRes())) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.studio_status), style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ThoughtStatus.entries.forEach { st ->
                    FilterChip(selected = current.status == st, onClick = { onChange(current.copy(status = st)) }, label = { Text(stringResource(st.labelRes())) })
                }
            }
            Text(stringResource(R.string.studio_project), style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = current.projectIds.isEmpty(), onClick = { onChange(current.copy(projectIds = emptyList())) }, label = { Text(stringResource(R.string.studio_no_project)) })
                projects.forEach { p ->
                    FilterChip(
                        selected = p.id in current.projectIds,
                        onClick = { onChange(current.copy(projectIds = listOf(p.id))) },
                        label = { Text(p.title) },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(tagDraft, { tagDraft = it }, placeholder = { Text(stringResource(R.string.studio_tag_hint)) })
                TextButton(onClick = {
                    val tag = tagDraft.trim().lowercase()
                    if (tag.isNotEmpty() && tag !in current.tags) {
                        onChange(current.copy(tags = current.tags + tag))
                        tagDraft = ""
                    }
                }) { Text("+") }
            }
            current.tags.forEach { tag ->
                FilterChip(selected = true, onClick = { onChange(current.copy(tags = current.tags - tag)) }, label = { Text("#$tag") })
            }

            TextButton(onClick = { showRaw = !showRaw }) {
                Text(stringResource(if (showRaw) R.string.studio_raw_hide else R.string.studio_raw_show))
            }
            if (showRaw) {
                InstrumentPanel {
                    Kicker(stringResource(R.string.studio_raw))
                    Text(current.rawText.ifBlank { "—" })
                }
            }
            TextButton(onClick = { showHypo = !showHypo }) {
                Text(stringResource(if (showHypo) R.string.studio_hypothesis_hide else R.string.studio_hypothesis))
            }
            if (showHypo) {
                Text(stringResource(R.string.studio_hypothesis_help), color = MaterialTheme.colorScheme.onSurfaceVariant)
                HypoField(R.string.hypo_observation, hypo.observation) { hypo = hypo.copy(observation = it); onChange(current.copy(hypothesis = hypo)) }
                HypoField(R.string.hypo_proposal, hypo.proposal) { hypo = hypo.copy(proposal = it); onChange(current.copy(hypothesis = hypo)) }
                HypoField(R.string.hypo_mechanism, hypo.mechanism) { hypo = hypo.copy(mechanism = it); onChange(current.copy(hypothesis = hypo)) }
                HypoField(R.string.hypo_assumptions, hypo.assumptions) { hypo = hypo.copy(assumptions = it); onChange(current.copy(hypothesis = hypo)) }
                HypoField(R.string.hypo_next, hypo.nextStep) { hypo = hypo.copy(nextStep = it); onChange(current.copy(hypothesis = hypo)) }
            }

            Spacer(Modifier.height(12.dp))
            Kicker(stringResource(R.string.studio_expansions))
            Text(stringResource(R.string.studio_expansions_help), color = MaterialTheme.colorScheme.onSurfaceVariant)
            current.expansions.forEach { item ->
                InstrumentPanel(Modifier.padding(vertical = 6.dp)) {
                    Kicker(stringResource(item.provenance.labelRes()), if (item.provenance == Provenance.ai) Indigo else Amber)
                    Text(item.text)
                }
            }
            OutlinedTextField(expansion, { expansion = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.studio_expansion_hint)) })
            Button(onClick = { onExpand(expansion, Provenance.user); expansion = "" }, enabled = expansion.isNotBlank()) {
                Text(stringResource(R.string.studio_add_expansion))
            }

            Spacer(Modifier.height(12.dp))
            Kicker(stringResource(R.string.studio_images))
            OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Text(stringResource(R.string.action_pick_image))
            }
            attachments.forEach { att ->
                Text("${att.name} · ${att.uri.take(48)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
            InstrumentPanel {
                Kicker(stringResource(R.string.studio_local_impulse))
                Text(SparksCatalog.localImpulse(current.types, current.status, current.hypothesis != null))
                Text(stringResource(R.string.studio_local_impulse_note), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))
            InstrumentPanel {
                Kicker(stringResource(R.string.settings_ai))
                when {
                    !gatewayConfigured -> Text(stringResource(R.string.ai_unconfigured))
                    !online -> Text(stringResource(R.string.ai_offline))
                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AiMode.entries.forEach { mode ->
                                FilterChip(selected = aiMode == mode, onClick = { aiMode = mode }, label = { Text(stringResource(mode.labelRes())) })
                            }
                        }
                        Text(stringResource(aiMode.hintRes()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.ai_will_send), style = MaterialTheme.typography.labelMedium)
                        Text(AiGateway.consentPreview(current), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(consent, { consent = it })
                            Text(stringResource(R.string.ai_consent))
                        }
                        Button(onClick = {
                            scope.launch {
                                aiError = null
                                onAskAi(aiMode, current)
                                    .onSuccess { aiText = it }
                                    .onFailure { aiError = it.message }
                            }
                        }, enabled = consent) { Text(stringResource(R.string.action_ask_ai)) }
                        aiError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        aiText?.let { text ->
                            InstrumentPanel(accent = Indigo.copy(alpha = 0.5f)) {
                                Kicker(stringResource(R.string.ai_mark), Indigo)
                                Text(text)
                                Button(onClick = { onExpand(text, Provenance.ai); aiText = null }) {
                                    Text(stringResource(R.string.action_keep_ai))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Kicker(stringResource(R.string.studio_links))
            linked.forEach { rel ->
                val other = if (rel.fromId == current.id) rel.toId else rel.fromId
                val label = allThoughts.find { it.id == other }?.title ?: projects.find { it.id == other }?.title ?: other
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { onOpenThought(other) }) { Text("${stringResource(rel.relationType.labelRes())} → $label") }
                    TextButton(onClick = { onUnlink(rel.id) }) { Text("×") }
                }
            }
            Text(stringResource(R.string.studio_link_target))
            allThoughts.filter { it.id != current.id }.take(12).forEach { t ->
                FilterChip(selected = relTarget == t.id, onClick = { relTarget = t.id }, label = { Text(t.title.ifBlank { t.id }) })
            }
            RelationType.entries.forEach { type ->
                FilterChip(selected = relType == type, onClick = { relType = type }, label = { Text(stringResource(type.labelRes())) })
            }
            OutlinedTextField(relWhy, { relWhy = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.studio_link_why)) })
            Button(onClick = { onLink(relTarget, relType, relWhy); relTarget = ""; relWhy = "" }, enabled = relTarget.isNotBlank()) {
                Text(stringResource(R.string.action_add_link))
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { onChange(current.copy(status = ThoughtStatus.archived)) }) { Text(stringResource(R.string.action_archive)) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HypoField(label: Int, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth().padding(vertical = 4.dp), label = { Text(stringResource(label)) }, minLines = 2)
}

private val Amber = lab.neurotrino.app.ui.theme.Amber
