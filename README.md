# Simple Calculator Android App

Minimal Android calculator sample (Kotlin).

How to run:

- Open the `calculator-app` folder in Android Studio.
- Let Android Studio sync/upgrade Gradle if requested.
- Run on an emulator or connected device.
 - Optionally build from the command line using the Gradle wrapper.
	 - If you have Gradle installed, run `gradle wrapper` in the project root to generate the wrapper JAR.
	 - After the wrapper JAR exists (or if you committed it), run the wrapper on Windows:

```powershell
./gradlew.bat assembleDebug
```

Or on macOS/Linux:

```bash
./gradlew assembleDebug
```

Files created:
- `app/src/main/java/com/example/calculator/MainActivity.kt` — app logic
- `app/src/main/res/layout/activity_main.xml` — UI
- `app/src/main/AndroidManifest.xml` — manifest
- Gradle files for a minimal project

Wrapper files added:
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties` (run `gradle wrapper` to generate the wrapper JAR if needed)

Notes:
- Android Studio may prompt to add Gradle wrapper or update plugin versions; follow its suggestions to build and run.