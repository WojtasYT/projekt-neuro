package lab.neurotrino.app.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.AssistantContextItem
import lab.neurotrino.app.domain.AssistantMessage
import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.Conversation
import lab.neurotrino.app.domain.matchesQuery
import lab.neurotrino.app.domain.relativeTimePl
import lab.neurotrino.app.ui.components.EmptyState
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.transparentTextFieldColors
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Indigo
import lab.neurotrino.app.ui.theme.Amber

@Composable
fun AssistantScreen(
    thoughtId: String?, // null = otwarte z laboratorium (lista)
    vm: AssistantViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenThought: (String) -> Unit,
) {
    LaunchedEffect(thoughtId) { vm.bind(thoughtId) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.savedToast.collect { snackbar.showSnackbar(it) }
    }

    val selectedId by vm.selectedId.collectAsStateWithLifecycle()

    LabField {
        Box(Modifier.fillMaxSize()) {
            if (selectedId == null) {
                ConversationListPane(
                    vm = vm,
                    onBack = onBack,
                    onOpenSettings = onOpenSettings,
                )
            } else {
                ChatPane(
                    vm = vm,
                    onBack = onBack,
                    onOpenSettings = onOpenSettings,
                    onOpenThought = onOpenThought,
                )
            }
            SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

// ─────────────────────────────────────────────────────────────── lista rozmów

@Composable
private fun ConversationListPane(
    vm: AssistantViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Conversation?>(null) }
    var deleteTarget by remember { mutableStateOf<Conversation?>(null) }

    val filtered = conversations.filter {
        query.isBlank() || matchesQuery("${it.title}", query)
    }
    val active = filtered.filter { !it.archived }
    val archived = filtered.filter { it.archived }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = null) }
            Spacer(Modifier.width(4.dp))
            Column {
                Kicker("NEUROTRINO LAB", Cyan)
                Text(stringResource(R.string.assistant_title), style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenSettings) { Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.nav_settings)) }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.assistant_search_conversations)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            colors = transparentTextFieldColors(),
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            FilledIconButton(onClick = { vm.newConversation() }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.assistant_new_conversation))
            }
        }
        Spacer(Modifier.height(6.dp))

        if (filtered.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.assistant_new_conversation),
                body = stringResource(R.string.assistant_conversations_empty),
                actionLabel = stringResource(R.string.assistant_new_conversation),
                onAction = { vm.newConversation() },
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(active, key = { it.id }) { convo ->
                ConversationRow(
                    convo = convo,
                    onOpen = { vm.selectConversation(convo.id) },
                    onRename = { renameTarget = convo },
                    onArchive = { vm.setArchived(convo.id, true) },
                    onDelete = { deleteTarget = convo },
                )
            }
            if (archived.isNotEmpty()) {
                item {
                    TextButton(onClick = { showArchived = !showArchived }) {
                        Text(stringResource(R.string.assistant_conversation_archived) + " (${archived.size})")
                    }
                }
                if (showArchived) {
                    items(archived, key = { "arc-${it.id}" }) { convo ->
                        ConversationRow(
                            convo = convo,
                            onOpen = { vm.selectConversation(convo.id) },
                            onRename = { renameTarget = convo },
                            onArchive = { vm.setArchived(convo.id, false) },
                            onDelete = { deleteTarget = convo },
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }

    renameTarget?.let { convo ->
        var text by remember(convo.id) { mutableStateOf(convo.title) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.assistant_rename)) },
            text = {
                OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true, label = { Text(stringResource(R.string.assistant_rename_hint)) })
            },
            confirmButton = {
                TextButton(onClick = { vm.renameConversation(convo.id, text); renameTarget = null }) {
                    Text(stringResource(R.string.assistant_rename))
                }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text(stringResource(R.string.action_dismiss)) } },
        )
    }
    deleteTarget?.let { convo ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.assistant_delete_conversation)) },
            text = { Text(stringResource(R.string.assistant_delete_conversation_confirm)) },
            confirmButton = {
                TextButton(onClick = { vm.deleteConversation(convo.id); deleteTarget = null }) {
                    Text(stringResource(R.string.assistant_delete_conversation), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.action_dismiss)) } },
        )
    }
}

@Composable
private fun ConversationRow(
    convo: Conversation,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    InstrumentPanel(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(convo.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row {
                    Text(stringResource(convo.mode.labelRes()), style = MaterialTheme.typography.labelSmall, color = Indigo)
                    Text(" · " + relativeTimePl(convo.updatedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onRename) { Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.assistant_rename), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = onArchive) {
                Icon(
                    if (convo.archived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                    contentDescription = stringResource(if (convo.archived) R.string.assistant_unarchive else R.string.assistant_archive),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.assistant_delete_conversation), tint = MaterialTheme.colorScheme.error) }
        }
    }
}

// ─────────────────────────────────────────────────────────────── konwersacja

@Composable
private fun ChatPane(
    vm: AssistantViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenThought: (String) -> Unit,
) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    val gatewayReady = profile.gatewayUrl.isNotBlank()
    val conversation by vm.selectedConversation.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    val sending by vm.sending.collectAsStateWithLifecycle()
    val lastError by vm.lastError.collectAsStateWithLifecycle()
    val lastRetryable by vm.lastRetryable.collectAsStateWithLifecycle()
    val preview by vm.preview.collectAsStateWithLifecycle()
    val previewing by vm.previewing.collectAsStateWithLifecycle()
    val excluded by vm.excluded.collectAsStateWithLifecycle()
    val contextItems by vm.contextItems.collectAsStateWithLifecycle()
    val sourceThought by vm.sourceThought.collectAsStateWithLifecycle()

    var composer by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var renameOpen by remember { mutableStateOf(false) }
    var deleteOpen by remember { mutableStateOf(false) }
    val convo = conversation ?: return

    Column(Modifier.fillMaxSize()) {
        // ── pasek górny ─────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { vm.selectConversation(null) }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = null)
            }
            Column(Modifier.weight(1f)) {
                Text(convo.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.assistant_mode_label) + ": " + stringResource(convo.mode.labelRes()), style = MaterialTheme.typography.labelSmall, color = Indigo)
            }
            sourceThought?.let { st ->
                IconButton(onClick = { onOpenThought(st.id) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = st.title.ifBlank { "Myśl" })
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) { Icon(Icons.Outlined.MoreVert, contentDescription = null) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.assistant_rename)) }, onClick = { menuOpen = false; renameOpen = true })
                    DropdownMenuItem(
                        text = { Text(stringResource(if (convo.archived) R.string.assistant_unarchive else R.string.assistant_archive)) },
                        onClick = { menuOpen = false; vm.setArchived(convo.id, !convo.archived) },
                    )
                    DropdownMenuItem(text = { Text(stringResource(R.string.assistant_delete_conversation), color = MaterialTheme.colorScheme.error) }, onClick = { menuOpen = false; deleteOpen = true })
                }
            }
        }

        // ── tryby ────────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistantMode.entries.forEach { mode ->
                FilterChip(
                    selected = convo.mode == mode,
                    onClick = { vm.setMode(mode) },
                    label = { Text(stringResource(mode.labelRes())) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // ── kontekst ─────────────────────────────────────────────────────────
        ContextChips(
            items = contextItems,
            sourceThoughtTitle = sourceThought?.title,
            currentThoughtIncluded = "thought:current" !in excluded,
            excluded = excluded,
            previewing = previewing,
            onToggle = { vm.toggleExcluded(it) },
            onPreview = { vm.previewNow() },
        )
        Spacer(Modifier.height(2.dp))

        // ── wiadomości ───────────────────────────────────────────────────────
        if (!gatewayReady) {
            InstrumentPanel(Modifier.padding(12.dp), accent = Amber.copy(alpha = 0.45f)) {
                Kicker("NEUROTRINO LAB", Amber)
                Text(stringResource(R.string.assistant_error_unconfigured), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                OutlinedButton(onClick = onOpenSettings, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.assistant_open_settings))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message, onSaveAsConcept = { vm.saveAssistantMessageAsConcept(message) })
            }
            if (sending) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Kicker(stringResource(R.string.assistant_thinking), Indigo)
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { vm.cancel() }) { Text(stringResource(R.string.assistant_cancel)) }
                    }
                }
            }
            lastError?.let { err ->
                item {
                    Column(Modifier.padding(top = 4.dp)) {
                        Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        if (lastRetryable) {
                            TextButton(onClick = { vm.retry() }) { Text(stringResource(R.string.assistant_retry)) }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── kompozytor ───────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = composer,
                onValueChange = { composer = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.assistant_composer_hint)) },
                maxLines = 4,
                colors = transparentTextFieldColors(),
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    val text = composer
                    composer = ""
                    vm.send(text)
                },
                enabled = composer.isNotBlank() && !sending && gatewayReady,
            ) {
                Icon(Icons.Outlined.Send, contentDescription = stringResource(R.string.assistant_send))
            }
        }
    }

    if (renameOpen) {
        var text by remember(convo.id) { mutableStateOf(convo.title) }
        AlertDialog(
            onDismissRequest = { renameOpen = false },
            title = { Text(stringResource(R.string.assistant_rename)) },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true, label = { Text(stringResource(R.string.assistant_rename_hint)) }) },
            confirmButton = { TextButton(onClick = { vm.renameConversation(convo.id, text); renameOpen = false }) { Text(stringResource(R.string.assistant_rename)) } },
            dismissButton = { TextButton(onClick = { renameOpen = false }) { Text(stringResource(R.string.action_dismiss)) } },
        )
    }
    if (deleteOpen) {
        AlertDialog(
            onDismissRequest = { deleteOpen = false },
            title = { Text(stringResource(R.string.assistant_delete_conversation)) },
            text = { Text(stringResource(R.string.assistant_delete_conversation_confirm)) },
            confirmButton = {
                TextButton(onClick = { vm.deleteConversation(convo.id); deleteOpen = false }) {
                    Text(stringResource(R.string.assistant_delete_conversation), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteOpen = false }) { Text(stringResource(R.string.action_dismiss)) } },
        )
    }
    preview?.let { p ->
        AlertDialog(
            onDismissRequest = { vm.clearPreview() },
            title = { Text(stringResource(R.string.assistant_preview_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.assistant_preview_chars, p.contextChars), style = MaterialTheme.typography.labelMedium, color = Cyan)
                    Spacer(Modifier.height(8.dp))
                    p.messages.forEach { m ->
                        Text(m.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(m.content, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { vm.clearPreview() }) { Text(stringResource(R.string.assistant_preview_close)) } },
        )
    }
}

@Composable
private fun ContextChips(
    items: List<AssistantContextItem>,
    sourceThoughtTitle: String?,
    currentThoughtIncluded: Boolean,
    excluded: Set<String>,
    previewing: Boolean,
    onToggle: (String) -> Unit,
    onPreview: () -> Unit,
) {
    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Kicker(stringResource(R.string.assistant_context_items), Cyan)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onPreview, enabled = !previewing) {
                Text(stringResource(R.string.assistant_preview_button), style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (sourceThoughtTitle != null) {
                InputChipToggle(
                    label = stringResource(R.string.assistant_context_current) + ": " + sourceThoughtTitle.take(48),
                    active = currentThoughtIncluded,
                    onClick = { onToggle("thought:current") },
                )
            }
            items.forEach { item ->
                InputChipToggle(
                    label = item.title.ifBlank { item.id }.take(48),
                    active = true, // elementy z listy są już po wykluczeniu; kliknięcie usuwa
                    onClick = { onToggle(item.id) },
                )
            }
            if (excluded.isNotEmpty()) {
                TextButton(onClick = { excluded.forEach { onToggle(it) } }) {
                    Text(stringResource(R.string.assistant_context_excluded) + ": ${excluded.size}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun InputChipToggle(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = if (active) Indigo.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.clip(RoundedCornerShape(percent = 50)).clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun MessageBubble(
    message: AssistantMessage,
    onSaveAsConcept: () -> Unit,
) {
    val isUser = message.role == "user"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Column(Modifier.widthIn(max = 520.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isUser) Indigo.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (!isUser) {
                        Kicker(stringResource(R.string.assistant_message_ai_mark), Indigo)
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(message.content, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (!isUser) {
                TextButton(onClick = onSaveAsConcept) {
                    Text(stringResource(R.string.assistant_save_as_concept), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
