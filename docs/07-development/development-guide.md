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
