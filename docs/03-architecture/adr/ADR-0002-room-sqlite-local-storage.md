# ADR-0002 — Store all data in a local Room (SQLite) database as the only source of truth
- **Status:** Proposed
- **Date:** 2026-11-01
- **Decided by:** Owner
- **Approval evidence:** none yet. Only the Owner approves, with an `approve:` commit.

## Context
- All user data is stored on the device and is the only source of truth; no backend (NFR-002, OI-021, CTX-001 §7).
- Data to keep: tenants, properties, rentals and reminder settings; amounts are whole colones stored as integers (NFR-008, OI-027).
- Data must survive closing the app and restarting the phone (NFR-003).
- Deleting a tenant must delete their rentals, and a rental must link exactly one tenant and one property (FR-006, FR-008, OI-029), so relations and constraints are useful.
- Backup is a JSON file built by the app, not a copy of the database file (FR-027, FR-028, FR-029, NFR-005, OI-037). Importing must replace all data or change nothing, which calls for transactions.
- ADR-0001 proposes a native Kotlin Android app.
- Per the Android guide on offline-first apps, a local data source is the canonical source of truth and higher layers read only from it; it lists Room, DataStore and simple files as options (see sources).
- Per the Android guide on local databases, Room is a layer over SQLite that checks queries at compile time, reduces boilerplate and provides migration paths, and the guide recommends it instead of using the SQLite APIs directly.
- Room 3.0 exists as a new package (`androidx.room3`) that requires a SQLite driver, and the Room 2.x guide is marked legacy. Which version to use is left to implementation, to check against the current release notes.
- SQLite fits device-local storage; its limits are one writer at a time and simultaneous network access (sqlite.org "Appropriate Uses", cited in OI-021). A single-user app with no network access is within those limits.

## Decision
Use a Room database on SQLite as the single local store for tenants, properties, rentals and reminder settings. The rest of the app reads and writes through it. Amounts are stored as integers. Imports and cascading deletes run inside a transaction. Schema changes use Room migrations, and the backup file carries its own schema version (OI-037) separate from the database version.

## Alternatives considered
- **Android SQLite APIs directly.** Discarded: more boilerplate and no compile-time query checks; the platform guide recommends Room instead.
- **Preferences DataStore or plain files only.** Discarded for the main data: no relations, queries or transactions, which the cascading deletes and the all-or-nothing import need. DataStore remains acceptable for a few UI preferences if the Owner prefers to keep them out of the database.
- **Realm (Kotlin SDK).** Discarded: MongoDB announced the end of life of Atlas Device Sync and the Device SDKs for 30 September 2025 and states the on-device database continues as an open-source project; the vendor change adds uncertainty with no benefit here, since there is no sync.
- **JSON file as the database.** Discarded: whole-file rewrites, no integrity guarantees, and it would blur the line with the backup format.
- **Cloud database (Firestore or similar).** Discarded: against the offline-first constraint and the no-backend rule (OI-021).

## Consequences
- Positive: recommended path in the platform documentation; relations and cascades enforced by the database; transactions make the import atomic (NFR-005); migrations are supported.
- Negative: schema changes need migration code; the database is not encrypted (OI-019, OI-040); entity, DAO and backup-JSON mapping must be kept in sync.
- Risk, unverified: Android may copy app data to the user's Google account through its automatic backup, which would contradict NFR-002. Tracked as OI-042; to verify in the Android documentation before implementation.

## Refs
FR-006 · FR-008 · FR-027 · FR-028 · FR-029 · NFR-002 · NFR-003 · NFR-005 · NFR-008 · ADR-0001 · OI-019 · OI-021 · OI-037 · OI-042
Sources: [Save data in a local database using Room](https://developer.android.com/training/data-storage/room) · [Room 3.0](https://developer.android.com/jetpack/androidx/releases/room3) · [Build an offline-first app](https://developer.android.com/topic/architecture/data-layer/offline-first) · [MongoDB Device SDKs deprecation](https://www.mongodb.com/docs/atlas/device-sdks/deprecation.md)
