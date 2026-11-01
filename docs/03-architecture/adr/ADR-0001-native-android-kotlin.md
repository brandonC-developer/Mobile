# ADR-0001 — Build the app as a native Android app in Kotlin
- **Status:** Proposed
- **Date:** 2026-11-01
- **Decided by:** Owner
- **Approval evidence:** none yet. Only the Owner approves, with an `approve:` commit.

## Context
Verified facts and constraints:
- The target is Android 13 or later only, installed by APK, for one user, the Owner (NFR-006, OI-018, OI-038). iOS is out of scope.
- Core functions work without Internet and data lives on the device (NFR-001, NFR-002, OI-021).
- Reminders must arrive within 5 minutes of the chosen time (NFR-004). Android 12 and later require the exact-alarm permission, which Android 14 denies by default to new apps, and Expo notes that without it notifications arrive at an approximate time (sources below). Behavior after a forced close or a reboot is unverified (OI-039).
- The Owner is more fluent in Kotlin than in JavaScript or TypeScript (stated 2026-11-01).
- The technologies in CTX-001 §8 (React Native, Expo, TypeScript, SQLite) are preliminary references, not decisions (OI-021).
- Comparisons found are blog posts of low to medium reliability; they agree that native avoids a cross-platform layer for an Android-only app and that React Native or Flutter pay off mainly when iOS is needed or the team already knows JavaScript or Dart. None of them compared scheduled local notifications across stacks.

## Decision
Build the app as a native Android application in Kotlin, using Android's own APIs for storage, scheduling and notifications. Specific libraries (UI toolkit, database, dependency injection) are decided in later ADRs; ADR-0002 will cover local storage.

## Alternatives considered
- **React Native with Expo and TypeScript.** Listed in CTX-001. Discarded as the first choice because iOS is out of scope, the Owner is less fluent in it, and delivery of exact-time reminders would depend on what the library exposes (OI-039). Remains viable if the prototype showed no problem and the Owner prefers it; it would then need a new ADR.
- **Flutter (Dart).** Discarded: same cross-platform layer without the benefit of iOS, and a language the Owner did not mention.
- **Progressive web app or web view.** Discarded: reliable exact-time local reminders without a server are not available in a browser context; not verified here, listed as a risk to check if proposed again.
- **Kotlin Multiplatform.** Discarded: its value is code shared with iOS, which is out of scope.

## Consequences
- Positive: direct access to AlarmManager and notification APIs for NFR-004; one language the Owner knows; no cross-platform runtime; precedent in open-source Kotlin apps for offline-first rent tracking and local reminders (to be inspected, not copied).
- Negative: adding iOS later means rewriting the app; the Android toolchain (Android Studio, Gradle) is required.
- Open: exact-alarm behavior, forced close and reboot must be tested on the Owner phone before relying on it (OI-039). A short prototype of reminders is recommended before ADR-0003.

## Refs
NFR-001 · NFR-002 · NFR-004 · NFR-006 · FR-021 · FR-023 · OI-018 · OI-021 · OI-039
Sources: [Android: exact alarms denied by default](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms) · [Android: schedule alarms](https://developer.android.com/develop/background-work/services/alarms) · [Expo Notifications](https://docs.expo.dev/versions/latest/sdk/notifications/) · [Rent Management System (Kotlin, offline-first)](https://github.com/pootinx/NestHub) · [Daily Advice (Kotlin, Room, AlarmManager, WorkManager; MIT)](https://github.com/Bejussi/DailyAdvice)
