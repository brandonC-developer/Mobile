# ADR-0004 — Back up to a versioned JSON file that is validated fully before it replaces any data
- **Status:** Approved
- **Date:** 2026-11-01
- **Decided by:** Owner
- **Approval evidence:** Owner commit starting `approve: ADR-0001, ADR-0002, ADR-0003, ADR-0004` (see git log).

## Context
- The backup is one JSON file with a schema version, shared through the device share menu and restored by importing it; no cloud integration (FR-027, FR-028, OI-016, OI-037).
- The file is not encrypted; the app warns before export (FR-027, OI-040).
- A file that is not valid, or has an unknown or newer version, is rejected and the data stays unchanged (FR-029). An import replaces all data or changes nothing (NFR-005).
- Export then import on a clean install must give the same data (AC-FR-028-3), and reminders are rebuilt after import (FR-028, ADR-0003).
- Android's automatic backup and device-to-device transfer are disabled (ADR-0002, OI-042), so this file is the only way to move data to a new phone.
- Reminder dates are computed from the rental rule, and rentals have no payment status (FR-013), so there is nothing derived to store.
- The entities are defined in the data model (DOC-DAT, ENT-001 to ENT-004). The field list below comes from the requirements and follows that document.

## Decision
1. **Container.** One UTF-8 JSON object:
   - `format`: fixed text identifying the app's backup, so a foreign JSON file is rejected early.
   - `schemaVersion`: integer, starting at 1, independent of the Room database version (ADR-0002).
   - `exportedAt`: ISO-8601 UTC timestamp, informative only.
   - `tenants`, `properties`, `rentals`: arrays; `settings`: object with the reminder offsets in days (3, 2, 1, 0 by default) and the reminder time.
2. **Content rules.** Each record has its own integer `id`; a rental refers to its tenant and property by those ids. Amounts are whole colones as integers; dates are `yyyy-MM-dd`; the reminder time is `HH:mm`. No derived data, no statuses, no stored reminders.
3. **Export.** Read all data in one consistent read, build the JSON, write it to a temporary file, show the plain-text warning, then offer the file through the share menu. The suggested name is `apartment-backup-<date>.json` (agent's call).
4. **Import, in this order.** Read and parse the file; check `format` and `schemaVersion`; migrate an older version in memory, one step at a time; validate every field and relation (types, required fields, non-negative integer amounts, payment day 1 to 31, valid dates, no id repeated, each rental pointing at an existing tenant and property, a tenant and a property in at most one open rental each, per FR-006 and OI-029); ask for explicit confirmation that current data will be lost; replace everything in a single database transaction; rebuild the reminders (ADR-0003). Any failure before the transaction changes nothing, and a failure inside it rolls back.
5. **Versions.** A `schemaVersion` greater than the app supports, or unknown, is rejected. Any change to a field name, type or meaning raises the version and adds a migration step. Reading old versions is kept as long as the app supports them.
6. **Unknown extra fields** at a supported version are ignored (agent's call, to be confirmed by the Owner).
7. **Where the file is read from.** The import uses the system file picker, so the app asks for no storage permission (to confirm against the Android documentation at implementation).
8. **JSON library** is left to implementation; kotlinx.serialization is the agent's recommendation as the Kotlin-native option, to verify then.

## Alternatives considered
- **Copy of the SQLite database file.** Discarded: ties the backup to Room's internal schema and version, may involve extra journal files, and cannot be validated field by field (OI-037).
- **CSV.** Discarded: three related collections and settings do not fit a flat file, and relations would be lost.
- **Encrypted file with a password.** Discarded by the Owner (OI-040): a lost password would make the backup unrecoverable.
- **Android Auto Backup or Google Drive.** Discarded: data would leave the device outside the landlord's action (NFR-002, OI-016, OI-042).
- **Export rentals as nested inside tenants.** Not chosen: a tenant with many ended rentals and a property referenced from several rentals are clearer as separate arrays with ids.

## Consequences
- Positive: readable file the Owner can inspect; validation before any write makes the import safe (NFR-005); the version field allows future change; independent of the storage engine.
- Negative: the file holds personal data in plain text and can be shared by mistake (OI-040, mitigated only by the warning); every change to stored data needs a matching change in the export, the import validation and a migration step; keeping old versions readable has a maintenance cost.
- Risk: an interrupted export leaves a partial temporary file, which must not be offered for sharing; to test with valid, invalid and truncated files (NFR-005).

## Refs
FR-006 · FR-013 · FR-027 · FR-028 · FR-029 · NFR-002 · NFR-005 · ADR-0002 · ADR-0003 · OI-016 · OI-029 · OI-037 · OI-040 · OI-042
