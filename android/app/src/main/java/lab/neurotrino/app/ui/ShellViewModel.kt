package lab.neurotrino.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import lab.neurotrino.app.NeurotrinoApp
import lab.neurotrino.app.data.ai.AiGateway
import lab.neurotrino.app.data.ai.AiUnavailableException
import lab.neurotrino.app.domain.AiMode
import lab.neurotrino.app.domain.GraphKind
import lab.neurotrino.app.domain.HypothesisFields
import lab.neurotrino.app.domain.MindState
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.UserProfile
import lab.neurotrino.app.domain.lastResumable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShellState(
    val ready: Boolean = false,
    val thoughts: List<Thought> = emptyList(),
    val projects: List<Project> = emptyList(),
    val profile: UserProfile = UserProfile(),
    val captureOpen: Boolean = false,
    val captureDraft: String = "",
    val pendingShare: String? = null,
    val error: String? = null,
)

class ShellViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as NeurotrinoApp).container.repository

    private val ready = MutableStateFlow(false)
    val captureOpen = MutableStateFlow(false)
    val captureDraft = MutableStateFlow("")
    val error = MutableStateFlow<String?>(null)
    val readyState: StateFlow<Boolean> = ready

    val thoughts = repo.thoughts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val projects = repo.projects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val relationships = repo.relationships.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val reading = repo.reading.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val profile = repo.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    val resume: StateFlow<Thought?> = thoughts
        .map { lastResumable(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            runCatching { repo.start() }
                .onFailure { error.value = "Nie udało się otworzyć lokalnej bazy." }
            ready.value = true
        }
    }

    fun openCapture(preset: String? = null) {
        if (preset != null) captureDraft.value = preset
        captureOpen.value = true
    }

    fun closeCapture() {
        captureOpen.value = false
    }

    fun setDraft(text: String) {
        captureDraft.value = text
    }

    fun capture(text: String, openStudio: (String) -> Unit, stay: Boolean) {
        viewModelScope.launch {
            val created = repo.capture(text)
            captureDraft.value = ""
            captureOpen.value = false
            val first = created.firstOrNull() ?: return@launch
            if (!stay) openStudio(first.id)
        }
    }

    fun setMind(state: MindState?) {
        viewModelScope.launch {
            val p = profile.value
            repo.saveProfile(p.copy(lastMindState = if (p.lastMindState == state) null else state))
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch { repo.saveProfile(profile.value.copy(onboarded = true)) }
    }

    fun saveProfile(next: UserProfile) {
        viewModelScope.launch { repo.saveProfile(next) }
    }

    fun updateThought(thought: Thought) {
        viewModelScope.launch { repo.updateThought(thought) }
    }

    fun touch(id: String) {
        viewModelScope.launch { repo.touch(id) }
    }

    fun addExpansion(id: String, text: String, provenance: Provenance) {
        viewModelScope.launch { repo.addExpansion(id, text, provenance) }
    }

    fun setHypothesis(id: String, fields: HypothesisFields) {
        viewModelScope.launch { repo.setHypothesis(id, fields) }
    }

    fun deleteThought(id: String) {
        viewModelScope.launch { repo.deleteThought(id) }
    }

    fun setStatus(id: String, status: ThoughtStatus) {
        viewModelScope.launch { repo.setStatus(id, status) }
    }

    fun createProject(title: String, purpose: String) {
        viewModelScope.launch { repo.createProject(title, purpose) }
    }

    fun updateProject(project: Project, note: String? = null) {
        viewModelScope.launch { repo.updateProject(project, note) }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch { repo.deleteProject(id) }
    }

    fun attachToProject(thoughtId: String, projectId: String) {
        viewModelScope.launch { repo.attachThoughtToProject(thoughtId, projectId) }
    }

    fun addLink(from: String, to: String, fromKind: GraphKind, toKind: GraphKind, type: RelationType, why: String) {
        viewModelScope.launch {
            repo.addRelationship(from, to, fromKind, toKind, type, why)
            val t = thoughts.value.find { it.id == from }
            if (t != null && t.status == ThoughtStatus.seed) repo.setStatus(from, ThoughtStatus.connected)
        }
    }

    fun removeLink(id: String) {
        viewModelScope.launch { repo.deleteRelationship(id) }
    }

    fun addBook(title: String, author: String) {
        viewModelScope.launch { repo.addBook(title, author) }
    }

    fun updateBook(item: ReadingItem) {
        viewModelScope.launch { repo.updateBook(item) }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch { repo.deleteBook(id) }
    }

    fun addImage(thoughtId: String, uri: Uri, name: String, mime: String, size: Long) {
        viewModelScope.launch { repo.addAttachment(thoughtId, uri, name, mime, size) }
    }

    fun deleteAttachment(id: String) {
        viewModelScope.launch { repo.deleteAttachment(id) }
    }

    suspend fun exportJson(): String = repo.exportJson()

    suspend fun importJson(raw: String, replace: Boolean): Pair<Int, Int> = repo.importJson(raw, replace)

    fun removeSamples() {
        viewModelScope.launch { repo.removeSamples() }
    }

    fun wipe() {
        viewModelScope.launch { repo.wipe(getApplication()) }
    }

    fun thoughtFlow(id: String) = repo.thought(id)
    fun projectFlow(id: String) = repo.project(id)
    fun bookFlow(id: String) = repo.book(id)
    fun attachmentsFlow(id: String) = repo.attachments(id)

    suspend fun askAi(mode: AiMode, thought: Thought): Result<String> =
        runCatching { AiGateway.request(mode, thought, profile.value) }

    fun handleIncomingText(text: String) {
        viewModelScope.launch {
            repo.capture(text)
        }
    }
}
