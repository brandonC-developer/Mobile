---
id: DOC-NFR
title: Non-Functional Requirements
status: Approved
version: 0.1
date: 2026-11-01
approved_by: Brandon
refs: [DOC-VIS, DOC-PEN]
---

# Non-Functional Requirements

> **Status:** Draft proposed by the agent. Every requirement is `Proposed`. Quality categories are free text until the vocabulary is verified (OI-005). Thresholds come from Owner decisions; where a number or method is the agent's proposal it is marked.

### NFR-001 — Works without a connection
- **Status:** Approved
- **Origin:** OBJ-001, OBJ-002, OBJ-003 · OI-021
- **Quality category:** Offline operation
- **Measurable requirement:** With airplane mode on, every function except opening external apps (WhatsApp, phone) works, including reminders.
- **Verification method:** Test on the device in airplane mode.
- **Refs:** none yet

### NFR-002 — Data stored on the device
- **Status:** Approved
- **Origin:** OBJ-005 · OI-021
- **Quality category:** Data ownership
- **Measurable requirement:** All user data is stored on the device and is the only source of truth. Data leaves the device only through a backup export started by the landlord or through the external apps the landlord opens.
- **Verification method:** Review of data flows; network inspection during a normal session shows no outgoing data.
- **Refs:** none yet

### NFR-003 — Data persistence
- **Status:** Approved
- **Origin:** OBJ-001 · OI-021
- **Quality category:** Reliability
- **Measurable requirement:** After closing the app and after restarting the phone, all saved data is intact.
- **Verification method:** Test on the device.
- **Refs:** none yet

### NFR-004 — Reminder punctuality
- **Status:** Approved
- **Origin:** OBJ-003 · OI-017
- **Quality category:** Timeliness
- **Measurable requirement:** Each reminder arrives at the time chosen by the landlord with a delay of at most 5 minutes, with the notification and exact-alarm permissions granted.
- **Verification method:** Test on the Owner device with scheduled reminders, including the phone idle and battery optimization on. Open: behavior after a forced close or a reboot (OI-039).
- **Refs:** none yet

### NFR-005 — Backup integrity
- **Status:** Approved
- **Origin:** OBJ-005 · OI-037 · agent proposal
- **Quality category:** Reliability
- **Measurable requirement:** An import either replaces all data or changes nothing. A failed or interrupted import leaves the previous data intact.
- **Verification method:** Test with valid, invalid and truncated files.
- **Refs:** none yet

### NFR-006 — Platform and installation
- **Status:** Approved
- **Origin:** OBJ-001 · OI-018 · OI-038
- **Quality category:** Compatibility, portability
- **Measurable requirement:** The app runs on Android 13 or later and installs from an APK file without a store account. iOS is not supported.
- **Verification method:** Install and run on the Owner device (Android 13).
- **Refs:** none yet

### NFR-007 — Interface language
- **Status:** Approved
- **Origin:** OBJ-001 · OI-020
- **Quality category:** Localization
- **Measurable requirement:** All text shown to the user is in Spanish.
- **Verification method:** Review of all screens, messages and notifications.
- **Refs:** none yet

### NFR-008 — Currency and amounts
- **Status:** Approved
- **Origin:** OBJ-001 · OI-020
- **Quality category:** Data format
- **Measurable requirement:** Amounts are whole Costa Rican colones, stored as integers and shown with the colon symbol and without cents. No other currency exists.
- **Verification method:** Review of entry, storage and display of amounts.
- **Refs:** none yet

### NFR-009 — Font size
- **Status:** Approved
- **Origin:** OBJ-001 · OI-020
- **Quality category:** Accessibility
- **Measurable requirement:** Text follows the font-size setting of the phone; with the largest setting the content remains readable and no control is cut off. No other accessibility requirement for now.
- **Verification method:** Test on the device with the default and the largest font sizes.
- **Refs:** none yet

### NFR-010 — Access protection
- **Status:** Approved
- **Origin:** OBJ-005 · OI-019
- **Quality category:** Security, privacy
- **Measurable requirement:** The app adds no lock of its own; protection relies on the phone screen lock. The backup file is not encrypted (OI-040).
- **Verification method:** Review.
- **Refs:** none yet

## Not yet covered
- Performance targets (screen load times), storage size and similar: not requested by the Owner; to ask if relevant.
- Notification limit per app on Android: unverified (OI-039).
