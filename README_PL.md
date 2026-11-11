# Neurotrino Lab — instrukcja

## Co to jest

Natywna aplikacja Android: zapis myśli, pracownia, powiązania, projekty, iskry, biblioteka. Dane zostają na telefonie (Room).

## Jak zbudować APK

1. Zainstaluj Android Studio (Ladybug / nowsze) i JDK 17.
2. **File → Open** i wskaż katalog `android/` (nie korzeń PWA).
3. Poczekaj na sync Gradle.
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)** albo:

```bash
cd android
./gradlew :app:assembleDebug
```

Plik: `android/app/build/outputs/apk/debug/app-debug.apk`.

Jeśli brakuje `gradle/wrapper/gradle-wrapper.jar`, Android Studio wygeneruje wrapper przy otwarciu projektu, albo:

```bash
gradle wrapper --gradle-version 8.13
```

## Testy

```bash
cd android
./gradlew :app:testDebugUnitTest
./gradlew :app:lint
# UI (emulator):
./gradlew :app:connectedDebugAndroidTest
```

## Import z prototypu przeglądarkowego

Zobacz `docs/MIGRACJA_PWA.md`.

## Asystent

Ustawienia → Adres serwera asystenta. Klucza API nie wklejaj do aplikacji.
