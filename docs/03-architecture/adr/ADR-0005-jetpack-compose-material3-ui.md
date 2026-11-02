# ADR-0005 — Build the screens with Jetpack Compose and Material 3
- **Status:** Proposed
- **Date:** 2026-11-02
- **Decided by:** Owner
- **Approval evidence:** none yet. Only the Owner approves, with an `approve:` commit. The Owner chose this option on 2026-11-02 when asked; development of milestone H0 started on that instruction.

## Context
- ADR-0001 fixes a native Android app in Kotlin and leaves the UI toolkit open ("the UI toolkit ... left to implementation", DOC-CMP).
- The UX framework (DOC-UXF) and the mockups use Material 3 patterns: bottom navigation of four destinations, floating action button, dialogs, switches (OI-044).
- Android documentation, verified 2026-11-02 at developer.android.com/develop/ui/compose/why-adopt: Jetpack Compose is "Android's declarative UI toolkit" and is "replacing the View toolkit"; Google considers the View toolkit and the View-based Jetpack libraries, including Material Design Components (Views), to be in maintenance mode; new Android Studio UI tools are built for Compose only; documentation and samples are Compose-first.
- The Owner is more fluent in Kotlin (ADR-0001), which Compose is written in.

## Decision
Build all screens with Jetpack Compose and Material 3 components, in a single activity. The interface follows DOC-UXF and OI-044. Text uses `sp` so it follows the phone's font size (NFR-009). A light color scheme only; the dark theme is not adopted (OI-044 d).

## Alternatives considered
- **Classic XML Views.** Discarded: in maintenance mode per the documentation above; more boilerplate; the Material components for Views are also in maintenance mode.
- **Compose Multiplatform or a cross-platform UI.** Discarded: iOS is out of scope (NFR-006).

## Consequences
- Positive: the toolkit Google is investing in; fewer files than XML; previews help review screens against the mockups.
- Negative: Compose needs a compatible Kotlin compiler plugin version; large version upgrades have to be done together with Kotlin; some UI behavior (for example the notification and permission flows) still uses Android system APIs.
- Risk: library versions are chosen at implementation and recorded in the version catalog (`gradle/libs.versions.toml`); they are the agent's choice, verified only by a successful build.

## Refs
NFR-006 · NFR-009 · ADR-0001 · DOC-UXF · OI-044
Sources: [Why adopt Compose](https://developer.android.com/develop/ui/compose/why-adopt)
