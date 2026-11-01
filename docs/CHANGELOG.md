# Documentation Changelog

Format: one entry per approved change, citing IDs.

## [Unreleased]
### Added
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
