#!/usr/bin/env python3
"""
Weryfikacja migracji Room 1→2 bez emulatora (V4 dopuszcza 'static SQL diff verification').

1. Tworzy bazę ze schematem v1 (DDL odpowiada encjom z wersji 1, sprzed V4).
2. Wstawia dane do wszystkich tabel v1.
3. Stosuje DOKŁADNIE ten sam DDL, który wykonuje MIGRATION_1_2 w Kotlinie.
4. Sprawdza: wszystkie wiersze v1 nietknięte, nowe tabele istnieją,
   FK CASCADE działa (usunięcie rozmowy kasuje jej wiadomości).

Uruchom:  python3 android/scripts/verify_migration_1_2.py
Wyjście 0 = PASS, 1 = FAIL.
"""
import sqlite3
import sys

V1_DDL = [
    "CREATE TABLE thoughts (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, rawText TEXT NOT NULL,"
    " content TEXT NOT NULL, typesJson TEXT NOT NULL, status TEXT NOT NULL, tagsJson TEXT NOT NULL,"
    " createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, lastOpenedAt TEXT NOT NULL, provenance TEXT NOT NULL,"
    " projectIdsJson TEXT NOT NULL, sourceIdsJson TEXT NOT NULL, readingItemIdsJson TEXT NOT NULL,"
    " isSample INTEGER NOT NULL, hypothesisJson TEXT NOT NULL, expansionsJson TEXT NOT NULL)",
    "CREATE TABLE projects (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, purpose TEXT NOT NULL,"
    " description TEXT NOT NULL, status TEXT NOT NULL, nextStep TEXT NOT NULL, createdAt TEXT NOT NULL,"
    " updatedAt TEXT NOT NULL, isSample INTEGER NOT NULL, historyJson TEXT NOT NULL)",
    "CREATE TABLE relationships (id TEXT NOT NULL PRIMARY KEY, fromId TEXT NOT NULL, toId TEXT NOT NULL,"
    " fromKind TEXT NOT NULL, toKind TEXT NOT NULL, relationType TEXT NOT NULL, explanation TEXT NOT NULL,"
    " provenance TEXT NOT NULL, confidence TEXT, createdAt TEXT NOT NULL)",
    "CREATE TABLE reading (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL,"
    " status TEXT NOT NULL, progressNote TEXT NOT NULL, currentPage TEXT NOT NULL, notes TEXT NOT NULL,"
    " isStarter INTEGER NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL)",
    "CREATE TABLE sources (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, authors TEXT NOT NULL,"
    " year TEXT NOT NULL, publisher TEXT NOT NULL, doi TEXT NOT NULL, url TEXT NOT NULL,"
    " sourceType TEXT NOT NULL, notes TEXT NOT NULL, accessedAt TEXT NOT NULL, createdAt TEXT NOT NULL)",
    "CREATE TABLE attachments (id TEXT NOT NULL PRIMARY KEY, thoughtId TEXT NOT NULL, name TEXT NOT NULL,"
    " mime TEXT NOT NULL, uri TEXT NOT NULL, createdAt TEXT NOT NULL, size INTEGER NOT NULL)",
    "CREATE TABLE profile (id TEXT NOT NULL PRIMARY KEY, displayName TEXT NOT NULL, interestsJson TEXT NOT NULL,"
    " currentFocus TEXT NOT NULL, onboarded INTEGER NOT NULL, theme TEXT NOT NULL, aiDefaultMode TEXT NOT NULL,"
    " gatewayUrl TEXT NOT NULL, model TEXT NOT NULL, remindersEnabled INTEGER NOT NULL,"
    " lastMindState TEXT, notesToSelf TEXT NOT NULL, updatedAt TEXT NOT NULL)",
    "CREATE TABLE meta (`key` TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)",
]

# Dokładnie te same instrukcje co MIGRATION_1_2 w NeurotrinoDatabase.kt
MIGRATION_1_2 = [
    "CREATE TABLE IF NOT EXISTS `conversations` ("
    "`id` TEXT NOT NULL, `title` TEXT NOT NULL, `projectId` TEXT, "
    "`sourceThoughtId` TEXT, `mode` TEXT NOT NULL, `archived` INTEGER NOT NULL, "
    "`createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, "
    "PRIMARY KEY(`id`))",
    "CREATE TABLE IF NOT EXISTS `messages` ("
    "`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `role` TEXT NOT NULL, "
    "`content` TEXT NOT NULL, `model` TEXT, `provenance` TEXT NOT NULL, `createdAt` TEXT NOT NULL, "
    "PRIMARY KEY(`id`), "
    "FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
    "CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)",
    "CREATE INDEX IF NOT EXISTS `index_conversations_updatedAt` ON `conversations` (`updatedAt`)",
]

SEED = {
    "thoughts": ("INSERT INTO thoughts VALUES ('th1','t','raw','content','[]','seed','[]','a','b','c','user','[]','[]','[]',0,'{}','[]')", 1),
    "projects": ("INSERT INTO projects VALUES ('p1','proj','purpose','desc','active','next','a','b',0,'[]')", 1),
    "relationships": ("INSERT INTO relationships VALUES ('r1','th1','p1','thought','project','belongs_to_project','expl','user',NULL,'a')", 1),
    "reading": ("INSERT INTO reading VALUES ('b1','book','author','unopened','note','1','notes',1,'a','b')", 1),
    "sources": ("INSERT INTO sources VALUES ('s1','src','auth','2024','pub','doi','url','paper','notes','acc','a')", 1),
    "attachments": ("INSERT INTO attachments VALUES ('att1','th1','name','image/png','uri','a',123)", 1),
    "profile": ("INSERT INTO profile VALUES ('local','name','[]','focus',1,'dark','iskra','url','model',0,NULL,'notes','a')", 1),
    "meta": ("INSERT INTO meta VALUES ('seeded','true')", 1),
}


def main() -> int:
    conn = sqlite3.connect(":memory:")
    conn.execute("PRAGMA foreign_keys = ON")
    cur = conn.cursor()
    for ddl in V1_DDL:
        cur.execute(ddl)
    for table, (sql, _) in SEED.items():
        cur.execute(sql)
    # migracja
    for stmt in MIGRATION_1_2:
        cur.execute(stmt)
    # 1) wiersze v1 nienaruszone
    for table in SEED:
        (count,) = cur.execute(f"SELECT COUNT(*) FROM {table}").fetchone()
        status = "OK " if count == 1 else "FAIL"
        print(f"[{status}] tabela v1 '{table}': {count} wiersz(y) po migracji")
        if count != 1:
            return 1
    # 2) nowe tabele istnieją
    for table in ("conversations", "messages"):
        (count,) = cur.execute(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)
        ).fetchone()
        print(f"[{'OK ' if count == 1 else 'FAIL'}] nowa tabela '{table}' istnieje po migracji")
        if count != 1:
            return 1
    # 3) round-trip: rozmowa + wiadomości + CASCADE
    cur.execute("INSERT INTO conversations VALUES ('cv1','Rozmowa',NULL,'th1','impuls',0,'2026-11-14T00:00:00Z','2026-11-14T00:00:00Z')")
    cur.execute("INSERT INTO messages VALUES ('m1','cv1','user','pytanie',NULL,'user','2026-11-14T00:00:00Z')")
    cur.execute("INSERT INTO messages VALUES ('m2','cv1','assistant','odpowiedź','model-x','ai','2026-11-14T00:00:01Z')")
    (count,) = cur.execute("SELECT COUNT(*) FROM messages").fetchone()
    print(f"[{'OK ' if count == 2 else 'FAIL'}] round-trip wiadomości: {count}/2")
    if count != 2:
        return 1
    cur.execute("DELETE FROM conversations WHERE id='cv1'")
    (count,) = cur.execute("SELECT COUNT(*) FROM messages").fetchone()
    print(f"[{'OK ' if count == 0 else 'FAIL'}] ON DELETE CASCADE: po skasowaniu rozmowy zostało {count} wiadomości")
    if count != 0:
        return 1
    # 4) indeksy
    (count,) = cur.execute(
        "SELECT COUNT(*) FROM sqlite_master WHERE type='index' AND name='index_messages_conversationId'"
    ).fetchone()
    print(f"[{'OK ' if count == 1 else 'FAIL'}] indeks index_messages_conversationId")
    if count != 1:
        return 1
    print("\nWYNIK: PASS — migracja 1→2 nie narusza danych, FK działa.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
