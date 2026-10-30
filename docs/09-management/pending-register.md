---
id: DOC-PEN
title: Pending Items Register
status: Draft
version: 0.2
date: 2026-10-30
approved_by:
refs: []
---

# Pending Items Register

Types: Assumption · Unknown · Question · Pending decision · Verification.

| ID | Type | Description | Affects | Owner | Status | Resolution |
|---|---|---|---|---|---|---|
| OI-001 | Pending decision | Git remote (GitHub or other, or local only) | DOC-GOV | Owner | Open | |
| OI-002 | Pending decision | Branching and pull-request policy (single approver) | DOC-GOV | Owner | Open | Proposal: branches and direct commits; PRs when more people join |
| OI-003 | Pending decision | Document review cadence | DOC-GOV | Owner | Open | |
| OI-004 | Verification | Confirm on iso.org the status of the ISO/IEC/IEEE 29148 revision; decide on automated link/trace checking | DOC-STD, DOC-TRC | Owner | Open | |
| OI-005 | Verification | Verify the characteristics of ISO/IEC 25010:2023 in the official preview before using them as a closed list | DOC-NFR | Owner | Open | |
| OI-006 | Verification | Identify the current arc42 template version and attribution needs | DOC-ARC | Owner | Open | |
| OI-007 | Pending decision | Diagram source format (C4 is notation-independent) | DOC-ARC | Owner | Open | Decide when diagrams exist |
| OI-008 | Pending decision | Requirement priority scheme | DOC-FR | Owner | Open | |
| OI-009 | Unknown | Project context, stack and constraints not yet provided | All | Owner | Resolved | Initial context received 2026-10-30, see CTX-001 below. Received is not approved. |

## Context inputs
Raw inputs, kept unchanged and unapproved. Nothing here is a requirement or decision until processed in a later phase and approved by the Owner.

| ID | Source file | Received | Status |
|---|---|---|---|
| CTX-001 | `docs/01-context/contexto_proyecto_app_inquilino.md` (Spanish, kept as provided) | 2026-10-30 | Received, unprocessed |

## Open items declared by the context source (CTX-001 §10)
Translated from the Spanish source. The source states they "must not be resolved through assumptions".

| ID | Type | Description | Affects | Owner | Status | Resolution |
|---|---|---|---|---|---|---|
| OI-010 | Question | Behavior when a month does not contain the registered payment day | DOC-FR | Owner | Open | |
| OI-011 | Question | Rule that determines when an obligation becomes overdue | DOC-FR | Owner | Open | |
| OI-012 | Question | How and when an obligation is marked as paid | DOC-FR | Owner | Open | |
| OI-013 | Question | Whether amount or payment date may be modified, and how that affects future obligations | DOC-FR | Owner | Open | |
| OI-014 | Question | Generation horizon for recurring obligations | DOC-FR | Owner | Open | |
| OI-015 | Question | Exact rules for grouping dates into weeks and the limits of each week | DOC-FR | Owner | Open | |
| OI-016 | Question | Format and content of the backup file | DOC-FR, DOC-DAT | Owner | Open | |
| OI-017 | Question | Notification permission requirements and behavior on Android and/or iOS | DOC-NFR | Owner | Open | |
| OI-018 | Question | Target mobile platform and distribution requirements | DOC-VIS | Owner | Open | |
| OI-019 | Question | Privacy and data-protection needs | DOC-SEC | Owner | Open | |
| OI-020 | Question | Accessibility, language and currency-format rules | DOC-NFR | Owner | Open | |

## Ambiguities detected while logging (agent-detected, to confirm with the Owner)
Each cites the source section. These are questions, not findings of error.

| ID | Type | Description | Source | Owner | Status | Resolution |
|---|---|---|---|---|---|---|
| OI-021 | Question | §7 says the initial scope "contemplates SQLite as local storage", while §8 says technologies, SQLite included, are preliminary references and not approved decisions. Is SQLite a constraint or a candidate? | CTX-001 §7, §8 | Owner | Resolved | Owner decision, 2026-10-30: the product is offline-first. Constraint to carry into the NFRs (wording proposed, pending Owner approval of the requirement): core functions (registration, calendar, summary, notifications) work without connection; user data is stored locally on the device as the only source of truth; Internet is used only to open external apps. SQLite is a candidate, to be decided in an ADR with alternatives. Inputs for that ADR: sqlite.org "Appropriate Uses" (device-local storage fits; limits are one writer at a time and simultaneous network access); Expo SQLite docs (persists across restarts; no encryption in Expo Go; no documented user-data backup strategy). |
| OI-022 | Question | §5.1 mentions maintenance fees and services included in the rent as examples of recurring obligations, but §4.3 describes only rent obligations. Is more than one type of obligation in scope? | CTX-001 §4.3, §5.1 | Owner | Open | |
| OI-023 | Question | §6.1 requires saving landlord contact data (WhatsApp, phone, email), but §4.2 lists only the landlord's name (optional). Where do the landlord's contact data belong? | CTX-001 §4.2, §6.1 | Owner | Open | |
| OI-024 | Question | The obligation statuses named are pending, overdue (§4.4) and today, upcoming, overdue, paid (§5.3). The exact set of statuses is not stated. | CTX-001 §4.3, §4.4, §5.3 | Owner | Open | |

## Technologies mentioned (not approved)
React Native, Expo, TypeScript, SQLite, local notifications. The source says they are preliminary references and not approved decisions (CTX-001 §8). They enter the architecture only through ADRs approved by the Owner.
