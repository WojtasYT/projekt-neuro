package lab.neurotrino.app.ui.library

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
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.ReadingStatus
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.ui.components.CaptureWell
import lab.neurotrino.app.ui.components.EmptyState
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.PressablePanel
import lab.neurotrino.app.ui.components.SectionHeader
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Indigo

@Composable
fun LibraryScreen(
    items: List<ReadingItem>,
    onOpen: (String) -> Unit,
    onAdd: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }

    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Kicker(stringResource(R.string.library_kicker))
                    Text(stringResource(R.string.library_title), style = MaterialTheme.typography.headlineLarge)
                }
                IconButton(onClick = { showAdd = !showAdd }) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.action_add_book))
                }
            }
            Text(stringResource(R.string.library_lede), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (showAdd) {
                CaptureWell(Modifier.padding(top = 12.dp)) {
                    Kicker(stringResource(R.string.action_add_book), Indigo)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.library_book_title)) }, singleLine = true)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(author, { author = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.library_author)) }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title.trim(), author.trim())
                                title = ""; author = ""; showAdd = false
                            }
                        }, enabled = title.isNotBlank()) { Text(stringResource(R.string.action_add_book)) }
                        TextButton(onClick = { showAdd = false; title = ""; author = "" }) { Text(stringResource(R.string.action_cancel)) }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            if (items.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.library_empty_title),
                    body = stringResource(R.string.library_empty),
                    actionLabel = stringResource(R.string.action_add_book),
                    onAction = { showAdd = true },
                )
            } else {
                items.sortedBy { it.title }.forEach { item ->
                    PressablePanel(onClick = { onOpen(item.id) }, Modifier.padding(vertical = 6.dp), accent = Indigo.copy(alpha = 0.3f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${item.author}${if (item.isStarter) " · " + stringResource(R.string.library_starter) else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(item.status.labelRes()), style = MaterialTheme.typography.labelMedium, color = Indigo)
                        if (item.progressNote.isNotBlank()) {
                            Text(item.progressNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
fun BookScreen(
    book: ReadingItem?,
    thoughts: List<Thought>,
    onBack: () -> Unit,
    onChange: (ReadingItem) -> Unit,
    onNote: () -> Unit,
    onOpenThought: (String) -> Unit,
    onDelete: () -> Unit,
) {
    if (book == null) {
        LabField {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.library_missing), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }
        }
        return
    }
    var confirmDelete by remember { mutableStateOf(false) }
    val linked = thoughts.filter { book.id in it.readingItemIds }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.library_delete_confirm)) },
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
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
            Text(book.title, style = MaterialTheme.typography.headlineLarge)
            Text(book.author, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (book.isStarter) {
                Text(stringResource(R.string.library_keep_starter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Kicker(stringResource(R.string.library_status))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadingStatus.entries.forEach { st ->
                    FilterChip(
                        selected = book.status == st,
                        onClick = { onChange(book.copy(status = st)) },
                        label = { Text(stringResource(st.labelRes())) },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(book.currentPage, { onChange(book.copy(currentPage = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.library_progress)) }, singleLine = true)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(book.progressNote, { onChange(book.copy(progressNote = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.library_progress_note)) }, minLines = 2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(book.notes, { onChange(book.copy(notes = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.library_notes)) }, minLines = 4)
            Spacer(Modifier.height(10.dp))
            Button(onClick = onNote) { Text(stringResource(R.string.action_note_from_book)) }

            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.library_linked), kickerColor = Indigo)
            if (linked.isEmpty()) {
                Text(stringResource(R.string.library_linked_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                linked.forEach { t ->
                    TextButton(onClick = { onOpenThought(t.id) }) {
                        Text(t.title.ifBlank { stringResource(R.string.thoughts_untitled) }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            if (!book.isStarter) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}
