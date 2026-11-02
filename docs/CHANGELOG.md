# Documentation Changelog

Format: one entry per approved change, citing IDs.

## [Unreleased]
### Added
- ADR-0005 (Jetpack Compose and Material 3) drafted as Proposed; Owner chose it 2026-11-02. Milestone H0 built: Gradle project in `app/`, Compose placeholder activity, Room dependencies, no permissions, Android backup blocked, unit tests for TC-026 pass. OI-045 logs the technical values chosen by the agent. DOC-DEV 0.1 drafted.
- Milestone H2 built: screens for rentals, tenants and properties in Jetpack Compose with the four-destination bottom bar (OI-044 a, c partly, e), Spanish strings, repository with atomic rules (FR-001 to FR-012), 9 device tests of the repository and 6 unit tests of formats and validation. Week, Calendar and Settings show "Próximamente" until H3 and H4; the WhatsApp message of OI-044 c arrives with the contact actions in H5.
- Milestone H1 built: domain rules and collection calculator (FR-013, FR-014, FR-015, FR-017, AC-FR-007-2, AC-FR-007-3, FR-008) with 23 unit tests (TC-005, TC-006, TC-009, TC-011, TC-013, TC-031), Room entities ENT-001 to ENT-004 with a device test of the cascade and restrict rules (4 tests, emulator API 37).
- CHG-001 (proposed, Owner instruction 2026-11-02): rentals get a `startDate` set at creation so the previous-week view of FR-016 cannot show collections that never existed. FR-013 modified (new sentence and AC-FR-013-3), ENT-003 gains `startDate`, TC-031 added. Approval pending: needs an Owner `approve: CHG-001` commit.
- OI-044 resolved by the Owner (accepted a, c, e; b and d not adopted).
- Approval: Owner commit `a1bc3ba` approved DOC-VIS, DOC-GLO, DOC-FR, DOC-NFR, ADR-0001 to ADR-0004, DOC-DAT, DOC-CMP, DOC-INT, DOC-UXF, DOC-SEC, DOC-TST, DOC-ARC, DOC-RSK, DOC-UC (2026-11-02). Editorial follow-up: status notes at the top of those documents updated; no requirement or decision text changed.
- DOC-UC 0.1 (UC-001 to UC-015 with sequence diagrams) and DOC-UXF 0.1 (UX framework from Nielsen, Laws of UX, Fogg, Google touch-target guidance) drafted by the agent, Proposed; OI-044 logged for five UX proposals. A roadmap page with mockups was published as an Artifact.
- DOC-ARC 0.1 (architecture, VW-001 to VW-003) and DOC-RSK 0.1 (RSK-001 to RSK-014) drafted by the agent, Proposed, not approved.
- OI-043 resolved by the Owner (all four proposals accepted); DOC-DAT, DOC-INT, DOC-SEC updated.
- Phase 6: DOC-DAT 0.1 (ENT-001 to ENT-004), DOC-CMP 0.1 (CMP-001 to CMP-009), DOC-INT 0.1 (INT-001 to INT-008), DOC-SEC 0.1 (SEC-001 to SEC-008) and DOC-TST 0.1 (TC-001 to TC-030) drafted by the agent, Proposed, not approved. OI-043 logged for four small decisions found while drafting.
- ADR-0004 (versioned JSON backup, validated fully before import) drafted by the agent as Proposed. Not approved.
- OI-042 resolved by the Owner: block all Android backup and device transfer (ADR-0002 updated).
- OI-042 verified in the Android documentation (Auto Backup is on by default and would copy the database to Google Drive); mitigation proposed in ADR-0002, Owner decision pending.
- ADR-0003 "Verification results": the seven device tests of OI-039 were run on the Owner phone with a throwaway app; OI-039 set to "Verified on device, pending Owner review". Decision unchanged; ADR still Proposed.
- ADR-0003 (exact alarms for reminders, rebuilt on every relevant event) drafted by the agent as Proposed, with a device verification plan (OI-039). Not approved.
- ADR-0002 (Room over SQLite as the local store) drafted by the agent as Proposed; OI-042 logged (Android automatic backup vs NFR-002). Not approved.
- Phase 5: ADR-0001 (native Android app in Kotlin) drafted by the agent as Proposed, with alternatives. Not approved.
- Phase 4: DOC-FR 0.1 (FR-001 to FR-029) and DOC-NFR 0.1 (NFR-001 to NFR-010) drafted by the agent, MoSCoW priorities proposed, traced to OBJ-001 to OBJ-005 and to the resolved open items. OI-041 logged for the assumptions they contain and resolved by the Owner as drafted. Proposed, not approved.
- Phase 3: DOC-VIS 0.1 (vision, objectives OBJ-001 to OBJ-005, scope, constraints) and DOC-GLO 0.1 (glossary) drafted by the agent from the resolved open items. Proposed, not approved.

### Changed
- DOC-PEN 0.4: OI-032 resolved (tenant deletion cascades to its rentals; property with active rentals cannot be deleted); OI-033 logged as assumption; "active/occupied" rental defined by the Owner (related tenant with obligations); deleting a tenant never deletes a property; properties are never deleted but can be edited, and become free when a rental ends; tenants can be edited at any time; OI-033 resolved, OI-034 logged and resolved (ended rentals kept until their tenant is deleted); OI-029 resolved (one tenant per property at a time, tenant represents a household); OI-035 logged and resolved (tenants and free properties can be reused in new rentals); OI-025 resolved (weekly summary shows the collections due that week); OI-015 resolved (week is Monday to Sunday, collections shown in the week their date falls in; previous, current and next week viewable, opens on current); OI-036 logged and resolved (previous week only in the summary); OI-014 resolved (collections computed on demand, rolling window for notifications); OI-016 resolved (backup by exported file, no cloud), OI-037 logged and resolved (JSON with schema version, full replace on import); OI-018 resolved (Android only, personal use), OI-038 logged and resolved (direct APK, Android 13 minimum); OI-017 resolved (reminders within 5 minutes of the chosen time; permission flow on first rental), OI-039 logged and partly verified (notification and exact-alarm permissions); OI-019 resolved (phone screen lock is enough), OI-040 logged and resolved (unencrypted backup with export warning); OI-020 resolved (accessibility: respects phone font size; currency is CRC, whole amounts without cents, interface in Spanish only); OI-008 resolved (MoSCoW priorities).
- DOC-PEN 0.3: OI-021, OI-022 resolved; CTX-002 logged; primary user is the landlord (OI-026), superseding CTX-001 §3; OI-023 superseded; OI-025 to OI-028 added.

### Added
- Phase 2: initial project context received (CTX-001), logged unprocessed. Pending register DOC-PEN 0.2: OI-009 resolved; OI-010 to OI-020 taken from the source's own pending list; OI-021 to OI-024 detected ambiguities.

### Changed
- DOC-GOV 0.2 and AGENTS.md: approval evidence distinguishes Owner from agent commits; no rank between ADRs and requirements; Owner instructions that contradict approved documents must be formalized; editing an Approved document returns it to In review; element statuses defined; empty template means information not provided.

### Added
- Initial documentation skeleton (phase 1): folders, empty templates, governance, traceability rules, agent instructions.
