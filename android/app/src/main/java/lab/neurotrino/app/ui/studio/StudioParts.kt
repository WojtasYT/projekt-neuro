package lab.neurotrino.app.ui.studio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.AttachmentMeta
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.SourceItem
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.matchesQuery
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.PanelShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// ── shared styling ──────────────────────────────────────────────────────────
@Composable
internal fun HypoField(label: Int, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        label = { Text(stringResource(label)) },
        minLines = 2,
    )
}

/** Resolved relationship endpoint: a label plus where it can be opened (if anywhere). */
internal data class Endpoint(
    val label: String,
    val thoughtId: String? = null,
    val projectId: String? = null,
    val sourceUrl: String? = null,
)

@Composable
internal fun resolveEndpoint(
    id: String,
    thoughts: List<Thought>,
    projects: List<Project>,
    sources: List<SourceItem>,
): Endpoint {
    thoughts.find { it.id == id }?.let {
        return Endpoint(it.title.ifBlank { stringResource(R.string.thoughts_untitled) }, thoughtId = it.id)
    }
    projects.find { it.id == id }?.let {
        return Endpoint(it.title, projectId = it.id)
    }
    sources.find { it.id == id }?.let {
        return Endpoint(it.title, sourceUrl = it.url.takeIf { u -> u.isNotBlank() })
    }
    return Endpoint(id)
}

// ── attachments ─────────────────────────────────────────────────────────────
@Composable
internal fun AttachmentPicker(
    onPickImage: (Uri, String, String, Long) -> Unit,
    attachments: List<AttachmentMeta>,
    onDelete: (String) -> Unit,
) {
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onPickImage(uri, "obraz", "image/*", 0L)
    }
    OutlinedButton(
        onClick = {
            picker.launch(PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
    ) {
        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.action_pick_image))
    }
    attachments.forEach { attachment ->
        val attachmentId = attachment.id
        AttachmentThumb(attachment, onDelete = { onDelete(attachmentId) })
    }
}

@Composable
private fun AttachmentThumb(attachment: AttachmentMeta, onDelete: () -> Unit) {
    var bitmap by remember(attachment.uri) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(attachment.uri) { mutableStateOf(false) }
    LaunchedEffect(attachment.uri) {
        val decoded = withContext(Dispatchers.IO) { decodeSampled(attachment.uri, 192) }
        if (decoded == null) failed = true else bitmap = decoded
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(72.dp).clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            val image = bitmap
            when {
                image != null -> Image(image.asImageBitmap(), contentDescription = attachment.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(72.dp))
                failed -> Icon(Icons.Outlined.Image, contentDescription = stringResource(R.string.attachment_missing), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> Text("…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(attachment.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                if (failed) stringResource(R.string.attachment_missing) else formatSize(attachment.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_remove), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> ""
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
}

private fun decodeSampled(uri: String, targetPx: Int): Bitmap? {
    val path = Uri.parse(uri).path ?: return null
    val file = File(path)
    if (!file.exists()) return null
    return runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > targetPx * 2) sample *= 2
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }.getOrNull()
}

// ── link dialog ─────────────────────────────────────────────────────────────
@Composable
internal fun LinkThoughtDialog(
    candidates: List<Thought>,
    onDismiss: () -> Unit,
    onLink: (String, RelationType, String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    var type by remember { mutableStateOf(RelationType.extends) }
    var why by remember { mutableStateOf("") }
    val filtered = remember(candidates, query) {
        candidates
            .filter { matchesQuery("${it.title} ${it.content} ${it.rawText} ${it.tags.joinToString(" ")}", query) }
            .sortedByDescending { it.updatedAt }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = PanelShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(Modifier.padding(18.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Kicker(stringResource(R.string.studio_link_target))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.studio_link_search)) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RelationType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(stringResource(option.labelRes())) },
                        )
                    }
                }
                Text(
                    stringResource(type.hintRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
                Spacer(Modifier.height(6.dp))
                if (filtered.isEmpty()) {
                    Text(stringResource(R.string.studio_link_none), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    filtered.take(40).forEach { candidate ->
                        val isSel = selected == candidate.id
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainer)
                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .clickable { selected = if (isSel) null else candidate.id },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    candidate.title.ifBlank { stringResource(R.string.thoughts_untitled) },
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    candidate.content.ifBlank { candidate.rawText }.take(90),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            FilterChip(
                                selected = isSel,
                                onClick = { selected = if (isSel) null else candidate.id },
                                label = { Text(if (isSel) "✓" else "+") },
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
                OutlinedTextField(
                    value = why,
                    onValueChange = { why = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    placeholder = { Text(stringResource(R.string.studio_link_why)) },
                    minLines = 2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    Button(
                        onClick = { selected?.let { onLink(it, type, why) } },
                        enabled = selected != null,
                    ) { Text(stringResource(R.string.action_add_link)) }
                }
            }
        }
    }
}

// ── add source dialog ───────────────────────────────────────────────────────
@Composable
internal fun AddSourceDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, authors: String, url: String, doi: String, notes: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var authors by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var doi by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = PanelShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(Modifier.padding(18.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Kicker(stringResource(R.string.studio_sources))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.source_title)) }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(authors, { authors = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.source_authors)) }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.source_url)) }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(doi, { doi = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.source_doi)) }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.source_honesty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    Button(onClick = { onSave(title, authors, url, doi, "") }, enabled = title.isNotBlank()) {
                        Text(stringResource(R.string.action_add_source))
                    }
                }
            }
        }
    }
}
