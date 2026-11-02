---
id: DOC-DEV
title: Development Guide
status: Draft
version: 0.1
date: 2026-11-02
approved_by:
refs: [ADR-0001, ADR-0002, ADR-0003, ADR-0004, ADR-0005, DOC-CMP]
---

# Development Guide

> **Status:** Draft written by the agent during milestone H0. Not approved.

## Environment
- Windows, Android Studio, Android SDK with platform 34 and build-tools (installed on the Owner machine).
- **JDK 17** for Gradle (`C:\Program Files\Java\jdk-17`). The JDK bundled with the newest Android Studio (25) failed with this Gradle and Kotlin version. In Android Studio: Settings > Build Tools > Gradle > Gradle JDK = 17.
- `local.properties` (git-ignored) holds `sdk.dir`, with forward slashes: `sdk.dir=C:/Users/<user>/AppData/Local/Android/Sdk`.

## Project layout
- Gradle root at the repository root; one module `app/` (ADR-0001, Owner instruction 2026-11-02).
- Package `app.apartment.mobile`; screens in Jetpack Compose with Material 3 (ADR-0005).
- Library versions live in `gradle/libs.versions.toml`.

## Commands
```
JAVA_HOME="C:\Program Files\Java\jdk-17" ./gradlew :app:testDebugUnitTest :app:assembleDebug
```
The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Install with `adb install -r`.

## Rules
- Code traces to approved requirements; cite the FR, NFR, TC or ADR in comments only where it explains a rule that is not obvious.
- Write the test of a behavior before its code where the behavior is pure logic (collections, validation, backup).
- No permission is added to the manifest without an approved requirement; the Internet permission is never declared (TC-026).
- Milestones H0 to H6 follow the roadmap; a milestone ends when its test cases pass on the Owner phone.

## Emulator
- The Android Studio virtual device `Medium_Phone_API_37.0` (Android API 37, x86_64) boots and runs the debug APK (checked 2026-11-02).
- With the default graphics backend it went offline in `adb` after boot on the Owner machine; starting it with software rendering was stable: `emulator -avd Medium_Phone_API_37.0 -no-snapshot -no-audio -gpu swiftshader_indirect`.
- Use it for screens and logic. Reminder timing, reboot and permission behavior (ADR-0003) were verified on the Owner phone (Android 13) and must be re-checked there: the emulator is API 37, not the target Android 13.

## Tests
- Unit tests (JVM, no device): `./gradlew :app:testDebugUnitTest`. Domain logic lives in `app/src/main/java/app/apartment/mobile/domain` with no Android dependency.
- Device tests (Room schema, cascade): start the emulator or connect the phone, then `ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest`.
- Room writes each schema version to `app/schemas/`; commit it with every schema change.
- Screen tests use Compose test rules (`WeekCalendarUiTest`). The Espresso version pulled in by the Compose test library failed on Android API 37 (`InputManager.getInstance` missing); `espresso-core` 3.7.0 is declared explicitly in `gradle/libs.versions.toml` to fix it.
- Delivery test (real alarm, about two minutes): `ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.delivery=true -Pandroid.testInstrumentationRunnerArguments.class=app.apartment.mobile.reminders.ReminderDeliveryTest`. It prints `REMINDER-DELIVERY ... delaySeconds=` in the device log. Run it on the phone with the screen off to test with the phone idle.
- If Gradle reports `Failed to create MD5 hash` for the connected-test results folder, stop the Gradle daemons and delete `app/build/outputs/androidTest-results`. If the emulator shows `offline` in adb, restart it with `-gpu swiftshader_indirect`.

## Roadmap page
- The Owner follows progress in a roadmap page (an Artifact). Its source is `docs/09-management/roadmap.html`.
- **After every commit, update the roadmap page** (Owner instruction, 2026-11-02): status of the phases and milestones, test counts, pending decisions, last commit. Then publish it again at the same address.

## QA checklist (run before closing a milestone and at each sync)
1. Every ID cited in the documents exists (`grep` the IDs against their definitions).
2. Every AC has a test case; every CMP traces to a requirement; statuses in the test plan match what was run.
3. All tests pass: unit, device, screen. Lint has no errors.
4. No user text is hardcoded outside `res/values/strings.xml` (NFR-007).
5. Documents that say "to be verified" are checked against the open items.
6. Approved documents are changed only through a CHG.
- Scenarios that need the test to end first (reboot, forced stop; TC-019): install the app and test APKs with `adb install -r`, then `adb shell am instrument -w -e scenario <name> -e minutes <n> -e class app.apartment.mobile.reminders.ReminderScenarioSetupTest app.apartment.mobile.debug.test/androidx.test.runner.AndroidJUnitRunner`. It seeds a rental with one same-day reminder and leaves the data in place.
- Release file: `./gradlew :app:assembleRelease` (signing key and passwords: DOC-OPS section 2).

## Technology assessments
- Before adopting a library, SDK, model or service, use the project skill `tech-fit-assessment` (`.claude/skills/tech-fit-assessment/`). It produces a report in `docs/03-architecture/assessments/` covering compatibility with the approved documents, price and licence, software load, optimization, processing power of the Owner phone and a test plan against the project. The first one is DOC-TFA-001 (local assistant, OI-049).
- `scripts/device_probe.sh [-s <serial>] [<package>]` in the skill folder reads chipset, cores, memory, storage, battery and app size from a connected device. It only reads. It was checked for the no-device case; run it on the phone for the first real probe.

## Test policy: emulator first, then the phone, without damage (Owner rule, 2026-11-02)
1. **Stage 1, no risk to the phone:** JVM unit tests and the emulator (`./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest` with `ANDROID_SERIAL=emulator-5554`). A change is not tried on the phone until stage 1 passes.
2. **Stage 2, the physical phone:** the same suites on the moto g72, then the checks only the phone can answer (real alarms, reboot, permission dialogs, WhatsApp, file picker, font size, speed, memory). Reports say which stage each result comes from.
3. **The test app is a different app.** The debug build installs as `app.apartment.mobile.debug`, named "Apartment (debug)", with its own data. The signed release (`app.apartment.mobile`) is the app the Owner uses and is never the target of a test, so a test cannot wipe real data. Both can be installed together.
4. **Care on the phone:** do not uninstall, clear data, reboot, force-stop or change system settings (font size, airplane mode, permissions) without telling the Owner first; restore what was changed; ask for an exported backup before anything that could touch the release app; avoid screenshots of personal screens (messages, file lists) and prefer shell checks.
5. **Emulator tips:** it needs about 4 GB of free RAM (stop Gradle daemons first); if `adb` shows it offline, restart it with `-gpu swiftshader_indirect`.
