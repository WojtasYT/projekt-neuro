# INSTALLATION_GUIDE_PL — NeuroLab (Android)

## 1. Co instalujesz

APK debug (artefakt z CI): GitHub → Actions → ostatni zielony przebieg workflow **Android** → artifacts → `app-debug` (exp. 2027-02-10 dla builda z 2026-11-14).
Albo zbuduj samodzielnie (patrz §4).

## 2. Instalacja APK

1. Pobierz `app-debug.apk` na telefon.
2. Ustawienia → Aplikacje / Bezpieczeństwo → Zezwól na instalację z nieznanych źródeł dla przeglądarki/menedżera plików.
3. Otwórz plik APK → Zainstaluj.
4. Uruchom **NeuroLab** (ikona z nazwą aplikacji).

Wymagania: Android 8.0+ (minSdk 26).

## 3. Pierwsze uruchomienie

1. Przejdź onboarding (30 sekund).
2. (Opcjonalnie) Ustawienia → Asystent:
   - **Adres bramki AI** — patrz BACKEND_DEPLOYMENT_PL.md, żeby wystawić własną bramkę.
   - **Model** — domyślny lub wybrany z listy po naciśnięciu „Pobierz listę modeli”.
   - **„Sprawdź połączenie”** — realnie woła `GET /health`; zobaczysz: połączono / dostawca niedostępny / brak klucza.
   - Bez bramki aplikacja działa w 100% offline (bez AI). Nic nie udaje czatu.
3. Gotowe. Asystent dostępny: ikona ✦ w Laboratorium oraz Studio → „Otwórz w Asystencie”.

## 4. Build ze źródeł

```bash
git clone https://github.com/WojtasYT/projekt-neuro.git
cd projekt-neuro/android
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
Wymagania: JDK 17, Android SDK (platform 36, build-tools zgodne z AGP), internet przy pierwszym sync.

Testy i lint (jak w CI):
```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
python3 scripts/verify_migration_1_2.py
```

## 5. Aktualizacja

Zainstaluj nowsze APK — z tą samą sygnaturą debug bez odinstalowywania. **Room migruje automatycznie (v1→v2)**; danych nie trzeba eksportować przed aktualizacją. Dla świętego spokoju: Ustawienia → Eksport przed pierwszą aktualizacją na nowy major.

## 6. Rozwiązywanie problemów

| Problem | Rozwiązanie |
|---|---|
| "Aplikacja nie zainstalowana" | odinstaluj starą wersję (jeśli sygnatury się różnią) lub użyj tego samego źródła APK |
| Asystent: "Asystent nie jest skonfigurowany" | Ustawienia → Asystent → podaj Adres bramki AI |
| Asystent: "Nie udało się połączyć z bramką" | czy serwer działa? czy ten sam network/VPN? http vs https? |
| „Dostawca nieosiągalny” w health check | sprawdź klucz i status dostawcy po stronie bramki (`/health`) |
| Błąd importu | plik musi być JSON-em formatu `neurotrino-lab` (v1 albo v2); eksport z NeuroLab zawsze się wczyta |
