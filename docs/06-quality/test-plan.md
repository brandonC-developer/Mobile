---
id: DOC-TST
title: Test Plan
status: Approved
version: 0.1
date: 2026-11-01
approved_by: Brandon
refs: [DOC-FR, DOC-NFR, DOC-DAT, DOC-CMP, ADR-0002, ADR-0003, ADR-0004]
---

# Test Plan

> **Status:** Approved by the Owner in commit `a1bc3ba` (2026-11-02); drafted by the agent. The test tools are a recommendation to confirm at implementation. Every case is `Planned`; none has been run.

## Strategy
| Level | Scope | Environment |
|-------|-------|-------------|
| Unit | CMP-003 collection rules and week logic; backup validation (CMP-007) | JVM on the computer |
| Data | CMP-002 constraints and transactions with an in-memory Room database | JVM or emulator |
| Device | Reminders, permissions, reboot, installation, font size, airplane mode | Owner phone (moto g72, Android 13) |
| Manual review | Spanish text, no statuses, no delete for properties, manifest contents | Reading the app and the build |

Entry: the requirements and ADRs of the item are approved. Exit: every Must case passes on the Owner phone. Dates in unit tests are passed in, never read from the clock.

## Test cases
| ID | Verifies | Type | Steps | Expected result | Status |
|----|----------|------|-------|-----------------|--------|
| TC-001 | AC-FR-001-1, AC-FR-001-2 | Unit | Save a property with and without a name | Saves without description; rejects empty name | Planned |
| TC-002 | AC-FR-003-1, AC-FR-003-2 | Manual | Look for any delete action on properties; delete a tenant and a rental | None exists; properties remain | Planned |
| TC-003 | AC-FR-004-2, AC-FR-005-1 | Data | Create a tenant without a name; edit one with an active rental | Rejected; edit saved | Planned |
| TC-004 | AC-FR-006-1 to AC-FR-006-4 | Data | Delete a tenant with two rentals, once cancelling and once confirming | Count shown; cancel keeps all; confirm removes tenant and rentals, properties free | Planned |
| TC-005 | AC-FR-007-2, AC-FR-007-3 | Unit | Save amounts 0, -5, 1500.5 and a past end date | All rejected | Planned |
| TC-006 | AC-FR-008-1 to AC-FR-008-3 | Data | Create a second active rental on one property, then on one tenant; end the first and retry | Rejected with message; accepted after ending | Planned |
| TC-007 | AC-FR-009-1 | Manual | Open the rental edit screen | No tenant or property selector | Planned |
| TC-008 | AC-FR-010-1, AC-FR-010-2 | Device | Change the payment day; read calendar and scheduled alarms | New dates shown; no alarm on old dates | Planned |
| TC-009 | AC-FR-011-1 to AC-FR-011-3 | Unit | Set an end date; list collections after it | None after the end date; property free | Planned |
| TC-010 | AC-FR-012-1 to AC-FR-012-3 | Data | Delete a rental after cancel and confirm | Cancel keeps; confirm removes rental and alarms; tenant and property remain | Planned |
| TC-011 | AC-FR-013-1, AC-FR-013-2 | Unit | Payment day 31 over a year including a leap February; payment day 15 | 28/29 in February, 30 in April; 15th always | Planned |
| TC-012 | AC-FR-014-1 to AC-FR-014-3 | Device | Open the calendar; select dates; look at past dates | Amount on selection; nothing before today; no statuses | Planned |
| TC-013 | AC-FR-015-1 to AC-FR-015-3, AC-FR-017-1 | Unit | Collections on a Sunday, a Monday, the 31st across a month change; empty week | Correct week; empty state; total equals the sum | Planned |
| TC-014 | AC-FR-016-1, AC-FR-016-2 | Device | Move to next week, leave the app, reopen; try to go two weeks away | Opens on the current week; limited to one week each way | Planned |
| TC-015 | AC-FR-018-1, AC-FR-018-2 | Device | Select some offsets, then turn one off | Only selected offsets produce alarms; removed ones vanish | Planned |
| TC-016 | AC-FR-019-1, NFR-004 | Device | Change the reminder time; wait for a reminder with the screen off | Rescheduled; arrives within 5 minutes | Planned |
| TC-017 | AC-FR-020-1 | Device | Receive a reminder | Shows property, tenant, amount and due date; tap opens the current week | Planned |
| TC-018 | AC-FR-021-1, AC-FR-021-2, AC-FR-022-1 | Device | Open the app; edit, end, delete a rental; use more collections than the window | Nearest reminders scheduled; none stale; renewal reminder exists | Planned |
| TC-019 | NFR-004, ADR-0003 | Device | Reboot and unlock without opening the app; force stop and reopen | Reminders rebuilt after unlock; after force stop, rebuilt on open | Planned |
| TC-020 | AC-FR-023-1, AC-FR-023-2, AC-FR-024-1, AC-FR-024-2 | Device | First rental on a fresh install; deny, then grant | Explanation then requests; not repeated; warning only while denied | Planned |
| TC-021 | AC-FR-025-1/2, AC-FR-026-1/2 | Device | Use the actions with and without a phone | Opens WhatsApp or dialer; hidden or disabled without a phone | Planned |
| TC-022 | AC-FR-027-1, AC-FR-027-2 | Device | Export a backup | Warning first; file has a schema version and all data | Planned |
| TC-023 | AC-FR-028-1 to AC-FR-028-3, NFR-005 | Device | Import with and without confirming; export, clean install, import | Cancel changes nothing; data equals the file; round trip equal | Planned |
| TC-024 | AC-FR-029-1, AC-FR-029-2, NFR-005 | Unit | Import newer version, invalid JSON, missing field, truncated file | All rejected; data unchanged | Planned |
| TC-025 | NFR-001 | Device | Use every function in airplane mode | All work | Planned |
| TC-026 | NFR-002 | Manual | Inspect the manifest and network traffic during a session | No Internet permission; no outgoing data; backup flags off | Planned |
| TC-027 | NFR-003 | Device | Save data, close the app, restart the phone | Data intact | Planned |
| TC-028 | NFR-006 | Device | Install the APK on the Owner phone | Installs and runs on Android 13 | Planned |
| TC-029 | NFR-007, NFR-008 | Manual | Review all texts and amounts | Spanish; colon symbol; no cents | Planned |
| TC-030 | NFR-009 | Device | Largest phone font size | Readable, no control cut off | Planned |
| TC-031 | AC-FR-013-3 (CHG-001) | Unit | Create a rental on the 5th with payment day 3; list collections for the previous week and the current month; create one on its payment day | No collection before the creation date; first collection on the 3rd of next month; collection of the creation day included | Planned |

## Results
Run on 2026-11-02 (emulator Medium_Phone_API_37.0, JVM): H4: TC-008, TC-015, TC-018 (alarms rebuilt from the database, no duplicates, none left after ending or deleting a rental, offsets removed when turned off, renewal reminder), TC-017 (notification content), TC-020 (warning shown only when a permission is missing, never blocking; unit and screen level) and TC-026 (manifest: only the three reminder permissions, private alarm receiver) pass as automated tests. TC-016 on the emulator (exact alarm allowed, screen on): reminder planned 11:44:00 arrived 11:44:00.8, delay under 1 second. NOT yet run on the Owner phone: TC-016 with the screen off, TC-019 (reboot, force stop), the real permission dialogs of TC-020. H3: TC-012, TC-013 and TC-014 pass as automated screen tests (`app/src/androidTest/.../WeekCalendarUiTest.kt`, fixed date 2026-11-04) plus calendar and week-navigation unit tests; the week and calendar screens were also looked at on the emulator. H1 and H2 data and logic cases pass as automated tests: TC-005, TC-006, TC-009, TC-011, TC-013, TC-031 (unit, `app/src/test`) and TC-004, TC-010, plus the edit and end rules of FR-005 to FR-012 (device, `app/src/androidTest`). The UI flow of UC-001, UC-002, UC-003 (create tenant, properties and a rental; occupied property shown unavailable; next collection date shown) was exercised by hand on the emulator. Not yet run on the Owner phone. Other cases are still planned.
