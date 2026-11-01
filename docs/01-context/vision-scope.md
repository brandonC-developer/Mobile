---
id: DOC-VIS
title: Vision and Scope
status: Draft
version: 0.1
date: 2026-11-01
approved_by:
refs: [DOC-PEN, DOC-GLO]
---

# Vision and Scope

> **Status:** Draft proposed by the agent from the Owner decisions recorded in [`pending-register`](../09-management/pending-register.md). Nothing here is approved. Statements cite the open item (OI) or context input (CTX) they come from.

## Purpose
A mobile app for a landlord to keep track of the rent collections due from their tenants and to be reminded of them, working without Internet. The user is the landlord (OI-026), and for now the only user is the Owner (OI-018).

## Objectives
Success indicators are proposals for the Owner to confirm. Objectives are proposed from CTX-001 §2, adapted to the landlord user (OI-026, OI-028).

| ID | Objective | Success indicator | Status |
|----|-----------|-------------------|--------|
| OBJ-001 | The landlord records properties, tenants and rentals once and the app derives every monthly collection date from them | Proposal: a rental needs only amount, payment day and optional end date to appear in the calendar for all future months | Proposed |
| OBJ-002 | The landlord sees which collections are due and when | Proposal: the current week's collections are visible on opening the app; the calendar shows today and future dates | Proposed |
| OBJ-003 | The landlord is reminded before each collection without depending on Internet | Proposal: reminders arrive at the chosen time within 5 minutes (OI-017) with no connection | Proposed |
| OBJ-004 | The landlord can contact a tenant quickly | Proposal: from a rental, WhatsApp or a phone call opens in the external app (OI-028) | Proposed |
| OBJ-005 | The landlord can keep a copy of their data and recover it on another phone | Proposal: an exported file restores all data on a clean install (OI-016, OI-037) | Proposed |

## Scope
**In scope** (decisions in the pending register):
- Properties, tenants and rentals, one tenant per property at a time (OI-027, OI-029, OI-035).
- Rent collections only, monthly, on a fixed payment day, using the last day of the month when the day does not exist (OI-022, OI-010).
- Rental with optional end date; edit, end and delete (OI-030, OI-031); tenants editable and deletable, properties editable and never deleted (OI-032 to OI-034).
- Calendar of today and future dates (OI-030, OI-036).
- Weekly summary, Monday to Sunday, for the previous, current and next week, opening on the current week (OI-025, OI-015).
- Collection reminders to the landlord only, 3, 2, 1 days before and the same day, with selectable time (OI-028).
- Contact with the tenant through WhatsApp and phone call in external apps (OI-028).
- Backup by exported JSON file and restore by importing it (OI-016, OI-037, OI-040).
- Android 13 or later, installed by APK (OI-018, OI-038); interface in Spanish; amounts in whole Costa Rican colones (OI-020).

**Out of scope for now** (each needs a CHG to be added):
- Obligation statuses: paid, pending, overdue (OI-024).
- Obligations other than rent, such as maintenance fees or services (OI-022).
- Notifying tenants, a tenant-facing app or portal, any backend (OI-028, CTX-001 §7).
- Email contact, property address, tenant email, landlord name (OI-027, OI-028).
- Cloud backup or sync, accounts, multiple users (OI-016, CTX-001 §7, §9).
- Several tenants per property or several properties per tenant (OI-029).
- iOS (OI-018); other languages or currencies (OI-020); app lock, PIN or biometrics, backup encryption (OI-019, OI-040).
- Payment processing, bank or SINPE integration, own chat, AI or legal advice, advanced financial reports (CTX-001 §9).

## Stakeholders
| Role | Concerns |
|------|----------|
| Owner and sole user (landlord) | Not missing a collection; fast to use; data kept on the phone and recoverable |
| Tenants (arrendatarios) | Not users of the app and not notified (OI-028); they are only a contact record |

## Constraints
| Constraint | Source |
|---|---|
| Offline-first: core functions work without a connection; user data lives on the device as the only source of truth; Internet only to open external apps | OI-021 |
| No backend | CTX-001 §7, OI-028 |
| Android 13 or later, direct APK install | OI-018, OI-038 |
| Technologies (React Native, Expo, TypeScript, SQLite, local notifications) are candidates, not decisions; each enters through an approved ADR | CTX-001 §8, OI-021 |
| Phone screen lock is the only protection; no app-level lock | OI-019 |

## Assumptions
- The rental's tenant and property do not change after creation; a new rental is created instead. [OI-035]
- The minimum Android version is 13, derived from the Owner device. [OI-038]
- Behavior of reminders after a forced close or a reboot of the phone is unverified. [OI-039]
