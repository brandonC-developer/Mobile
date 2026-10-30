---
id: DOC-PEN
title: Pending Items Register
status: Draft
version: 0.3
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
| CTX-001 | `docs/01-context/contexto_proyecto_app_inquilino.md` (Spanish, kept as provided) | 2026-10-30 | Received, unprocessed. Contradicted by CTX-002 on the primary user (§3); see OI-026 |
| CTX-002 | Owner statements in chat, 2026-10-30: (a) "the app is for the landlord (arrendador) to manage collections from tenants (arrendatarios)"; (b) proposed data shape: Property = id, name, description (optional); Tenant = id, full name, phone (optional); Rental = tenant id, property id, amount, payment due date; (c) "for now, only rent"; (d) "the app is offline-first" | 2026-10-30 | Received, unprocessed. Where it contradicts CTX-001, the Owner's later statement prevails (OI-026) |

## Open items declared by the context source (CTX-001 §10)
Translated from the Spanish source. The source states they "must not be resolved through assumptions".

| ID | Type | Description | Affects | Owner | Status | Resolution |
|---|---|---|---|---|---|---|
| OI-010 | Question | Behavior when a month does not contain the registered payment day | DOC-FR | Owner | Resolved | Owner confirmed 2026-10-30: in a month without the registered day, the reminder falls on the last day of that month (the day used is the lesser of the registered day and the last day of the month). Design recommendation, not a norm: RFC 5545 (iCalendar) instead ignores instances with invalid dates, which would skip the month; rejected because a skipped collection reminder defeats the purpose. Alternative not chosen: move to day 1 of the next month. |
| OI-011 | Question | Rule that determines when an obligation becomes overdue | DOC-FR | Owner | Superseded | Owner decision 2026-10-30 (OI-024): no status concept for now, so nothing becomes overdue. Reopen if statuses are added through a CHG. |
| OI-012 | Question | How and when an obligation is marked as paid | DOC-FR | Owner | Superseded | Owner decision 2026-10-30 (OI-024): no status concept for now, so there is no "mark as paid". Reopen if statuses are added through a CHG. |
| OI-013 | Question | Whether amount or payment date may be modified, and how that affects future obligations | DOC-FR | Owner | Resolved | Resolved together with OI-031 on 2026-10-30; see OI-031. |
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
| OI-022 | Question | §5.1 mentions maintenance fees and services included in the rent as examples of recurring obligations, but §4.3 describes only rent obligations. Is more than one type of obligation in scope? | CTX-001 §4.3, §5.1 | Owner | Resolved | Owner decision, 2026-10-30: for now, only rent obligations are in scope. Maintenance fees and services are not in the current scope and need a change (CHG) to be added. Consequence logged as OI-025. |
| OI-023 | Question | §6.1 requires saving landlord contact data (WhatsApp, phone, email), but §4.2 lists only the landlord's name (optional). Where do the landlord's contact data belong? | CTX-001 §4.2, §6.1 | Owner | Resolved | Superseded by OI-026: the user is the landlord, so the contact data the app needs are the tenant's. Owner's proposed shape: the tenant has a full name and an optional phone (CTX-002). Fields omitted from that shape are tracked in OI-027. |
| OI-024 | Question | The obligation statuses named are pending, overdue (§4.4) and today, upcoming, overdue, paid (§5.3). The exact set of statuses is not stated. | CTX-001 §4.3, §4.4, §5.3 | Owner | Resolved | Owner decision, 2026-10-30: "there are no statuses; for now the app only notifies". Out of scope for now: obligation status, paid/pending/overdue differentiation in the calendar (§4.4, §5.3), marking as paid. Adding them needs a CHG. Open consequences: OI-025, OI-030. |
| OI-025 | Question | With rent as the only obligation (OI-022), a month has one obligation per property, so the weekly summary and "upcoming obligations of the month" (§5.5) carry little information. Is the weekly summary still wanted as described, or should it be reduced or deferred? | CTX-001 §5.5 | Owner | Open | Partly changed: the landlord may have several rentals (OI-026), but §5.5 also shows weekly states "pending, overdue or paid", which do not exist now (OI-024). |
| OI-026 | Question | Primary user is the landlord (arrendador), who manages collections from tenants (arrendatarios). | CTX-002 vs CTX-001 §3 | Owner | Resolved | Owner confirmed 2026-10-30; replaces CTX-001 §3. Terms: arrendador = landlord (the user); arrendatario = tenant (the payer). Points in CTX-001 that assume a tenant user need review, tracked in OI-028. |
| OI-027 | Question | The Owner's proposed data shape (CTX-002) omits fields that CTX-001 had: property address (§4.2), email (§4.1), obligation status (§4.3). Are they dropped, or only left out of the message? | CTX-001 §4.1-4.3, CTX-002 | Owner | Resolved | Owner confirmed 2026-10-30: property address, tenant email and landlord name are not stored. Consequence: the email contact option of CTX-001 §6.1 has no data to act on (noted in OI-028). Obligation status was not decided here: it is treated under OI-024. Multiplicity moved to OI-029. |
| OI-029 | Assumption | A rental links one tenant with one property, and the landlord may have several rentals. Not stated by the Owner; to confirm. | CTX-002 | Owner | Open | |
| OI-028 | Question | Review the parts of CTX-001 written for a tenant user: purpose and objective (§1, §2), reminders to pay (§4.5, §5.2), landlord contact (§6.1), weekly summary meaning (§5.5, see OI-025), out-of-scope item "landlord portal" (§9), the file name. Which hold with a landlord user, and how do notifications work: reminders to collect, to the landlord only? | CTX-001 | Owner | Resolved | Owner confirmed 2026-10-30: notifications are collection reminders received only by the landlord, with the options of CTX-001 §4.5 (3, 2, 1 days before and the same day; selectable time). Notifying tenants is out of scope for now: it would need a server, against the no-backend constraint (§7). Contact with the tenant is limited to WhatsApp and phone call, opened in external apps; email is dropped (OI-027). The "landlord portal" out-of-scope item (§9) is replaced by: no tenant-facing app or portal. The weekly summary is revisited in OI-025. |
| OI-030 | Question | Without statuses (OI-024), what does the calendar show for dates already passed, and until when do the monthly reminders repeat for a rental (no end date, or an end of rental)? | CTX-002 | Owner | Resolved | Owner confirmed 2026-10-30: each rental may have an optional end date. Monthly reminders repeat until that date; with no end date they continue until the landlord ends the rental. The calendar shows only today and future dates. Not decided here: how the landlord ends, edits or deletes a rental without an end date (see OI-013, OI-031). Related: OI-014 (generation horizon). |
| OI-031 | Question | How does the landlord end, edit or delete a rental (change of amount or day, removal)? Overlaps OI-013 | CTX-002 | Owner | Resolved | Owner confirmed 2026-10-30. Edit amount, payment day or end date: applies from the next reminder onward; already-scheduled future reminders are recalculated; there is no history to preserve (OI-024, OI-030). End: set the end date (OI-030). Delete: removes the rental and its pending reminders, after confirmation because it cannot be undone; the tenant and the property are not deleted. Not decided: editing or deleting a tenant or a property on its own, and what happens to their rentals (OI-032). |
| OI-032 | Question | Can a tenant or a property be edited or deleted on its own, and what happens to the rentals that use it? | CTX-002 | Owner | Open | |

## Technologies mentioned (not approved)
React Native, Expo, TypeScript, SQLite, local notifications. The source says they are preliminary references and not approved decisions (CTX-001 §8). They enter the architecture only through ADRs approved by the Owner.
