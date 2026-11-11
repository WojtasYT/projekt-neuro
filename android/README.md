# Neurotrino Lab — moduł Android

Otwórz **ten katalog** w Android Studio.

- applicationId: `lab.neurotrino.app`
- minSdk 26, targetSdk 36
- Gradle 8.13 / AGP 8.13 / Kotlin 2.2.21

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Jeśli nie ma `gradle/wrapper/gradle-wrapper.jar`, Android Studio wygeneruje wrapper przy pierwszym sync.

Prototyp PWA leży poziom wyżej i nie jest częścią tego builda.
