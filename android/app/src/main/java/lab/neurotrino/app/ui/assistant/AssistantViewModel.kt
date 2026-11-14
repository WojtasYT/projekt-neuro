package lab.neurotrino.app.ui.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import lab.neurotrino.app.NeurotrinoApp
import lab.neurotrino.app.data.ai.AiGateway
import lab.neurotrino.app.data.ai.AiGatewayException
import lab.neurotrino.app.data.ai.ContextPreviewDto
import lab.neurotrino.app.domain.AssistantContextItem
import lab.neurotrino.app.domain.AssistantMessage
import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.Conversation
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.UserProfile
import lab.neurotrino.app.domain.buildContextItems
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as NeurotrinoApp).container.repository

    val thoughts = repo.thoughts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val projects = repo.projects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val sources = repo.sources.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val relationships = repo.relationships.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val profile = repo.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())
    val conversations = repo.conversations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    val selectedConversation: StateFlow<Conversation?> = combine(conversations, _selectedId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _messages = MutableStateFlow<List<AssistantMessage>>(emptyList())
    val messages: StateFlow<List<AssistantMessage>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedId.collectLatest { id ->
                if (id == null) {
                    _messages.value = emptyList()
                } else {
                    repo.messages(id).collect { _messages.value = it }
                }
            }
        }
    }

    // ── Kontekst (bieżąca treść, nie kopia) ─────────────────────────────────
    private val _sourceThoughtId = MutableStateFlow<String?>(null)
    val sourceThoughtId: StateFlow<String?> = _sourceThoughtId.asStateFlow()

    /** Żywa myśl źródłowa z przepływu repozytorium — edytor jest write-through, więc to jest "aktualny tekst". */
    val sourceThought: StateFlow<Thought?> = combine(thoughts, _sourceThoughtId) { list, id ->
        id?.let { key -> list.find { it.id == key } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _excluded = MutableStateFlow<Set<String>>(emptySet())
    val excluded: StateFlow<Set<String>> = _excluded.asStateFlow()

    /** Elementy kontekstu (po wykluczeniach) — to samo zobaczą chips, podgląd i wysyłka. */
    private val linkedThoughts: StateFlow<List<Thought>> = combine(sourceThought, thoughts, relationships) { thought, allThoughts, rels ->
        if (thought == null) return@combine emptyList<Thought>()
        // tylko relacje, które faktycznie dotykają bieżącej myśli
        val touching = rels.filter { it.fromId == thought.id || it.toId == thought.id }
            .flatMap { listOf(it.fromId, it.toId) }
            .filter { it != thought.id }
            .toSet()
        allThoughts.filter { it.id in touching }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val contextItemsRaw: StateFlow<List<AssistantContextItem>> = combine(
        sourceThought, linkedThoughts, projects, sources,
    ) { thought, linked, projs, srcs ->
        buildContextItems(thought, linked, projs, srcs, emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val contextItems: StateFlow<List<AssistantContextItem>> = combine(contextItemsRaw, _excluded) { raw, excludedIds ->
        raw.filterNot { it.id in excludedIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Tekst wysyłany jako currentThought ("" gdy użytkownik wykluczył bieżącą myśl). */
    val currentThoughtForSend: StateFlow<String> = combine(sourceThought, _excluded) { thought, excludedIds ->
        if ("thought:current" in excludedIds) ""
        else thought?.content?.ifBlank { thought.rawText }?.trim().orEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // ── Wysyłka ──────────────────────────────────────────────────────────────
    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _lastRetryable = MutableStateFlow(false)
    val lastRetryable: StateFlow<Boolean> = _lastRetryable.asStateFlow()

    private val _preview = MutableStateFlow<ContextPreviewDto?>(null)
    val preview: StateFlow<ContextPreviewDto?> = _preview.asStateFlow()

    private val _previewing = MutableStateFlow(false)
    val previewing: StateFlow<Boolean> = _previewing.asStateFlow()

    private val savedEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val savedToast = savedEvents.asSharedFlow()

    private var sendJob: Job? = null

    // ── Operacje ─────────────────────────────────────────────────────────────

    /** Wejście z trasy. thoughtId == null → lista rozmów. Z myślą → dopnij (lub utwórz) wątek z kontekstem. */
    fun bind(thoughtId: String?) {
        _sourceThoughtId.value = thoughtId
        viewModelScope.launch {
            if (thoughtId == null) return@launch
            val existing = repo.latestConversationForThought(thoughtId)
            if (existing != null) {
                _selectedId.value = existing.id
            } else {
                val created = repo.createConversation(
                    mode = profile.value.aiDefaultMode,
                    sourceThoughtId = thoughtId,
                )
                _selectedId.value = created.id
            }
        }
    }

    fun selectConversation(id: String?) {
        _selectedId.value = id
    }

    fun newConversation() {
        viewModelScope.launch {
            val created = repo.createConversation(mode = profile.value.aiDefaultMode, sourceThoughtId = _sourceThoughtId.value)
            _selectedId.value = created.id
        }
    }

    fun renameConversation(id: String, title: String) {
        viewModelScope.launch { repo.renameConversation(id, title) }
    }

    fun setArchived(id: String, archived: Boolean) {
        viewModelScope.launch {
            repo.setConversationArchived(id, archived)
            if (archived && _selectedId.value == id) _selectedId.value = null
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repo.deleteConversation(id)
            if (_selectedId.value == id) _selectedId.value = null
        }
    }

    fun setMode(mode: AssistantMode) {
        val id = _selectedId.value ?: return
        viewModelScope.launch { repo.setConversationMode(id, mode) }
    }

    fun toggleExcluded(key: String) {
        _excluded.value = if (key in _excluded.value) _excluded.value - key else _excluded.value + key
    }

    fun clearPreview() {
        _preview.value = null
    }

    fun send(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || _sending.value) return
        sendJob = viewModelScope.launch {
            try {
                val profileNow = profile.value
                if (!AiGateway.configured(profileNow)) {
                    _lastError.value = "Asystent nie jest skonfigurowany. W Ustawieniach podaj adres bramki AI — klucz zostaje na serwerze."
                    _lastRetryable.value = false
                    return@launch
                }
                val convoId = ensureConversation()
                repo.addConversationMessage(convoId, "user", clean, Provenance.user)
                requestCompletion(convoId)
            } catch (e: Exception) {
                _lastError.value = "Nie udało się zapisać wiadomości: ${e.message ?: "błąd bazy danych"}"
                _lastRetryable.value = true
                _sending.value = false
            }
        }
    }

    /** Ponów ostatnią nieodpowieaną wypowiedź użytkownika (bez duplikowania wpisu). */
    fun retry() {
        val convoId = _selectedId.value ?: return
        if (_sending.value) return
        if (messages.value.lastOrNull()?.role != "user") return // nie ma czego ponawiać
        sendJob = viewModelScope.launch {
            val profileNow = profile.value
            if (!AiGateway.configured(profileNow)) {
                _lastError.value = "Asystent nie jest skonfigurowany."
                _lastRetryable.value = false
                return@launch
            }
            requestCompletion(convoId)
        }
    }

    fun cancel() {
        sendJob?.cancel()
        sendJob = null
        _sending.value = false
    }

    private suspend fun ensureConversation(): String {
        val existing = _selectedId.value
        if (existing != null) return existing
        val created = repo.createConversation(mode = profile.value.aiDefaultMode, sourceThoughtId = _sourceThoughtId.value)
        _selectedId.value = created.id
        return created.id
    }

    private suspend fun requestCompletion(convoId: String) {
        _sending.value = true
        _lastError.value = null
        try {
            val history = repo.messagesOnce(convoId)
                .takeLast(24)
                .map { it.role to it.content }
            val convo = repo.conversationOnce(convoId)
            val mode = convo?.mode ?: AssistantMode.impuls
            val reply = AiGateway.sendMessage(
                profile = profile.value,
                conversationId = convoId,
                mode = mode,
                messages = history,
                currentThought = currentThoughtForSend.value,
                contextItems = contextItems.value,
            )
            val content = reply.content.trim()
            if (content.isEmpty()) {
                _lastError.value = "Bramka zwróciła pustą odpowiedź."
                _lastRetryable.value = true
            } else {
                repo.addConversationMessage(convoId, "assistant", content, Provenance.ai, reply.model)
            }
        } catch (e: AiGatewayException) {
            _lastError.value = e.message
            _lastRetryable.value = e.retryable
        } catch (e: Exception) {
            _lastError.value = e.message ?: "Nieoczekiwany błąd asystenta."
            _lastRetryable.value = true
        } finally {
            _sending.value = false
        }
    }

    fun previewNow() {
        if (_previewing.value) return
        viewModelScope.launch {
            val profileNow = profile.value
            if (!AiGateway.configured(profileNow)) {
                _lastError.value = "Asystent nie jest skonfigurowany — nie ma czego podglądać."
                return@launch
            }
            _previewing.value = true
            try {
                val convoId = _selectedId.value
                val mode = convoId?.let { repo.conversationOnce(it)?.mode } ?: profileNow.aiDefaultMode
                val history = convoId?.let { repo.messagesOnce(it).takeLast(24).map { m -> m.role to m.content } } ?: emptyList()
                _preview.value = AiGateway.previewContext(
                    profile = profileNow,
                    mode = mode,
                    messages = history,
                    currentThought = currentThoughtForSend.value,
                    contextItems = contextItems.value,
                )
            } catch (e: AiGatewayException) {
                _lastError.value = e.message
                _lastRetryable.value = e.retryable
            } catch (e: Exception) {
                _lastError.value = e.message ?: "Nie udało się pobrać podglądu kontekstu."
            } finally {
                _previewing.value = false
            }
        }
    }

    /** Jawne przeniesienie odpowiedzi modelu do koncepcji (nigdy automatyczne). */
    fun saveAssistantMessageAsConcept(message: AssistantMessage) {
        viewModelScope.launch {
            val convo = _selectedId.value?.let { repo.conversationOnce(it) }
            repo.saveAiTextAsThought(message.content, convo?.projectId)
            savedEvents.tryEmit("Zapisano jako koncepcję (proweniencja: AI).")
        }
    }
}
