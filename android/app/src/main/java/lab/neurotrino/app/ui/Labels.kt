package lab.neurotrino.app.ui

import androidx.annotation.StringRes
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.MindState
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.ReadingStatus
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType

@StringRes fun ThoughtType.labelRes() = when (this) {
    ThoughtType.observation -> R.string.type_observation
    ThoughtType.question -> R.string.type_question
    ThoughtType.hypothesis -> R.string.type_hypothesis
    ThoughtType.model -> R.string.type_model
    ThoughtType.analogy -> R.string.type_analogy
    ThoughtType.invention -> R.string.type_invention
    ThoughtType.reflection -> R.string.type_reflection
    ThoughtType.project_note -> R.string.type_project_note
    ThoughtType.reading_note -> R.string.type_reading_note
    ThoughtType.other -> R.string.type_other
}

@StringRes fun ThoughtStatus.labelRes() = when (this) {
    ThoughtStatus.seed -> R.string.status_seed
    ThoughtStatus.exploring -> R.string.status_exploring
    ThoughtStatus.paused -> R.string.status_paused
    ThoughtStatus.connected -> R.string.status_connected
    ThoughtStatus.checking -> R.string.status_checking
    ThoughtStatus.developed -> R.string.status_developed
    ThoughtStatus.archived -> R.string.status_archived
}

@StringRes fun RelationType.labelRes() = when (this) {
    RelationType.supports -> R.string.rel_supports
    RelationType.contradicts -> R.string.rel_contradicts
    RelationType.extends -> R.string.rel_extends
    RelationType.analogy -> R.string.rel_analogy
    RelationType.shared_mechanism -> R.string.rel_shared_mechanism
    RelationType.inspired_by -> R.string.rel_inspired_by
    RelationType.source_for -> R.string.rel_source_for
    RelationType.belongs_to_project -> R.string.rel_belongs_to_project
    RelationType.metaphor -> R.string.rel_metaphor
    RelationType.weak_association -> R.string.rel_weak_association
}

@StringRes fun RelationType.hintRes() = when (this) {
    RelationType.supports -> R.string.rel_hint_supports
    RelationType.contradicts -> R.string.rel_hint_contradicts
    RelationType.extends -> R.string.rel_hint_extends
    RelationType.analogy -> R.string.rel_hint_analogy
    RelationType.shared_mechanism -> R.string.rel_hint_shared_mechanism
    RelationType.inspired_by -> R.string.rel_hint_inspired_by
    RelationType.source_for -> R.string.rel_hint_source_for
    RelationType.belongs_to_project -> R.string.rel_hint_belongs
    RelationType.metaphor -> R.string.rel_hint_metaphor
    RelationType.weak_association -> R.string.rel_hint_weak
}

@StringRes fun ProjectStatus.labelRes() = when (this) {
    ProjectStatus.active -> R.string.project_status_active
    ProjectStatus.paused -> R.string.project_status_paused
    ProjectStatus.incubating -> R.string.project_status_incubating
    ProjectStatus.archived -> R.string.project_status_archived
}

@StringRes fun ReadingStatus.labelRes() = when (this) {
    ReadingStatus.unopened -> R.string.reading_unopened
    ReadingStatus.exploring -> R.string.reading_exploring
    ReadingStatus.paused -> R.string.reading_paused
    ReadingStatus.finished -> R.string.reading_finished
}

@StringRes fun Provenance.labelRes() = when (this) {
    Provenance.user -> R.string.prov_user
    Provenance.ai -> R.string.prov_ai
    Provenance.source -> R.string.prov_source
    Provenance.joint -> R.string.prov_joint
    Provenance.sample -> R.string.prov_sample
}

@StringRes fun MindState.labelRes() = when (this) {
    MindState.fog -> R.string.mind_fog
    MindState.curiosity -> R.string.mind_curiosity
    MindState.lowEnergy -> R.string.mind_low_energy
    MindState.tooMany -> R.string.mind_too_many
    MindState.impulse -> R.string.mind_impulse
}

@StringRes fun MindState.hintRes() = when (this) {
    MindState.fog -> R.string.mind_hint_fog
    MindState.curiosity -> R.string.mind_hint_curiosity
    MindState.lowEnergy -> R.string.mind_hint_low_energy
    MindState.tooMany -> R.string.mind_hint_too_many
    MindState.impulse -> R.string.mind_hint_impulse
}

@StringRes fun AssistantMode.labelRes() = when (this) {
    AssistantMode.impuls -> R.string.ai_mode_impuls
    AssistantMode.rozwiniecie -> R.string.ai_mode_rozwiniecie
    AssistantMode.analiza -> R.string.ai_mode_analiza
    AssistantMode.badanie -> R.string.ai_mode_badanie
    AssistantMode.adwersarz -> R.string.ai_mode_adwersarz
}

@StringRes fun AssistantMode.hintRes() = when (this) {
    AssistantMode.impuls -> R.string.ai_hint_impuls
    AssistantMode.rozwiniecie -> R.string.ai_hint_rozwiniecie
    AssistantMode.analiza -> R.string.ai_hint_analiza
    AssistantMode.badanie -> R.string.ai_hint_badanie
    AssistantMode.adwersarz -> R.string.ai_hint_adwersarz
}
