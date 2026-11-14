package lab.neurotrino.app

import lab.neurotrino.app.domain.AssistantContextItem
import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.SourceItem
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.buildContextItems
import lab.neurotrino.app.domain.conversationTitle
import lab.neurotrino.app.domain.missingConversationRefs
import lab.neurotrino.app.domain.normalizeGatewayBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AssistantDomainTest {

    private fun thought(id: String, title: String, content: String, sourceIds: List<String> = emptyList(), projectIds: List<String> = emptyList()) = Thought(
        id = id, title = title, rawText = content, content = content,
        types = emptyList(), status = ThoughtStatus.seed, tags = emptyList(),
        createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-01T00:00:00Z", lastOpenedAt = "2026-01-01T00:00:00Z",
        provenance = Provenance.user, projectIds = projectIds, sourceIds = sourceIds, readingItemIds = emptyList(),
    )

    private fun project(id: String, title: String) = Project(
        id = id, title = title, purpose = "cel", description = "opis",
        status = ProjectStatus.active, nextStep = "krok",
        createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-01T00:00:00Z",
    )

    private fun source(id: String, title: String) = SourceItem(
        id = id, title = title, authors = "A. Autor", createdAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun contextItemsPrioritisesProjectThenSourcesThenRelated() {
        val th = thought("t1", "Myśl", "treść", sourceIds = listOf("s1", "s2"), projectIds = listOf("p1"))
        val related = (1..6).map { thought("r$it", "Powiązana $it", "związana treść $it") }
        val items = buildContextItems(
            thought = th,
            linkedThoughts = related,
            projects = listOf(project("p1", "Projekt X")),
            sources = listOf(source("s1", "Źródło A"), source("s2", "Źródło B")),
        )
        assertEquals("project", items[0].kind)
        assertEquals("Projekt X", items[0].title)
        assertEquals(listOf("source:s1", "source:s2"), items.filter { it.kind == "source" }.map { it.id })
        assertEquals(4, items.count { it.kind == "thought" }) // limit 4 powiązań
    }

    @Test
    fun contextItemsDedupesByNormalisedText() {
        val th = thought("t1", "Myśl", "treść")
        val dup = listOf(
            thought("r1", "A", "Ta sama  treść"),
            thought("r2", "B", "ta sama treść"),
        )
        val items = buildContextItems(th, dup, emptyList(), emptyList())
        assertEquals(1, items.size)
    }

    @Test
    fun contextItemsRespectExclusions() {
        val th = thought("t1", "Myśl", "treść", sourceIds = listOf("s1"), projectIds = listOf("p1"))
        val items = buildContextItems(
            thought = th, linkedThoughts = emptyList(),
            projects = listOf(project("p1", "Projekt X")),
            sources = listOf(source("s1", "Źródło A")),
            excluded = setOf("project:p1", "source:s1"),
        )
        assertTrue(items.isEmpty())
    }

    @Test
    fun conversationTitleFallsBackForBlankInput() {
        assertEquals("Nowa rozmowa", conversationTitle(""))
        assertEquals("pytanie o czas", conversationTitle("pytanie o czas\ndruga linia"))
        assertTrue(conversationTitle("x".repeat(200)).length <= 64)
    }

    @Test
    fun normalizeGatewayBaseStripsKnownSuffixes() {
        assertEquals("http://192.168.0.10:8787", normalizeGatewayBase("http://192.168.0.10:8787/"))
        assertEquals("http://192.168.0.10:8787", normalizeGatewayBase("http://192.168.0.10:8787/v1/chat/completions"))
        assertEquals("https://gw.example.com", normalizeGatewayBase("https://gw.example.com/v1"))
        assertEquals("https://gw.example.com", normalizeGatewayBase("https://gw.example.com/health"))
        assertEquals("", normalizeGatewayBase("   "))
    }

    @Test
    fun missingConversationRefsFindsOrphans() {
        assertEquals(listOf("duch"), missingConversationRefs(listOf("a", "duch", "duch", "b"), setOf("a", "b")))
        assertTrue(missingConversationRefs(listOf("a"), setOf("a")).isEmpty())
    }

    @Test
    fun assistantModesMatchBackendContract() {
        // Te nazwy są częścią kontraktu z backendem — nie zmieniać bez zmiany bramki.
        assertEquals(
            listOf("impuls", "rozwiniecie", "analiza", "badanie", "adwersarz"),
            AssistantMode.entries.map { it.name },
        )
    }

    @Test
    fun randomConversationIdsAreUnique() {
        val ids = (1..50).map { UUID.randomUUID().toString() }.toSet()
        assertEquals(50, ids.size)
    }
}
