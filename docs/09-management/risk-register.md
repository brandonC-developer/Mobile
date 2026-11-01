---
id: DOC-RSK
title: Risk and Technical Debt Register
status: Draft
version: 0.1
date: 2026-11-01
approved_by:
refs: [DOC-ARC, DOC-SEC, ADR-0001, ADR-0002, ADR-0003, ADR-0004]
---

# Risk and Technical Debt Register

> **Status:** Draft proposed by the agent. Probability and impact are the agent's estimate (low, medium, high), not measured. Owner of every item is the Owner until assigned otherwise.

| ID | Type | Description | Probability | Impact | Mitigation | Owner | Status | Refs |
|----|------|-------------|-------------|--------|------------|-------|--------|------|
| RSK-001 | Risk | Force stop cancels all reminders until the app is opened (verified on the Owner phone) | Medium | High | State it as a known limit; renewal reminder cannot help; open the app regularly | Owner | Open | ADR-0003 · FR-021 · FR-022 |
| RSK-002 | Risk | After a reboot, reminders due before the first unlock are not delivered (verified) | Medium | Medium | Unlock the phone after a reboot; state it in the user notes | Owner | Open | ADR-0003 |
| RSK-003 | Risk | Manufacturer battery management delays reminders beyond 5 minutes; only one device tested, with forced Doze and USB attached | Low | High | Repeat the test on battery without USB and with battery saver before relying on it; `setAlarmClock()` is the documented fallback | Owner | Open | ADR-0003 · NFR-004 |
| RSK-004 | Risk | Exact-alarm permission revoked or denied; fallback fired 30 to 90 s late in one run, with no guarantee | Medium | Medium | Warning of FR-024; rebuild when granted again | Owner | Open | FR-024 · ADR-0003 |
| RSK-005 | Risk | The window of reminders exceeds the 500-alarm system limit | Low | Medium | Choose a window value well below 500; test at the chosen value | Owner | Open | ADR-0003 · FR-021 |
| RSK-006 | Risk | Android automatic backup copies the database to Google Drive on a sideloaded APK (not documented) | Low | High | Backup and transfer disabled (OI-042); check on the device once the app exists | Owner | Mitigated | ADR-0002 · NFR-002 |
| RSK-007 | Risk | Data lost with the phone: the only copy is on the device, backup is manual | Medium | High | Export a backup regularly and keep it outside the phone; no automatic reminder to back up is specified (a possible new requirement, Owner decides) | Owner | Open | FR-027 · NFR-002 |
| RSK-008 | Risk | The backup file holds personal data in plain text and is shared by mistake | Medium | Medium | Warning before export (Owner decision, OI-040) | Owner | Accepted | SEC-002 · FR-027 |
| RSK-009 | Risk | A phone number format breaks the WhatsApp action; the link format is unverified | Medium | Low | Verify the format before implementing; hide the action without a phone | Owner | Open | INT-004 · FR-025 |
| RSK-010 | Risk | Gradle and toolchain versions: the build needs JDK 17 or 21; the Android Studio bundled JDK 25 failed with the version used in the spike | Medium | Low | Fix the JDK in project settings and document it in the development guide | Owner | Open | ADR-0001 |
| RSK-011 | Risk | Room 3.0 is a new package; the Room 2.x guide is marked legacy | Low | Low | Choose and record the version at implementation against the release notes | Owner | Open | ADR-0002 |
| RSK-012 | Risk | Approval evidence for requirements is not valid (commit `f80eba2`), so work on them rests on unapproved documents | High | Medium | Owner redoes the approval commit; no development before approvals | Owner | Open | AGENTS.md · OI-001 to OI-007 |
| RSK-013 | Debt | Entities, defaults and the backup field list are logical only; physical details await implementation | Medium | Low | Revisit DOC-DAT and ADR-0004 when the Room schema is written | Owner | Open | DOC-DAT · ADR-0004 |
| RSK-014 | Debt | No use cases (UC) are written; scenarios are cited through requirements | Low | Low | Write UCs if the Owner asks for them | Owner | Open | DOC-ARC |
