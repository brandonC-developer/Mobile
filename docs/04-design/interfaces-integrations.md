---
id: DOC-INT
title: Interfaces and Integrations
status: Approved
version: 0.1
date: 2026-11-01
approved_by: Brandon
refs: [DOC-FR, DOC-NFR, DOC-CMP, ADR-0003, ADR-0004]
---

# Interfaces and Integrations

> **Status:** Draft proposed by the agent. The app has no backend and no Internet integration; its interfaces are the user and the Android system.

| ID | Interface | Direction | Used by | Contract | Traces |
|----|-----------|-----------|---------|----------|--------|
| INT-001 | Landlord (screens) | in/out | CMP-001 | Spanish text, colones without cents, large-font friendly | NFR-007 · NFR-008 · NFR-009 |
| INT-002 | Alarm service | out | CMP-004 | Exact allow-while-idle alarm per reminder; inexact fallback; at most 500 alarms per app (verified on device) | ADR-0003 · FR-021 |
| INT-003 | Notifications | out | CMP-005 | One notification channel; permission requested at first rental | FR-020 · FR-023 |
| INT-004 | WhatsApp (external app) | out | CMP-008 | Open a chat with the tenant's number. Number gets +506 when no country code is present (OI-043); the link format is not verified yet; no data leaves except the number the landlord opens | FR-025 |
| INT-005 | Phone dialer (external app) | out | CMP-008 | Open the dialer with the number; the call is placed by the landlord | FR-026 |
| INT-006 | Share menu | out | CMP-007 | Offer the backup JSON file; the landlord chooses the destination | FR-027 · ADR-0004 |
| INT-007 | File picker | in | CMP-007 | The landlord picks a backup file to import | FR-028 · ADR-0004 |
| INT-008 | System events | in | CMP-006 | Boot completed; exact-alarm permission changed | ADR-0003 |

## Rules
- The app declares no Internet permission (proposal, see DOC-SEC); external apps are opened by the system and make their own connections.
- Android automatic backup and device-to-device transfer are disabled (ADR-0002, OI-042).
- The backup file schema is defined in ADR-0004; any change raises its version.
