# Neurotrino Lab

Prywatne laboratorium myśli. **Produkt główny to natywna aplikacja Android** (`android/`).  
PWA w katalogu głównym to zachowany prototyp.

## Android (docelowo)

Wymagania: Android Studio, JDK 17, Android SDK 36.

```bash
cd android
# Pierwsze otwarcie: File → Open → android/
# Studio dociągnie Gradle wrapper, jeśli brakuje gradle-wrapper.jar
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

APK: `android/app/build/outputs/apk/debug/app-debug.apk`

Szczegóły po polsku: `README_PL.md`, migracja z PWA: `docs/MIGRACJA_PWA.md`.

## Prototyp PWA (nie rozwijać jako produktu)

```bash
npm install
npm run dev
npm test
```

## Prywatność

Brak konta, telemetrii i chmury. Asystent wymaga własnej bramki serwerowej; bez niej nie udaje odpowiedzi.
