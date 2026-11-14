package lab.neurotrino.app.ui.studio

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.thoughtDispatchPreview
import lab.neurotrino.app.domain.AttachmentMeta
import lab.neurotrino.app.domain.HypothesisFields
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.Relationship
import lab.neurotrino.app.domain.SourceItem
import lab.neurotrino.app.domain.SparksCatalog
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType
import lab.neurotrino.app.domain.relativeTimePl
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabBadge
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.transparentTextFieldColors
import lab.neurotrino.app.ui.components.SectionHeader
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Indigo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class SaveState { Idle, Saving, Saved, Error }

@Composable
fun StudioScreen(
    thought: Thought?,
    missing: Boolean,
    projects: List<Project>,
    allThoughts: List<Thought>,
    relationships: List<Relationship>,
    attachments: List<AttachmentMeta>,
    sources: List<SourceItem>,
    gatewayConfigured: Boolean,
    online: Boolean,
    onBack: () -> Unit,
    onChange: (Thought) -> Unit,
    onTouch: () -> Unit,
    onDelete: () -> Unit,
    onDeleted: () -> Unit,
    onExpand: (String, Provenance) -> Unit,
    onLink: (String, RelationType, String) -> Unit,
    onUnlink: (String) -> Unit,
    onAttachProject: (String, Boolean) -> Unit,
    onPickImage: (Uri, String, String, Long) -> Unit,
    onDeleteAttachment: (String) -> Unit,
    onAddSource: (String, String, String, String, String) -> Unit,
    onAttachSource: (String) -> Unit,
    onDetachSource: (String) -> Unit,
    onDeleteSource: (String) -> Unit,
    onAskAi: suspend (AssistantMode, Thought) -> Result<String>,
    onOpenAssistant: () -> Unit,
    onOpenThought: (String) -> Unit,
    onOpenProject: (String) -> Unit,
) {
    if (thought == null && !missing) {
        LabField { Box(Modifier.padding(24.dp)) { Text(stringResource(R.string.loading)) } }
        return
    }
    if (thought == null) {
        LabField {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.thoughts_missing), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }
        }
        return
    }
    val current = thought
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember(current.id) { mutableStateOf(current.title) }
    var content by remember(current.id) { mutableStateOf(current.content) }
    var types by remember(current.id) { mutableStateOf(current.types) }
    var status by remember(current.id) { mutableStateOf(current.status) }
    var tags by remember(current.id) { mutableStateOf(current.tags) }
    var projectIds by remember(current.id) { mutableStateOf(current.projectIds) }
    var hypo by remember(current.id) { mutableStateOf(current.hypothesis ?: HypothesisFields()) }
    var showHypo by remember(current.id) { mutableStateOf(current.hypothesis != null) }
    var showRaw by remember { mutableStateOf(false) }
    var tagDraft by remember { mutableStateOf("") }
    var expansion by remember { mutableStateOf("") }
    var saveState by remember { mutableStateOf(SaveState.Idle) }
    var saveJob by remember { mutableStateOf<Job?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var linkDialog by remember { mutableStateOf(false) }
    var sourceDialog by remember { mutableStateOf(false) }
    var aiMode by remember { mutableStateOf(AssistantMode.impuls) }
    var consent by remember { mutableStateOf(false) }
    var aiText by remember { mutableStateOf<String?>(null) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var aiWaiting by remember { mutableStateOf(false) }

    LaunchedEffect(current.id) { onTouch() }

    fun merged() = current.copy(
        title = title.trim(),
        content = content,
        types = types,
        status = status,
        tags = tags,
        projectIds = projectIds,
        hypothesis = hypo.takeIf { showHypo || it != HypothesisFields() },
    )

    fun commit() {
        saveJob?.cancel()
        saveState = SaveState.Saving
        saveJob = scope.launch {
            delay(450)
            runCatching { onChange(merged()) }
                .onSuccess { saveState = SaveState.Saved }
                .onFailure { saveState = SaveState.Error }
        }
    }

    val linked = relationships.filter { it.fromId == current.id || it.toId == current.id }
    val linkedSources = sources.filter { it.id in current.sourceIds }
    val saveLabel = when (saveState) {
        SaveState.Idle -> R.string.studio_save_idle
        SaveState.Saving -> R.string.studio_saving
        SaveState.Saved -> R.string.studio_saved
        SaveState.Error -> R.string.studio_save_error
    }
    val saveDot = when (saveState) {
        SaveState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
        SaveState.Saving -> Amber
        SaveState.Saved -> Cyan
        SaveState.Error -> MaterialTheme.colorScheme.error
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.studio_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                    onDeleted()
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
    if (linkDialog) {
        LinkThoughtDialog(
            candidates = allThoughts.filter { it.id != current.id && it.status != ThoughtStatus.archived },
            onDismiss = { linkDialog = false },
            onLink = { targetId, type, why ->
                onLink(targetId, type, why)
                linkDialog = false
            },
        )
    }
    if (sourceDialog) {
        AddSourceDialog(
            onDismiss = { sourceDialog = false },
            onSave = { t, a, u, d, n ->
                onAddSource(t, a, u, d, n)
                sourceDialog = false
            },
        )
    }

    LabField {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            // ── top bar ───────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (current.isSample) LabBadge(stringResource(R.string.sample_badge), Amber)
                    Box(Modifier.size(7.dp).clip(RoundedCornerShape(percent = 50)).background(saveDot))
                    Text(stringResource(saveLabel), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── manuscript title + body ───────────────────────────────────────
            TextField(
                value = title,
                onValueChange = { title = it; commit() },
                placeholder = { Text(stringResource(R.string.studio_title_hint), style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))) },
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = transparentTextFieldColors(),
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.studio_created, relativeTimePl(current.createdAt).ifBlank { "·" }),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            TextField(
                value = content,
                onValueChange = { content = it; commit() },
                placeholder = { Text(stringResource(R.string.studio_body_hint), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = transparentTextFieldColors(),
                minLines = 7,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── status (Etap) ────────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            Kicker(stringResource(R.string.studio_status))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThoughtStatus.entries.forEach { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = st; commit() },
                        label = { Text(stringResource(st.labelRes())) },
                    )
                }
            }

            // ── type (Rodzaj) ────────────────────────────────────────────────
            Kicker(stringResource(R.string.thoughts_filter_type))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThoughtType.entries.forEach { type ->
                    FilterChip(
                        selected = type in types,
                        onClick = { types = if (type in types) types - type else types + type; commit() },
                        label = { Text(stringResource(type.labelRes())) },
                    )
                }
            }

            // ── projects ─────────────────────────────────────────────────────
            Kicker(stringResource(R.string.studio_project))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = projectIds.isEmpty(),
                    onClick = {
                        projectIds.forEach { onAttachProject(it, false) }
                        projectIds = emptyList()
                        commit()
                    },
                    label = { Text(stringResource(R.string.studio_no_project)) },
                )
                projects.forEach { p ->
                    val selected = p.id in projectIds
                    FilterChip(
                        selected = selected,
                        onClick = {
                            projectIds = if (selected) projectIds - p.id else projectIds + p.id
                            onAttachProject(p.id, !selected)
                            commit()
                        },
                        label = { Text(p.title) },
                    )
                }
            }

            // ── tags ─────────────────────────────────────────────────────────
            Kicker(stringResource(R.string.studio_tags))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tagDraft,
                    onValueChange = { tagDraft = it },
                    placeholder = { Text(stringResource(R.string.studio_tag_hint)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(onClick = {
                    val tag = tagDraft.trim().lowercase().removePrefix("#")
                    if (tag.isNotEmpty() && tag !in tags) {
                        tags = tags + tag
                        tagDraft = ""
                        commit()
                    }
                }) { Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.studio_add_tag)) }
            }
            if (tags.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = true,
                            onClick = { tags = tags - tag; commit() },
                            label = { Text("#$tag") },
                        )
                    }
                }
            }

            // ── first capture (immutable) ────────────────────────────────────
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { showRaw = !showRaw }) {
                Text(stringResource(if (showRaw) R.string.studio_raw_hide else R.string.studio_raw_show))
            }
            if (showRaw) {
                InstrumentPanel {
                    Kicker(stringResource(R.string.studio_raw))
                    Text(current.rawText.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ── hypothesis fields ────────────────────────────────────────────
            TextButton(onClick = { showHypo = !showHypo; commit() }) {
                Text(stringResource(if (showHypo) R.string.studio_hypothesis_hide else R.string.studio_hypothesis))
            }
            if (showHypo) {
                Text(stringResource(R.string.studio_hypothesis_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HypoField(R.string.hypo_observation, hypo.observation) { hypo = hypo.copy(observation = it); commit() }
                HypoField(R.string.hypo_proposal, hypo.proposal) { hypo = hypo.copy(proposal = it); commit() }
                HypoField(R.string.hypo_mechanism, hypo.mechanism) { hypo = hypo.copy(mechanism = it); commit() }
                HypoField(R.string.hypo_assumptions, hypo.assumptions) { hypo = hypo.copy(assumptions = it); commit() }
                HypoField(R.string.hypo_known, hypo.known) { hypo = hypo.copy(known = it); commit() }
                HypoField(R.string.hypo_alternatives, hypo.alternatives) { hypo = hypo.copy(alternatives = it); commit() }
                HypoField(R.string.hypo_expectations, hypo.expectations) { hypo = hypo.copy(expectations = it); commit() }
                HypoField(R.string.hypo_falsifiers, hypo.falsifiers) { hypo = hypo.copy(falsifiers = it); commit() }
                HypoField(R.string.hypo_next, hypo.nextStep) { hypo = hypo.copy(nextStep = it); commit() }
                HypoField(R.string.hypo_related, hypo.related) { hypo = hypo.copy(related = it); commit() }
            }

            // ── attachments ──────────────────────────────────────────────────
            Spacer(Modifier.height(6.dp))
            SectionHeader(stringResource(R.string.studio_images))
            AttachmentPicker(onPickImage, attachments, onDeleteAttachment)

            // ── expansions ───────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            SectionHeader(stringResource(R.string.studio_expansions))
            Text(stringResource(R.string.studio_expansions_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            current.expansions.forEach { item ->
                InstrumentPanel(Modifier.padding(vertical = 6.dp), accent = if (item.provenance == Provenance.ai) Indigo else Amber.copy(alpha = 0.5f)) {
                    Kicker(stringResource(item.provenance.labelRes()), if (item.provenance == Provenance.ai) Indigo else Amber)
                    Text(item.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
            OutlinedTextField(
                value = expansion,
                onValueChange = { expansion = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                placeholder = { Text(stringResource(R.string.studio_expansion_hint)) },
                minLines = 2,
            )
            OutlinedButton(
                onClick = { if (expansion.isNotBlank()) { onExpand(expansion, Provenance.user); expansion = "" } },
                enabled = expansion.isNotBlank(),
            ) { Text(stringResource(R.string.studio_add_expansion)) }

            // ── offline impulse (never fake AI) ──────────────────────────────
            Spacer(Modifier.height(10.dp))
            InstrumentPanel(accent = Amber.copy(alpha = 0.4f)) {
                Kicker(stringResource(R.string.studio_local_impulse))
                Text(SparksCatalog.localImpulse(types, status, hypo != HypothesisFields()), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.studio_local_impulse_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── sources ──────────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            SectionHeader(
                stringResource(R.string.studio_sources),
                action = { TextButton(onClick = { sourceDialog = true }) { Text(stringResource(R.string.action_add_source)) } },
            )
            if (linkedSources.isEmpty()) {
                Text(stringResource(R.string.studio_sources_empty), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                linkedSources.forEach { source ->
                    val sourceId = source.id
                    InstrumentPanel(Modifier.padding(vertical = 6.dp), accent = Indigo.copy(alpha = 0.4f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(source.title, style = MaterialTheme.typography.titleSmall)
                                if (source.authors.isNotBlank()) Text(source.authors, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (source.doi.isNotBlank()) Text("doi: ${source.doi}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                else if (source.url.isNotBlank()) Text(source.url.take(72), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column {
                                IconButton(onClick = { onDetachSource(sourceId) }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_detach_source), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (source.url.isNotBlank()) {
                                    TextButton(onClick = {
                                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.url))) }
                                    }) { Text(stringResource(R.string.action_open_link)) }
                                }
                            }
                        }
                    }
                }
            }

            // ── AI assistant (optional, explicit consent) ────────────────────
            Spacer(Modifier.height(10.dp))
            InstrumentPanel(accent = Indigo.copy(alpha = 0.45f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Kicker(stringResource(R.string.settings_ai))
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onOpenAssistant) {
                        Text(stringResource(R.string.assistant_open), style = MaterialTheme.typography.labelMedium)
                    }
                }
                when {
                    !gatewayConfigured -> {
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.ai_unconfigured), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    !online -> {
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.ai_offline), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> {
                        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistantMode.entries.forEach { mode ->
                                FilterChip(selected = aiMode == mode, onClick = { aiMode = mode }, label = { Text(stringResource(mode.labelRes())) })
                            }
                        }
                        Text(stringResource(aiMode.hintRes()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.ai_will_send), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(thoughtDispatchPreview(current), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(consent, { consent = it })
                            Text(stringResource(R.string.ai_consent), style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    aiError = null
                                    aiWaiting = true
                                    onAskAi(aiMode, current)
                                        .onSuccess { aiText = it }
                                        .onFailure { aiError = it.message ?: it.toString() }
                                    aiWaiting = false
                                }
                            },
                            enabled = consent && !aiWaiting,
                        ) { Text(stringResource(if (aiWaiting) R.string.ai_waiting else R.string.action_ask_ai)) }
                        aiError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp)) }
                        aiText?.let { text ->
                            InstrumentPanel(Modifier.padding(top = 8.dp), accent = Indigo.copy(alpha = 0.6f)) {
                                Kicker(stringResource(R.string.ai_mark), Indigo)
                                Text(text, style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { onExpand(text, Provenance.ai); aiText = null }) {
                                        Text(stringResource(R.string.action_keep_ai))
                                    }
                                    TextButton(onClick = { aiText = null }) { Text(stringResource(R.string.action_dismiss)) }
                                }
                            }
                        }
                    }
                }
            }

            // ── relationships ────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            SectionHeader(
                stringResource(R.string.studio_links),
                action = { TextButton(onClick = { linkDialog = true }) { Text(stringResource(R.string.action_add_link)) } },
            )
            if (linked.isEmpty()) {
                Text(stringResource(R.string.studio_links_empty), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                linked.forEach { rel ->
                    val otherId = if (rel.fromId == current.id) rel.toId else rel.fromId
                    val endpoint = resolveEndpoint(otherId, allThoughts, projects, sources)
                    val open: () -> Unit = when {
                        endpoint.thoughtId != null -> { { onOpenThought(endpoint.thoughtId!!) } }
                        endpoint.projectId != null -> { { onOpenProject(endpoint.projectId!!) } }
                        endpoint.sourceUrl != null -> {
                            { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(endpoint.sourceUrl!!))) } }
                        }
                        else -> { {} }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Kicker(stringResource(rel.relationType.labelRes()))
                            TextButton(onClick = open, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                                Text(endpoint.label, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                            }
                            Text(
                                rel.explanation.ifBlank { stringResource(rel.relationType.hintRes()) },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { onUnlink(rel.id) }) { Text(stringResource(R.string.action_remove_link)) }
                    }
                }
            }

            // ── footer actions ───────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { status = if (status == ThoughtStatus.archived) ThoughtStatus.seed else ThoughtStatus.archived; commit() }) {
                    Icon(if (status == ThoughtStatus.archived) Icons.Outlined.Unarchive else Icons.Outlined.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(if (status == ThoughtStatus.archived) R.string.action_restore else R.string.action_archive))
                }
                OutlinedButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(96.dp))
        }
    }
}
