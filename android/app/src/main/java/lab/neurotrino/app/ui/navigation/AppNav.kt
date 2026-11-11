package lab.neurotrino.app.ui.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.SparksCatalog
import lab.neurotrino.app.ui.ShellViewModel
import lab.neurotrino.app.ui.capture.CaptureSheet
import lab.neurotrino.app.ui.capture.Onboarding
import lab.neurotrino.app.ui.explore.ExploreScreen
import lab.neurotrino.app.ui.graph.GraphScreen
import lab.neurotrino.app.ui.lab.LabScreen
import lab.neurotrino.app.ui.library.BookScreen
import lab.neurotrino.app.ui.library.LibraryScreen
import lab.neurotrino.app.ui.projects.ProjectDetailScreen
import lab.neurotrino.app.ui.projects.ProjectsScreen
import lab.neurotrino.app.ui.settings.SettingsScreen
import lab.neurotrino.app.ui.studio.StudioScreen
import lab.neurotrino.app.ui.thoughts.ThoughtsScreen
import lab.neurotrino.app.ui.theme.Cyan
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
    fun studio(id: String) = "studio/$id"
    fun project(id: String) = "project/$id"
    fun book(id: String) = "book/$id"
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
    val profile by vm.profile.collectAsStateWithLifecycle()
    val resume by vm.resume.collectAsStateWithLifecycle()
    val captureOpen by vm.captureOpen.collectAsState()
    val draft by vm.captureDraft.collectAsState()
    val ready by vm.readyState.collectAsState()
    val nav = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(incomingText) {
        if (!incomingText.isNullOrBlank()) vm.handleIncomingText(incomingText)
    }
    LaunchedEffect(openCapture) { if (openCapture) vm.openCapture() }
    LaunchedEffect(openResume, resume?.id) {
        if (openResume) resume?.id?.let { nav.navigate(Routes.studio(it)) }
    }

    if (!ready) {
        lab.neurotrino.app.ui.components.LabField { Text(stringResource(R.string.loading)) }
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
        floatingActionButton = {
            if (showBar) {
                FloatingActionButton(onClick = { vm.openCapture() }, containerColor = Cyan) {
                    Text("+", color = lab.neurotrino.app.ui.theme.Graphite)
                }
            }
        },
        bottomBar = {
            if (showBar) {
                NavigationBar {
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
                            label = { Text(stringResource(label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Routes.Lab, modifier = Modifier.padding(padding)) {
            composable(Routes.Lab) {
                LabScreen(
                    thoughts, projects, reading, profile.lastMindState, online,
                    onMind = { vm.setMind(it) },
                    onCapture = { text -> vm.capture(text, { nav.navigate(Routes.studio(it)) }, stay = false) },
                    onOpenCapture = { vm.openCapture() },
                    onResume = { nav.navigate(Routes.studio(it)) },
                    onSpark = { nav.navigate(Routes.Sparks) },
                    onProject = { nav.navigate(Routes.project(it)) },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                    onSettings = { nav.navigate(Routes.Settings) },
                )
            }
            composable(Routes.Thoughts) {
                ThoughtsScreen(thoughts, onOpen = { nav.navigate(Routes.studio(it)) }, onCapture = { vm.openCapture() })
            }
            composable(Routes.Studio, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val thought = thoughts.find { it.id == id }
                var attachments by remember { mutableStateOf(emptyList<lab.neurotrino.app.domain.AttachmentMeta>()) }
                LaunchedEffect(id) {
                    vm.attachmentsFlow(id).collect { attachments = it }
                }
                StudioScreen(
                    thought = thought,
                    missing = thought == null && thoughts.isNotEmpty(),
                    projects = projects,
                    allThoughts = thoughts,
                    relationships = relationships,
                    attachments = attachments,
                    gatewayConfigured = profile.gatewayUrl.isNotBlank(),
                    online = online,
                    onBack = { nav.popBackStack() },
                    onChange = { vm.updateThought(it) },
                    onTouch = { vm.touch(id) },
                    onDelete = { vm.deleteThought(id); nav.popBackStack() },
                    onExpand = { text, prov -> vm.addExpansion(id, text, prov) },
                    onLink = { to, type, why -> vm.addLink(id, to, lab.neurotrino.app.domain.GraphKind.thought, lab.neurotrino.app.domain.GraphKind.thought, type, why) },
                    onUnlink = { vm.removeLink(it) },
                    onPickImage = { uri, name, mime, size -> vm.addImage(id, uri, name, mime, size) },
                    onAskAi = { mode, th -> vm.askAi(mode, th) },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                )
            }
            composable(Routes.Graph) {
                GraphScreen(thoughts, projects, reading, relationships, { nav.navigate(Routes.studio(it)) }, { nav.navigate(Routes.project(it)) }, { vm.removeLink(it) })
            }
            composable(Routes.Projects) {
                ProjectsScreen(projects, onOpen = { nav.navigate(Routes.project(it)) }, onCreate = { t, p -> vm.createProject(t, p) })
            }
            composable(Routes.Project, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val project = projects.find { it.id == id }
                ProjectDetailScreen(
                    project, thoughts,
                    onBack = { nav.popBackStack() },
                    onChange = { p, note -> vm.updateProject(p, note) },
                    onAttach = { vm.attachToProject(it, id) },
                    onOpenThought = { nav.navigate(Routes.studio(it)) },
                    onDelete = { vm.deleteProject(id); nav.popBackStack() },
                    onCapture = { vm.openCapture() },
                )
            }
            composable(Routes.Sparks) {
                ExploreScreen(
                    thoughts, reading,
                    onSave = { spark ->
                        vm.capture(SparksCatalog.body(spark), { nav.navigate(Routes.studio(it)) }, stay = false)
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
                        vm.capture("Notatka z lektury: ${book?.title.orEmpty()}", { nav.navigate(Routes.studio(it)) }, false)
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
            onSaveOpen = { vm.capture(draft, { nav.navigate(Routes.studio(it)) }, stay = false) },
            onSaveLeave = { vm.capture(draft, {}, stay = true) },
            onDismiss = { vm.closeCapture() },
        )
    }
}

private fun writeUri(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
}

private fun readUri(context: Context, uri: Uri): String =
    context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it)).readText() }
        ?: throw IllegalArgumentException("Nie udało się odczytać pliku.")
