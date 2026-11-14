# CURRENT_ARCHITECTURE — stan przed przebudową V4 (2026-11-14)

```
PWA (korzeń: Vite/React/Dexie)  ← zaparkowany prototyp referencyjny
android/ :app
  data/ai/AiGateway.kt        → POST profil.gatewayUrl {mode, thought…} → {text}   (kontrakt ad-hoc)
  data/db/NeurotrinoDatabase  → Room v1 (fallbackToDestructiveMigration)
  data/db/Daos.kt             → thoughts/projects/relationships/reading/sources/attachments/profile/meta
  data/db/Entities.kt         → 8 encji
  data/db/Mappers.kt          → domain↔row (+ AttachmentMeta.toRow)
  data/export/LabExport.kt    → format neurotrino-lab v1 (DTO ↔ domain)
  data/repo/LabRepository.kt  → Flow<…> + operacje; eksport/import; backup-proweniencyjny w meta
  domain/                     → Models.kt (Thought/Project/Relationship/ReadingItem/SourceItem/Spark…),
                                SparksCatalog.kt (iskry lokalne, pick()), RelativeTime.kt
  ui/ShellViewModel           → AndroidViewModel; stateIn(Flows); capture/update/link/import/export/wipe
  ui/navigation/AppNav        → NavHost; 5 tabów (Laboratorium/Myśli/Połączenia/Projekty/Iskry) + Studio/…
  ui/{lab,thoughts,studio,graph,explore,projects,library,settings,capture,components,theme}
.github/workflows/android.yml → build+test+lint+APK (paths android/**)
```

## Szczeliny względem docelowej architektury V4
| Obszar | Stan obecny | V4 oczekuje |
|---|---|---|
| Rozmowy | **brak** | Conversation/Message, historia, tryby |
| Backend | **brak** | gateway + adapter + health-check + contract |
| Kontrakt AI | ad-hoc | `/health /v1/ai/status /v1/models /v1/chat/completions /v1/context/preview` |
| Migracje | v1, destructive fallback | v1→v2, jawne `MIGRATION_1_2`, bez fallbacku |
| Eksport | v1, bez rozmów | v1+sekcje rozmów (v2 plik, import wstecznie zgodny) |
| Kontekst AI | surowy `thought` | aktualna wersja, budżet, priorytety, podgląd, wykluczenia |
| Health check UI | statyczny tekst | rzeczywiste GET z wynikiem |
| Tryby | 7 AiModes (enum exists, unused in prod path) | impuls/rozwiniecie/analiza/badanie/adwersarz mapowane na system prompty |
