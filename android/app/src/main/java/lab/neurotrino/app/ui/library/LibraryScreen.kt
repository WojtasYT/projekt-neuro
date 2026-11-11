package lab.neurotrino.app.ui.library

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
// used in BookScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.ReadingStatus
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.ui.components.CaptureWell
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.labelRes

@Composable
fun LibraryScreen(items: List<ReadingItem>, onOpen: (String) -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Kicker(stringResource(R.string.library_kicker))
            Text(stringResource(R.string.library_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.library_lede), color = MaterialTheme.colorScheme.onSurfaceVariant)
            CaptureWell {
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.library_book_title)) })
                OutlinedTextField(author, { author = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.library_author)) })
                Button(onClick = { onAdd(title, author); title = ""; author = "" }, enabled = title.isNotBlank()) {
                    Text(stringResource(R.string.action_add_book))
                }
            }
            items.sortedBy { it.title }.forEach { item ->
                InstrumentPanel(Modifier.padding(vertical = 6.dp).clickable { onOpen(item.id) }) {
                    Text(item.title, style = MaterialTheme.typography.titleLarge)
                    Text("${item.author}${if (item.isStarter) " · " + stringResource(R.string.library_starter) else ""}")
                    Text(stringResource(item.status.labelRes()), color = MaterialTheme.colorScheme.secondary)
                    if (item.progressNote.isNotBlank()) Text(item.progressNote, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(80.dp))
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
        LabField { Text(stringResource(R.string.library_missing), Modifier.padding(24.dp)) }
        return
    }
    val linked = thoughts.filter { book.id in it.readingItemIds }
    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            Text(book.title, style = MaterialTheme.typography.headlineLarge)
            Text(book.author, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ReadingStatus.entries.forEach { st ->
                FilterChip(selected = book.status == st, onClick = { onChange(book.copy(status = st)) }, label = { Text(stringResource(st.labelRes())) })
            }
            OutlinedTextField(book.currentPage, { onChange(book.copy(currentPage = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.library_progress)) })
            OutlinedTextField(book.progressNote, { onChange(book.copy(progressNote = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.library_progress_note)) })
            OutlinedTextField(book.notes, { onChange(book.copy(notes = it)) }, Modifier.fillMaxWidth(), minLines = 4, label = { Text(stringResource(R.string.library_notes)) })
            Button(onClick = onNote) { Text(stringResource(R.string.action_note_from_book)) }
            linked.forEach { TextButton(onClick = { onOpenThought(it.id) }) { Text(it.title) } }
            if (!book.isStarter) TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            else Text(stringResource(R.string.library_keep_starter), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
