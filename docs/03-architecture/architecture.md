---
id: DOC-ARC
title: Architecture
status: Approved
version: 0.1
date: 2026-11-01
approved_by: Brandon
refs: [DOC-VIS, DOC-FR, DOC-NFR, DOC-DAT, DOC-CMP, DOC-INT, DOC-SEC, DOC-RSK, ADR-0001, ADR-0002, ADR-0003, ADR-0004]
---

# Architecture

> **Status:** Approved by the Owner in commit `a1bc3ba` (2026-11-02); drafted by the agent. It summarizes and points to other documents; the reasons for each decision live in the ADRs, which are approved. Use cases are in [use-cases](../02-requirements/use-cases/use-cases.md).

## 1. Introduction and goals
A personal Android app for one landlord to track monthly rent collections and be reminded locally. Objectives OBJ-001 to OBJ-005 are in [vision-scope](../01-context/vision-scope.md). Terms: [glossary](../01-context/glossary.md).

## 2. Constraints
Offline-first with no backend (NFR-001, NFR-002); Android 13 or later, APK install, no store (NFR-006); Spanish only, whole colones (NFR-007, NFR-008); personal use, one user (OI-019). Details in [functional](../02-requirements/functional.md) and [non-functional](../02-requirements/non-functional.md) requirements.

## 3. Context and scope (VW-001)
- **Stakeholders:** the landlord (only user and only owner of the data).
- **Concern:** nothing leaves the device except what the landlord starts.
- **View:** the app talks to the Android system (alarms, notifications, share menu, file picker, system events) and, through the system, to WhatsApp and the phone dialer. It has no server, account or Internet integration. See [interfaces](../04-design/interfaces-integrations.md) INT-001 to INT-008.

```
Landlord ⇄ [ App ] ⇄ Android system ⇢ WhatsApp / dialer / share menu
              │
        local database (private app storage)
```

## 4. Solution strategy
| Decision | ADR |
|----------|-----|
| Native Android app in Kotlin | ADR-0001 |
| Room on SQLite as the only store | ADR-0002 |
| Exact alarms for reminders, rebuilt on every relevant event | ADR-0003 |
| Versioned JSON backup, validated before import | ADR-0004 |

Collections and reminders are computed from rentals and settings, not stored (FR-013, FR-021).

## 5. Building block view (VW-002)
Nine components, CMP-001 to CMP-009, defined in [components](../04-design/components.md): screens, data store, collection calculator, reminder scheduler, reminder receiver and notifier, system event receiver, backup service, external actions, permissions helper. Data entities ENT-001 to ENT-004 in [data-model](../04-design/data-model.md).

## 6. Runtime view (VW-003)
- **Create a rental:** screens validate, the data store checks the one-active-rental rule and saves in one transaction, the scheduler rebuilds the reminders (FR-007, FR-008, FR-021).
- **A reminder fires:** the system calls the receiver at the chosen time, which reads the data and shows the notification (FR-020, NFR-004).
- **Reboot:** after the first unlock the system event receiver asks the scheduler to rebuild (ADR-0003).
- **Import a backup:** parse, validate, confirm, replace in one transaction, rebuild reminders (ADR-0004).
- **Open the app:** reminders are rebuilt and the weekly summary shows the current week (FR-016, FR-021).

## 7. Deployment view
One APK installed directly on the Owner phone (moto g72, Android 13). No servers, no environments. Updates are new APKs installed over the old one; the database version is migrated by Room (ADR-0002).

## 8. Crosscutting concepts
- **Persistence and integrity:** transactions for cascades and import (ADR-0002, NFR-005).
- **Time:** collection dates computed from the current date passed in, so they can be tested (test plan).
- **Permissions:** notification and exact-alarm permissions asked at the first rental, with a warning if denied (FR-023, FR-024).
- **Privacy and security:** [security-privacy](../05-security-privacy/security-privacy.md).
- **Backup of the platform:** Android automatic backup and device transfer disabled (OI-042).

## 9. Decisions
ADR-0001 to ADR-0004 in [adr](adr/). Not repeated here.

## 10. Quality requirements
NFR-001 to NFR-010; verification cases in the [test plan](../06-quality/test-plan.md).

## 11. Risks and technical debt
[risk-register](../09-management/risk-register.md).

## 12. Glossary
[glossary](../01-context/glossary.md).
