package lab.neurotrino.app.ui.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.AttachmentMeta
import lab.neurotrino.app.domain.SparksCatalog
import lab.neurotrino.app.ui.ShellViewModel
import lab.neurotrino.app.ui.capture.CaptureSheet
import lab.neurotrino.app.ui.capture.Onboarding
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.LabMark
import lab.neurotrino.app.ui.explore.ExploreScreen
import lab.neurotrino.app.ui.graph.GraphScreen
import lab.neurotrino.app.ui.lab.LabScreen
import lab.neurotrino.app.ui.library.BookScreen
import lab.neurotrino.app.ui.library.LibraryScreen
import lab.neurotrino.app.ui.projects.ProjectDetailScreen
import lab.neurotrino.app.ui.projects.ProjectsScreen
import lab.neurotrino.app.ui.assistant.AssistantScreen
import lab.neurotrino.app.ui.assistant.AssistantViewModel
import lab.neurotrino.app.ui.settings.SettingsScreen
import lab.neurotrino.app.ui.studio.StudioScreen
import lab.neurotrino.app.ui.thoughts.ThoughtsScreen
import java.io.BufferedReader
import java.io.InputStreamReader

object Routes {
    const val Lab = "lab"
    const val Thoughts = "thoughts"
    const val Studio = "studio/{id}"
    const val Graph = "graph"
    const val Projects = "projects"
    const val Project = "project/{id}"
    const val Sparks = "sparks"
    const val Library = "library"
    const val Book = "book/{id}"
    const val Settings = "settings"
    const val Assistant = "assistant/{thoughtId}"
    fun studio(id: String) = "studio/$id"
    fun project(id: String) = "project/$id"
    fun book(id: String) = "book/$id"
    fun assistant(thoughtId: String? = null) = "assistant/${thoughtId ?: "none"}"
}

@Composable
fun NeurotrinoRoot(
    vm: ShellViewModel,
    openCapture: Boolean,
    openResume: Boolean,
    incomingText: String?,
    online: Boolean,
) {
    val thoughts by vm.thoughts.collectAsStateWithLifecycle()
    val projects by vm.projects.collectAsStateWithLifecycle()
    val relationships by vm.relationships.collectAsStateWithLifecycle()
    val reading by vm.reading.collectAsStateWithLifecycle()
    val sources by vm.sources.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val resume by vm.resume.collectAsStateWithLifecycle()
    val captureOpen by vm.captureOpen.collectAsStateWithLifecycle()
    val draft by vm.captureDraft.collectAsStateWithLifecycle()
    val ready by vm.readyState.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(incomingText) {
        if (!incomingText.isNullOrBlank()) vm.handleIncomingText(incomingText)
    }
    LaunchedEffect(openCapture) { if (openCapture) vm.openCapture() }
    LaunchedEffect(openResume, resume?.id) {
        if (openResume) resume?.id?.let { nav.navigate(Routes.studio(it)) }
    }

    if (error != null) {
        FatalError(message = error!!, onRetry = { vm.retryAfterError() })
        return
    }
    if (!ready) {
        BootScreen()
        return
    }
    if (!profile.onboarded) {
        Onboarding(onFinish = { vm.finishOnboarding() })
        return
    }

    val tabs = listOf(
        Triple(Routes.Lab, R.string.nav_lab, Icons.Outlined.Science),
        Triple(Routes.Thoughts, R.string.nav_thoughts, Icons.Outlined.Notes),
        Triple(Routes.Graph, R.string.nav_links, Icons.Outlined.Hub),
        Triple(Routes.Projects, R.string.nav_projects, Icons.Outlined.WorkOutline),
        Triple(Routes.Sparks, R.string.nav_sparks, Icons.Outlined.AutoAwesome),
    )
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Routes.Lab
    val showBar = current in tabs.map { it.first }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (showBar) {
                ExtendedFloatingActionButton(
                    onClick = { vm.openCapture() },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    text = { Text(stringResource(R.string.action_capture)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                nav.navigate(route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = stringResource(label)) },
                            label = { Text(stringResource(label), style = MaterialTheme.typography.labelMedium) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            nav,
            startDestination = Routes.Lab,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 14 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 14 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 14 } },
        ) {
            composable(Routes.Lab) {
                LabScreen(
                    thoughts, projects, reading, profile.lastMindState, online,
                    displayName = profile.displayName,
                    onMind = { vm.setMind(it) },
                    onCapture = { text -> vm.capture(text, { id -> id?.let { nav.navigate(Routes.studio(it)) } }, stay = false) },
                    onOpenCapture = { vm.openCapture() },
                    onResume = { nav.navigate(Routes.studio(it)) },
                    onSpark = { nav.navigate(Routes.Sparks) },
                    onProject = { nav.navigate(Routes.project(it)) },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                    onSettings = { nav.navigate(Routes.Settings) },
                    onLibrary = { nav.navigate(Routes.Library) },
                    onAssistant = { nav.navigate(Routes.assistant(null)) },
                )
            }
            composable(Routes.Assistant, arguments = listOf(navArgument("thoughtId") { type = NavType.StringType })) { entry ->
                val thoughtId = entry.arguments?.getString("thoughtId")?.takeIf { it != "none" }
                val assistantVm: AssistantViewModel = viewModel()
                AssistantScreen(
                    thoughtId = thoughtId,
                    vm = assistantVm,
                    onBack = { nav.popBackStack() },
                    onOpenSettings = { nav.navigate(Routes.Settings) },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                )
            }
            composable(Routes.Thoughts) {
                ThoughtsScreen(thoughts, onOpen = { nav.navigate(Routes.studio(it)) }, onCapture = { vm.openCapture() })
            }
            composable(Routes.Studio, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val thought = thoughts.find { it.id == id }
                val attachments by vm.attachmentsFlow(id).collectAsStateWithLifecycle(emptyList())
                StudioScreen(
                    thought = thought,
                    missing = thought == null && thoughts.isNotEmpty(),
                    projects = projects,
                    allThoughts = thoughts,
                    relationships = relationships,
                    attachments = attachments,
                    sources = sources,
                    gatewayConfigured = profile.gatewayUrl.isNotBlank(),
                    online = online,
                    onBack = { nav.popBackStack() },
                    onChange = { vm.updateThought(it) },
                    onTouch = { vm.touch(id) },
                    onDelete = { vm.deleteThought(id) },
                    onDeleted = { nav.popBackStack() },
                    onExpand = { text, prov -> vm.addExpansion(id, text, prov) },
                    onLink = { to, type, why -> vm.addLink(id, to, lab.neurotrino.app.domain.GraphKind.thought, lab.neurotrino.app.domain.GraphKind.thought, type, why) },
                    onUnlink = { vm.removeLink(it) },
                    onAttachProject = { projectId, attach ->
                        if (attach) vm.attachToProject(id, projectId) else vm.detachFromProject(id, projectId)
                    },
                    onPickImage = { uri, name, mime, size -> vm.addImage(id, uri, name, mime, size) },
                    onDeleteAttachment = { vm.deleteAttachment(it) },
                    onAddSource = { t, a, u, d, n -> vm.addSource(t, a, u, d, n) },
                    onAttachSource = { vm.attachSource(id, it) },
                    onDetachSource = { vm.detachSource(id, it) },
                    onDeleteSource = { vm.deleteSource(it) },
                    onAskAi = { mode, th -> vm.askAi(mode, th) },
                    onOpenAssistant = { nav.navigate(Routes.assistant(id)) },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                    onOpenProject = { nav.navigate(Routes.project(it)) },
                )
            }
            composable(Routes.Graph) {
                GraphScreen(
                    thoughts, projects, reading, relationships,
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                    onOpenProject = { nav.navigate(Routes.project(it)) },
                    onOpenBook = { nav.navigate(Routes.book(it)) },
                    onUnlink = { vm.removeLink(it) },
                )
            }
            composable(Routes.Projects) {
                ProjectsScreen(
                    projects,
                    onOpen = { nav.navigate(Routes.project(it)) },
                    onCreate = { t, p -> vm.createProject(t, p) },
                )
            }
            composable(Routes.Project, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val project = projects.find { it.id == id }
                ProjectDetailScreen(
                    project, thoughts,
                    onBack = { nav.popBackStack() },
                    onChange = { p, note -> vm.updateProject(p, note) },
                    onAttach = { thoughtId -> vm.attachToProject(thoughtId, id) },
                    onDetach = { thoughtId -> vm.detachFromProject(thoughtId, id) },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                    onDelete = { vm.deleteProject(id); nav.popBackStack() },
                    onCapture = { vm.openCapture() },
                )
            }
            composable(Routes.Sparks) {
                ExploreScreen(
                    thoughts, reading,
                    onSave = { spark ->
                        vm.capture(SparksCatalog.body(spark), { id -> id?.let { nav.navigate(Routes.studio(it)) } }, stay = false)
                    },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                )
            }
            composable(Routes.Library) {
                LibraryScreen(reading, onOpen = { nav.navigate(Routes.book(it)) }, onAdd = { t, a -> vm.addBook(t, a) })
            }
            composable(Routes.Book, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val book = reading.find { it.id == id }
                BookScreen(
                    book, thoughts,
                    onBack = { nav.popBackStack() },
                    onChange = { vm.updateBook(it) },
                    onNote = {
                        vm.capture(
                            "Notatka z lektury: ${book?.title.orEmpty()}\n\n",
                            { id -> id?.let { nav.navigate(Routes.studio(it)) } },
                            stay = false,
                        )
                    },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                    onDelete = { vm.deleteBook(id); nav.popBackStack() },
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    profile,
                    onChange = { vm.saveProfile(it) },
                    onExport = { vm.exportJson() },
                    onImport = { raw, replace -> vm.importJson(raw, replace) },
                    onWriteExport = { uri, json -> writeUri(context, uri, json) },
                    onReadImport = { uri -> readUri(context, uri) },
                    onRemoveSamples = { vm.removeSamples() },
                    onWipe = { vm.wipe() },
                    onLibrary = { nav.navigate(Routes.Library) },
                )
            }
        }
    }

    if (captureOpen) {
        CaptureSheet(
            draft = draft,
            mind = profile.lastMindState,
            onDraft = { vm.setDraft(it) },
            onMind = { vm.setMind(it) },
            onSaveOpen = { vm.capture(draft, { id -> id?.let { nav.navigate(Routes.studio(it)) } }, stay = false) },
            onSaveLeave = { vm.capture(draft, {}, stay = true) },
            onDismiss = { vm.closeCapture() },
        )
    }
}

@Composable
private fun BootScreen() {
    LabField {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            LabMark(size = 46.dp)
            Text(
                text = stringResource(R.string.loading),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 18.dp),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun FatalError(message: String, onRetry: () -> Unit) {
    LabField {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            LabMark(size = 44.dp)
            Text(
                text = stringResource(R.string.error_db_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

private fun writeUri(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        ?: throw IllegalArgumentException("Nie udało się zapisać pliku.")
}

private fun readUri(context: Context, uri: Uri): String =
    context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it)).readText() }
        ?: throw IllegalArgumentException("Nie udało się odczytać pliku.")
