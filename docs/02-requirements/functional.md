---
id: DOC-FR
title: Functional Requirements
status: Approved
version: 0.1
date: 2026-11-01
approved_by: Brandon
refs: [DOC-VIS, DOC-PEN]
---

# Functional Requirements

> **Status:** Approved by the Owner in commit `a1bc3ba` (2026-11-02); drafted by the agent. Each one comes from an Owner decision recorded in the [pending register](../09-management/pending-register.md); the OI is cited in **Origin**. Priorities use MoSCoW (OI-008) and are proposals.

Terms are defined in the [glossary](../01-context/glossary.md). Objectives: [vision-scope](../01-context/vision-scope.md).

## Properties

### FR-001 — Create a property
- **Status:** Approved
- **Origin:** OBJ-001 · OI-027
- **Description:** The system shall let the landlord create a property with a name (required) and a description (optional).
- **Acceptance criteria:**
  - AC-FR-001-1: A property with a name and no description can be saved.
  - AC-FR-001-2: A property without a name cannot be saved.
- **Priority:** Must
- **Refs:** none yet

### FR-002 — Edit a property
- **Status:** Approved
- **Origin:** OBJ-001 · OI-032
- **Description:** The system shall let the landlord edit the name and description of a property at any time.
- **Acceptance criteria:**
  - AC-FR-002-1: After editing, the new name appears wherever the property is shown, including rentals and the weekly summary.
- **Priority:** Must
- **Refs:** none yet

### FR-003 — Properties are never deleted
- **Status:** Approved
- **Origin:** OBJ-001 · OI-033
- **Description:** The system shall not offer any way to delete a property.
- **Acceptance criteria:**
  - AC-FR-003-1: No screen or action deletes a property, whether it is free or occupied.
  - AC-FR-003-2: Deleting a tenant or a rental leaves the property in place.
- **Priority:** Must
- **Refs:** none yet

## Tenants

### FR-004 — Create a tenant
- **Status:** Approved
- **Origin:** OBJ-001 · OI-027 · OI-029
- **Description:** The system shall let the landlord create a tenant with a full name (required) and a phone number (optional). One tenant represents a family or household.
- **Acceptance criteria:**
  - AC-FR-004-1: A tenant with a full name and no phone can be saved.
  - AC-FR-004-2: A tenant without a full name cannot be saved.
- **Priority:** Must
- **Refs:** none yet

### FR-005 — Edit a tenant
- **Status:** Approved
- **Origin:** OBJ-001 · OI-033
- **Description:** The system shall let the landlord edit the name and phone of a tenant at any time, including while the tenant has an active rental.
- **Acceptance criteria:**
  - AC-FR-005-1: A tenant with an active rental can be edited and the new data appears everywhere the tenant is shown.
- **Priority:** Must
- **Refs:** none yet

### FR-006 — Delete a tenant
- **Status:** Approved
- **Origin:** OBJ-001 · OI-032 · OI-034
- **Description:** The system shall let the landlord delete a tenant after a confirmation that states how many rentals will be deleted and that the action cannot be undone. Deleting a tenant shall delete all their rentals, active or ended, and their pending reminders. The properties are kept.
- **Acceptance criteria:**
  - AC-FR-006-1: The confirmation states the number of rentals that will be deleted.
  - AC-FR-006-2: If the landlord cancels, nothing is deleted.
  - AC-FR-006-3: After confirming, the tenant, their rentals and their pending reminders no longer exist.
  - AC-FR-006-4: The properties of the deleted rentals still exist and are free.
- **Priority:** Must
- **Refs:** none yet

## Rentals

### FR-007 — Create a rental
- **Status:** Approved
- **Origin:** OBJ-001 · OI-027 · OI-029 · OI-030
- **Description:** The system shall let the landlord create a rental that links one tenant and one property, with an amount in whole colones, a payment day of the month (1 to 31) and an optional end date.
- **Acceptance criteria:**
  - AC-FR-007-1: A rental with tenant, property, amount and payment day can be saved without an end date.
  - AC-FR-007-2: An amount that is zero, negative or not a whole number is rejected.
  - AC-FR-007-3: An end date earlier than today is rejected.
- **Priority:** Must
- **Refs:** none yet

### FR-008 — One active rental per property and per tenant
- **Status:** Approved
- **Origin:** OBJ-001 · OI-029 · OI-035
- **Description:** The system shall not allow a property or a tenant to be in more than one active rental at the same time. A tenant whose rental ended may be linked to another property, and a free property may be linked to another tenant.
- **Acceptance criteria:**
  - AC-FR-008-1: Creating a rental on an occupied property is rejected with a message.
  - AC-FR-008-2: Creating a rental for a tenant with an active rental is rejected with a message.
  - AC-FR-008-3: After a rental ends, its tenant and its property can each be used in a new rental.
- **Priority:** Must
- **Refs:** none yet

### FR-009 — Fixed tenant and property
- **Status:** Approved
- **Origin:** OBJ-001 · OI-035 (assumption)
- **Description:** The system shall not allow changing the tenant or the property of an existing rental; the landlord creates a new rental instead.
- **Acceptance criteria:**
  - AC-FR-009-1: The rental edit screen offers no way to change the tenant or the property.
- **Priority:** Should
- **Refs:** none yet

### FR-010 — Edit a rental
- **Status:** Approved
- **Origin:** OBJ-001 · OI-031
- **Description:** The system shall let the landlord edit the amount, the payment day and the end date of a rental. The change applies from the next reminder onward and already scheduled future reminders are recalculated.
- **Acceptance criteria:**
  - AC-FR-010-1: After changing the payment day, the calendar and the weekly summary show the new dates for future collections.
  - AC-FR-010-2: After the change, no reminder is scheduled for the old dates.
- **Priority:** Must
- **Refs:** none yet

### FR-011 — End a rental
- **Status:** Approved
- **Origin:** OBJ-001 · OI-030 · OI-031 · OI-034
- **Description:** The system shall let the landlord end a rental by setting its end date. After the end date the rental generates no collections and no reminders, its property is free, and the rental record is kept until its tenant is deleted.
- **Acceptance criteria:**
  - AC-FR-011-1: A rental with an end date shows no collections after that date.
  - AC-FR-011-2: A rental with no end date keeps generating collections until ended.
  - AC-FR-011-3: The property of an ended rental is shown as free.
- **Priority:** Must
- **Refs:** none yet

### FR-012 — Delete a rental
- **Status:** Approved
- **Origin:** OBJ-001 · OI-031
- **Description:** The system shall let the landlord delete a rental after a confirmation that states the action cannot be undone. Deleting a rental shall delete its pending reminders and keep its tenant and its property.
- **Acceptance criteria:**
  - AC-FR-012-1: If the landlord cancels, the rental is kept.
  - AC-FR-012-2: After confirming, the rental and its pending reminders no longer exist.
  - AC-FR-012-3: The tenant and the property still exist.
- **Priority:** Must
- **Refs:** none yet

### FR-013 — Collection date of each month
- **Status:** Approved
- **Origin:** OBJ-001 · OI-010 · OI-014
- **Description:** The system shall derive the collection date of each month from the payment day of the rental, using the last day of the month when the month has no such day. Collections are computed from the rental and not stored in advance.
- **Acceptance criteria:**
  - AC-FR-013-1: With payment day 31, the February collection falls on 28 (29 in a leap year) and the April collection on 30.
  - AC-FR-013-2: With payment day 15, every collection falls on the 15th.
- **Priority:** Must
- **Refs:** none yet

## Calendar and weekly summary

### FR-014 — Calendar of collections
- **Status:** Approved
- **Origin:** OBJ-002 · OI-030 · OI-036
- **Description:** The system shall show a monthly calendar with the collections due today and on future dates, and the amount of each. It shall not show past dates.
- **Acceptance criteria:**
  - AC-FR-014-1: A date with a collection shows the amount on selection.
  - AC-FR-014-2: Dates before today show no collections.
  - AC-FR-014-3: The calendar does not show statuses such as paid or overdue.
- **Priority:** Must
- **Refs:** none yet

### FR-015 — Weekly summary
- **Status:** Approved
- **Origin:** OBJ-002 · OI-025 · OI-015
- **Description:** The system shall show a weekly summary that lists the collections due in a week, Monday to Sunday. Each entry shows the property, the tenant, the date and the amount. A collection appears in the week its date falls in, even when that week spans two months. The summary shows no status.
- **Acceptance criteria:**
  - AC-FR-015-1: A collection due on a Sunday appears in the week that ends on that Sunday, and one due on a Monday in the week that starts on it.
  - AC-FR-015-2: A collection due on the 31st with the week crossing into the next month appears in that week.
  - AC-FR-015-3: A week with no collections shows an empty state.
- **Priority:** Must
- **Refs:** none yet

### FR-016 — Weekly summary navigation
- **Status:** Approved
- **Origin:** OBJ-002 · OI-015 · OI-036
- **Description:** The system shall let the landlord view the previous, the current and the next week, and shall always open on the current week. The previous week is shown only in the summary, not in the calendar.
- **Acceptance criteria:**
  - AC-FR-016-1: Opening the app, including after leaving it open on another week, shows the current week.
  - AC-FR-016-2: The landlord can move one week back and one week forward from the current week, and no further.
- **Priority:** Must
- **Refs:** none yet

### FR-017 — Week total
- **Status:** Approved
- **Origin:** OBJ-002 · CTX-001 §5.5 · OI-041 (assumption)
- **Description:** The system shall show the total amount of the collections of the displayed week.
- **Acceptance criteria:**
  - AC-FR-017-1: The total equals the sum of the amounts listed for that week.
- **Priority:** Should
- **Refs:** none yet

## Reminders

### FR-018 — Reminder offsets
- **Status:** Approved
- **Origin:** OBJ-003 · OI-028
- **Description:** The system shall let the landlord choose which reminders to receive for the collections: 3 days before, 2 days before, 1 day before and the same day. Each can be turned on or off.
- **Acceptance criteria:**
  - AC-FR-018-1: Only the selected offsets produce a reminder.
  - AC-FR-018-2: Turning an offset off removes its pending reminders.
- **Priority:** Must
- **Refs:** none yet

### FR-019 — Reminder time
- **Status:** Approved
- **Origin:** OBJ-003 · OI-028 · OI-041 (assumption)
- **Description:** The system shall let the landlord choose the time of day of the reminders. The time applies to all reminders.
- **Acceptance criteria:**
  - AC-FR-019-1: After changing the time, pending reminders are rescheduled to the new time.
- **Priority:** Must
- **Refs:** none yet

### FR-020 — Reminder content
- **Status:** Approved
- **Origin:** OBJ-003 · OI-028 · OI-041 (assumption)
- **Description:** Each reminder shall state the property, the tenant, the amount and the due date. Tapping it opens the app on the current week.
- **Acceptance criteria:**
  - AC-FR-020-1: A reminder shows property, tenant, amount and due date.
- **Priority:** Should
- **Refs:** none yet

### FR-021 — Rolling schedule of reminders
- **Status:** Approved
- **Origin:** OBJ-003 · OI-014
- **Description:** The system shall schedule only the nearest reminders, up to a limit to be verified on the device (OI-039), and shall reschedule them each time the app is opened and each time a rental is created, edited, ended or deleted.
- **Acceptance criteria:**
  - AC-FR-021-1: After opening the app, the nearest reminders up to the limit are scheduled.
  - AC-FR-021-2: After editing, ending or deleting a rental, no reminder remains for dates that no longer apply.
- **Priority:** Must
- **Refs:** none yet

### FR-022 — Renewal reminder
- **Status:** Approved
- **Origin:** OBJ-003 · OI-014
- **Description:** The system shall schedule a last reminder at the end of the scheduled window that asks the landlord to open the app so the reminders are renewed.
- **Acceptance criteria:**
  - AC-FR-022-1: When the window does not cover all future collections, a renewal reminder exists after the last scheduled reminder.
- **Priority:** Should
- **Refs:** none yet

### FR-023 — Notification permissions
- **Status:** Approved
- **Origin:** OBJ-003 · OI-017 · OI-039
- **Description:** The system shall ask for the notification permission, and for the exact-alarm permission by sending the landlord to Alarms and reminders, the first time a rental is created, after a short explanation.
- **Acceptance criteria:**
  - AC-FR-023-1: Creating the first rental shows the explanation and then the permission requests.
  - AC-FR-023-2: The requests are not shown again once granted.
- **Priority:** Must
- **Refs:** none yet

### FR-024 — Warning when permissions are denied
- **Status:** Approved
- **Origin:** OBJ-003 · OI-017
- **Description:** The system shall show a visible warning that reminders may not arrive on time when either permission is denied, without blocking the use of the app.
- **Acceptance criteria:**
  - AC-FR-024-1: With either permission denied, the warning is visible and all other functions work.
  - AC-FR-024-2: With both granted, no warning is shown.
- **Priority:** Must
- **Refs:** none yet

## Contact

### FR-025 — Contact the tenant by WhatsApp
- **Status:** Approved
- **Origin:** OBJ-004 · OI-028
- **Description:** The system shall open WhatsApp on the phone number of the tenant from a rental. If the tenant has no phone, the action is not available.
- **Acceptance criteria:**
  - AC-FR-025-1: With a phone, the action opens the external WhatsApp app on that number.
  - AC-FR-025-2: Without a phone, the action is disabled or hidden.
- **Priority:** Should
- **Refs:** none yet

### FR-026 — Call the tenant
- **Status:** Approved
- **Origin:** OBJ-004 · OI-028
- **Description:** The system shall open the phone dialer on the phone number of the tenant from a rental. If the tenant has no phone, the action is not available.
- **Acceptance criteria:**
  - AC-FR-026-1: With a phone, the action opens the dialer with that number.
  - AC-FR-026-2: Without a phone, the action is disabled or hidden.
- **Priority:** Should
- **Refs:** none yet

## Backup

### FR-027 — Export a backup
- **Status:** Approved
- **Origin:** OBJ-005 · OI-016 · OI-037 · OI-040
- **Description:** The system shall export all data (tenants, properties, rentals and reminder settings) to a single JSON file with a schema version, and offer it through the device share menu. Before exporting it shall warn that the file contains personal data in plain text.
- **Acceptance criteria:**
  - AC-FR-027-1: The warning is shown before the share menu.
  - AC-FR-027-2: The file contains a schema version and all stored data.
- **Priority:** Must
- **Refs:** none yet

### FR-028 — Import a backup
- **Status:** Approved
- **Origin:** OBJ-005 · OI-016 · OI-037
- **Description:** The system shall import a backup file, replacing all current data after an explicit confirmation that states the current data will be lost, and shall reschedule the reminders.
- **Acceptance criteria:**
  - AC-FR-028-1: Without confirmation, the current data is unchanged.
  - AC-FR-028-2: After confirming, the data equals the content of the file and reminders match it.
  - AC-FR-028-3: Exporting and then importing on a clean install yields the same data.
- **Priority:** Must
- **Refs:** none yet

### FR-029 — Reject an invalid backup
- **Status:** Approved
- **Origin:** OBJ-005 · OI-037
- **Description:** The system shall reject a file that is not a valid backup or whose schema version is unknown or newer than the app supports, with a message, leaving the current data unchanged.
- **Acceptance criteria:**
  - AC-FR-029-1: A file with a newer schema version is rejected and the data is unchanged.
  - AC-FR-029-2: A file that is not valid JSON, or lacks required fields, is rejected and the data is unchanged.
- **Priority:** Must
- **Refs:** none yet

## Not yet covered
- **Home screen content (OI-041):** CTX-001 §5.5 mentioned the next payment, its amount and the days left. The Owner has not decided whether the home screen shows more than the weekly summary.
- **Use cases (UC):** not written yet.
- **Withdrawn from the source:** obligation statuses, paid marking, overdue (OI-011, OI-012, OI-024); obligations other than rent (OI-022).
