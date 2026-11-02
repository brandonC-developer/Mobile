---
id: DOC-GLO
title: Glossary
status: Approved
version: 0.1
date: 2026-11-01
approved_by: Brandon
refs: [DOC-PEN]
---

# Glossary

> **Status:** Approved by the Owner in commit `a1bc3ba` (2026-11-02); drafted by the agent. Each term cites where its meaning comes from. Spanish terms are those used in the source context (CTX-001).

| Term | Spanish | Definition | Source |
|---|---|---|---|
| Landlord | arrendador | The user of the app: the person who rents out properties and collects rent | OI-026 |
| Tenant | arrendatario | The person who pays rent. Registered with a full name and an optional phone; represents a family or household. Not a user of the app | OI-026, OI-027, OI-029 |
| Property | inmueble, propiedad | A rentable place, with a name and an optional description. Never deleted; can be edited | OI-027, OI-032 |
| Rental | alquiler | The link between one tenant and one property, with amount, payment day and optional end date. Edited, ended or deleted by the landlord | OI-029, OI-031 |
| Active rental | alquiler activo | A rental with a related tenant that still generates collections, because it has no end date or its end date has not passed | OI-033 |
| Ended rental | alquiler terminado | A rental whose end date has passed. It generates no reminders and is kept until its tenant is deleted | OI-030, OI-034 |
| Occupied property | propiedad ocupada | A property with an active rental | OI-033 |
| Free property | propiedad libre | A property with no active rental; it can receive a new tenant | OI-033, OI-035 |
| Collection | cobro | The rent amount the landlord expects on a given date, derived from an active rental. Replaces the "obligation" of CTX-001, which was written for a tenant user | OI-022, OI-026 |
| Payment day | día de pago | Day of the month on which the collection is due. In a month without that day, the last day of the month is used | OI-010 |
| End date | fecha de fin | Optional date after which a rental stops generating collections and reminders | OI-030 |
| Reminder | recordatorio | A local notification to the landlord about an upcoming collection: 3, 2, 1 days before and the same day, at a selectable time. Tolerance: up to 5 minutes late | OI-028, OI-017 |
| Calendar | calendario | Monthly view of collections for today and future dates only | OI-030, OI-036 |
| Weekly summary | resumen semanal | List of the collections due in a week, Monday to Sunday. Shows previous, current and next week and opens on the current one | OI-025, OI-015, OI-036 |
| Backup file | archivo de respaldo | Unencrypted JSON file with a schema version, containing tenants, properties, rentals and reminder settings; importing replaces all data | OI-016, OI-037, OI-040 |
| Offline-first | | Core functions work without a connection and the data on the device is the only source of truth | OI-021 |
| CHG | | A formal change that adds, alters or removes an approved element | DOC-GOV |
| OI | | An open item (assumption, unknown, question, pending decision) in the pending register | DOC-TRC |
