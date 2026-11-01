---
id: DOC-CMP
title: Component Design
status: Draft
version: 0.1
date: 2026-11-01
approved_by:
refs: [DOC-FR, DOC-NFR, DOC-DAT, ADR-0001, ADR-0002, ADR-0003, ADR-0004]
---

# Component Design

> **Status:** Draft proposed by the agent. Every component is `Proposed`. Component names and splits are a proposal; the UI toolkit and the dependency injection approach are left to implementation and are not decided here.

One Android app (ADR-0001), no backend. Data flows from the local store up to the screens; nothing reads from anywhere else (NFR-002).

## Components

### CMP-001 — Screens
- **Responsibility:** show and edit properties, tenants and rentals; calendar; weekly summary; reminder settings; backup export and import; permission warning.
- **Uses:** CMP-002, CMP-003, CMP-004, CMP-007, CMP-008, CMP-009.
- **Traces:** FR-001 to FR-017 · FR-018 to FR-020 · FR-024 · FR-025 to FR-029 · NFR-007 · NFR-009
- **Rules:** all text in Spanish; amounts shown with the colon symbol and no cents; the weekly summary always opens on the current week (FR-016).

### CMP-002 — Data store
- **Responsibility:** the Room database with ENT-001 to ENT-004; operations that must be atomic: delete a tenant with its rentals, check and create a rental, replace all data on import.
- **Contract:** every change that affects reminders ends by calling CMP-004 to rebuild them (FR-021).
- **Traces:** ADR-0002 · FR-006 · FR-008 · FR-012 · NFR-003 · NFR-005

### CMP-003 — Collection calculator
- **Responsibility:** from a rental list, a date range and today, return the collections (rental, date, amount). Pure logic with no Android dependency.
- **Rules:** last day of the month when the payment day does not exist; none after the end date; none for past dates in the calendar; weeks run Monday to Sunday.
- **Traces:** FR-013 · FR-014 · FR-015 · FR-016 · FR-017 · FR-011 · AC-FR-013-1

### CMP-004 — Reminder scheduler
- **Responsibility:** compute the nearest reminders from CMP-003 and settings, up to the window limit, plus the renewal reminder; cancel the previous alarms; create the new ones with exact allow-while-idle alarms; fall back to inexact alarms when the exact permission is missing.
- **Triggers:** app open; rental created, edited, ended or deleted; settings changed; boot; exact-alarm permission change.
- **Traces:** ADR-0003 · FR-018 to FR-022 · NFR-004 · NFR-001

### CMP-005 — Reminder receiver and notifier
- **Responsibility:** on an alarm, read the data needed from CMP-002 and show one notification with property, tenant, amount and due date; tapping it opens the app on the current week. The renewal reminder asks the landlord to open the app.
- **Traces:** FR-020 · FR-022

### CMP-006 — System event receiver
- **Responsibility:** on boot completed and on exact-alarm permission change, ask CMP-004 to rebuild the reminders.
- **Known limits (verified on the Owner phone, ADR-0003):** after a reboot it runs only after the first unlock; after a force stop nothing runs until the app is opened.

### CMP-007 — Backup service
- **Responsibility:** export to the JSON file and import from it as described in ADR-0004; validation before any write; replacement in one transaction through CMP-002; then rebuild reminders.
- **Traces:** FR-027 · FR-028 · FR-029 · NFR-005

### CMP-008 — External actions
- **Responsibility:** open WhatsApp and the phone dialer on the tenant's number; hide the actions when there is no number.
- **Traces:** FR-025 · FR-026 · INT-004 · INT-005

### CMP-009 — Permissions helper
- **Responsibility:** explain and request the notification permission and the exact-alarm permission the first time a rental is created; report their state to CMP-001 for the warning.
- **Traces:** FR-023 · FR-024

## Dependencies
CMP-001 → CMP-002, CMP-003, CMP-004, CMP-007, CMP-008, CMP-009. CMP-004 → CMP-002, CMP-003. CMP-005 → CMP-002. CMP-006 → CMP-004. CMP-007 → CMP-002, CMP-004.

## Open points
The window limit of CMP-004 (ADR-0003 step 5) is a configuration value to be chosen below the 500-alarm system limit.
