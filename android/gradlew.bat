@echo off
set DIR=%~dp0
set JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
if not exist "%JAR%" (
  echo Brak gradle-wrapper.jar. Otworz android\ w Android Studio.
  exit /b 1
)
java -jar "%JAR%" %*
