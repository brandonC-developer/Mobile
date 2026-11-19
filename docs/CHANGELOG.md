# Documentation Changelog

Format: one entry per approved change, citing IDs.

## [Unreleased]
### Changed
- Documentation cleanup and synchronization (2026-11-19, `CHG-004` proposed): completed the 39-row traceability matrix in `DOC-TRC` 0.2; populated backward `Refs` across `FR-001` to `FR-029` (`DOC-FR` 0.2) and `NFR-001` to `NFR-010` (`DOC-NFR` 0.2) plus `CMP-006` (`DOC-CMP` 0.2); closed resolved debt/risk items `RSK-011`, `RSK-013` and `RSK-014` (`DOC-RSK` 0.2); updated `DOC-PEN` 0.5 (`OI-039`, `OI-048`), `DOC-TFA-001` 0.2 (measured `Qwen3-0.6B` verdict), `DOC-VIS` 0.2, `DOC-ARC` 0.2, `DOC-INT` 0.2, `DOC-TST` 0.2, `DOC-IDX` 0.2 and `roadmap.html`. Edited formerly-Approved documents were returned to `In review` per `DOC-GOV`.

### Added
- ADR-0005 (Jetpack Compose and Material 3) drafted as Proposed; Owner chose it 2026-11-02. Milestone H0 built: Gradle project in `app/`, Compose placeholder activity, Room dependencies, no permissions, Android backup blocked, unit tests for TC-026 pass. OI-045 logs the technical values chosen by the agent. DOC-DEV 0.1 drafted.
- Owner confirmed eliminar = terminar and borrar = permanent removal from the local database (stronger confirmation proposed).
- On-device model spike (Qwen3-0.6B, LiteRT-LM) measured on the moto g72: 11-13 s per answer on GPU, 38-57 s on CPU, 1.2-2.2 GB memory; Kotlin 1.9.24 cannot read the runtime without a flag; accuracy not yet measured (DOC-TST-AST-R2). Spike code on branch spike/local-model.
- Owner answers applied: quitar/eliminar/terminar = end the rental, borrar = separate delete step; "n fecha" was the correction "María paga los 28 ahora" (O10). Owner phrases 6/10; authored set 97.4% unchanged.
- Spike corrected: tenant and property names are a Roster input loaded from data (assistant-test-roster.psv), no names in the rules; Owner phrases now 4/9.
- Owner own phrases (9) run untuned on the rule baseline: 3/9 exact (1/5 on the certain labels), against 97.4% on the agent-written set; findings in DOC-TST-AST-R1 addendum.
- Rule baseline spike run on the JVM (DOC-TST-AST-R1): 97.4% intent on the agent-written test split, optimistic by construction; spike code under app/src/test/.../spike/ kept by Owner decision (2026-11-02) to repeat the measurement.
- Assistant phrase set (90 phrases, DOC-TST-AST) and request catalog DOC-TFA-001A added; OI-050 (paid mark) logged.
- Test policy recorded (development guide): emulator first, then the phone, with the debug build installed as the separate app `app.apartment.mobile.debug` ("Apartment (debug)") so tests never touch the release app and its data. Stage 1 passed on the emulator (74 unit, 60 device). Stage 2 passed on the moto g72 (60 device tests, 2 skipped by design, 0 failures) against the debug app only; the release app was not touched. Phone probe 2026-11-02: MT6789 (Helio G99), 8 cores (6 at 2.0 GHz, 2 at 2.2 GHz), 5.86 GB RAM total with about 2.2 GB available, 3.2 GB swap, 5.7 GB free storage (95% used), Android 13.
- DOC-TFA-001 written (assessment of a local assistant: fit, price, load, processing power, options, advantages and disadvantages, test plan) and the project skill `tech-fit-assessment` added under `.claude/skills/`; roadmap section added.
- OI-049 logged: research on a local assistant with a very small language model (nothing built; spike proposed).
- OI-045 to OI-048 resolved by the Owner. OI-048 applied as CHG-003 (proposed): the current week returns only after more than 5 minutes away; `HomeReset` rule, `appLeft` and `appReturned` in the view model, 3 unit tests and 4 view-model tests (TC-035). New release file built; installing it on the Owner phone is pending (phone disconnected).
- OI-045, OI-046 and OI-047 resolved by the Owner (accepted as is/as built).
- OI-045 resolved by the Owner (accepted as is).
- Milestone H6 (done, pending Owner decisions): reboot verified on the Owner phone (alarms rebuilt after one unlock without opening the app; reminder at 13:47:00; data persisted); signed release file 0.1.0 installed on the phone; TC-019, TC-027 and TC-028 passed; RSK-016 mitigated; DOC-OPS section 7 and an ADR-0003 verification note added. Earlier H6 work: release signing and DOC-OPS 0.1 written; on the Owner phone (moto g72, Android 13): device suite passes (56 tests) also in airplane mode; reminders arrive with the screen off (1 s) and in forced deep idle (0 s); a forced stop removes the alarms and reopening rebuilds them; real permission flow (explanation, system dialog, Alarms and reminders screen, warning on and off); WhatsApp chat and dialer opened from the app; backup imported through the real file picker. Defect found and fixed: with the largest font the bottom bar words split (NFR-009); the bar is now capped at 1.3 times. RSK-009 closed, RSK-017 mitigated, RSK-018 added; OI-048 logged (tab reset after short trips). Pending: reboot test (needs the Owner to unlock the phone), installing the signed release file.
- Milestone H5 built: contact (WhatsApp and call from the rental detail, only with a phone, message when WhatsApp is missing) and backup (versioned JSON, export with warning and share menu, import validated in full, confirmed and replaced in one transaction). 17 new unit tests (phone numbers, backup format), 13 new device tests (backup round trip and rollback, intents, files, screens). TC-021 to TC-024 recorded. OI-047 logs the details chosen by the agent.
- QA of 2026-11-02: cross-references and traceability checked; TC-032 to TC-034 added; statuses of 22 test cases recorded; CHG-001 and CHG-002 recorded (apply: FR-013, FR-021, NFR-004, ADR-0004, architecture); risks updated (RSK-005, RSK-010, RSK-012; RSK-015 to RSK-017 added); plural forms, launcher icon and unused strings fixed in the app; roadmap page kept in `docs/09-management/roadmap.html` and updated after every commit.
- Milestone H4 built: reminders (ADR-0003). Planner of the nearest 100 reminders plus a renewal reminder, exact alarms rebuilt from the database when the app opens and after every rental, tenant or settings change, at boot and when the exact-alarm permission changes; notification with property, tenant, amount and date that opens the current week; Settings screen with the four offsets and the time; permission explanation after the first rental and a non-blocking warning. 13 planner tests, 5 manifest tests, 7 device tests of the scheduler and notification, 5 screen tests of settings and warning. OI-046 logs the details chosen by the agent.
- Milestone H3 built: weekly summary (Monday to Sunday, previous/current/next only, total, grouped by day, empty state, always back to the current week after the app is left) and calendar (today and future only, month navigation not before the current month, amounts on selection); 7 new unit tests and 6 screen tests. FR-014 to FR-017, TC-012 to TC-014.
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

## Change records (CHG)
Changes to approved elements, as `governance.md` requires: reason, impact, approval. `CHG-001` to `CHG-003` were approved by the Owner commit `"approve: CHG-001, CHG-002, CHG-003, ADR-0005"` (`f9fcf3f`); `CHG-004` is proposed and awaiting Owner approval.

### CHG-001 — Rental start date
- **Status:** Approved (Owner commit "approve: CHG-001, CHG-002, CHG-003, ADR-0005").
- **Reason:** without a creation date, the previous-week view of FR-016 could show collections that never existed.
- **Changes:** FR-013 (new sentence and AC-FR-013-3), ENT-003 (`startDate`).
- **Impact:** CMP-003 and CMP-004 skip dates before the start date; TC-031 added; code in milestone H1.

### CHG-002 — Documentation sync after milestones H0 to H4 (QA of 2026-11-02)
- **Status:** Approved (Owner commit "approve: CHG-001, CHG-002, CHG-003, ADR-0005"). No requirement changes meaning.
- **Reason:** verified facts and built behavior replaced statements that said "to be verified" or "unwritten".
- **Changes:** ADR-0003 (verification of the built app on the phone appended); FR-021 (limit now stated: below 500 alarms, window of 100 plus renewal, OI-039, OI-046); FR "Not yet covered" (use cases exist); NFR-004 verification note (reboot and force stop verified); NFR "Not yet covered" (alarm limit verified); ADR-0004 context (data model exists); architecture solution table (ADR-0005 row, component-to-code map); test plan (TC statuses, TC-032 to TC-034 added so every AC has a test case); risk register (RSK-005, RSK-010 mitigated, RSK-012 closed, RSK-015 to RSK-017 added).
- **Impact:** none on code.

### CHG-003 — Return to the current week only after a real absence (OI-048)
- **Status:** Approved (Owner commit "approve: CHG-001, CHG-002, CHG-003, ADR-0005").
- **Reason:** on the Owner phone the app returned to the Semana tab after every short trip it started itself (file picker, permission screens), taking the landlord out of what they were doing.
- **Changes:** FR-016 description and AC-FR-016-1 reworded, AC-FR-016-3 added; UC-004 note; TC-035 added.
- **Impact:** CMP-001 (view model keeps the time the app left the screen); no data or alarm change.

### CHG-004 — Post-H6 documentation cleanup and synchronization
- **Status:** Proposed (2026-11-19; awaiting Owner commit `approve: CHG-004`). No requirement or ADR changes meaning.
- **Reason:** resolve stale banners, missing traceability references, outdated test/risk/roadmap counts, and post-`f9fcf3f` status drift across documentation.
- **Changes:** `DOC-VIS` 0.2, `DOC-FR` 0.2 (`Refs` on `FR-001`..`FR-029`, `OI-041` note), `DOC-NFR` 0.2 (`Refs` on `NFR-001`..`NFR-010`), `DOC-ARC` 0.2 (`ADR-0005` refs, `CMP-001`/`CMP-003` code map), `DOC-CMP` 0.2 (`CMP-001` `CHG-003` alignment, `CMP-006` traces, `OI-046` window), `DOC-INT` 0.2 (`INT-004` verified `wa.me`, `SEC-007`), `DOC-TST` 0.2 (header and Results sync), `DOC-RSK` 0.2 (`RSK-011`/`RSK-013`/`RSK-014` closed, `RSK-017` wording); plus Draft docs `DOC-IDX` 0.2, `DOC-TRC` 0.2 (39-row traceability matrix), `DOC-PEN` 0.5 (`OI-039`, `OI-048`), `DOC-TFA-001` 0.2 (measured verdict), and `roadmap.html`.
- **Impact:** none on code; edited formerly-Approved documents returned to `In review` per `DOC-GOV` until re-approved by the Owner.
