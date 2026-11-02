---
id: DOC-SEC
title: Security and Privacy
status: Approved
version: 0.1
date: 2026-11-01
approved_by: Brandon
refs: [DOC-NFR, DOC-DAT, DOC-INT, ADR-0002, ADR-0004]
---

# Security and Privacy

> **Status:** Approved by the Owner in commit `a1bc3ba` (2026-11-02); drafted by the agent. Controls are proposals unless they cite an Owner decision. Scope: one landlord, one phone, personal use (OI-019).

## Data inventory
| Data | Entity | Personal | Where it lives | Leaves the device by |
|------|--------|----------|----------------|----------------------|
| Tenant full name, phone | ENT-002 | yes | Local database | Backup file; WhatsApp or dialer (the number only) |
| Property name, description | ENT-001 | possibly (free text) | Local database | Backup file |
| Rental amount, payment day, dates | ENT-003 | indirectly | Local database | Backup file; notification text on the phone |
| Reminder settings | ENT-004 | no | Local database | Backup file |

Data leaves the device only through the backup export the landlord starts or the external apps the landlord opens (NFR-002).

## Decisions already taken by the Owner
- The phone screen lock is the only protection; no app lock (OI-019, NFR-010).
- The backup file is not encrypted; the app warns before export (OI-040, FR-027).
- No cloud, no account, no analytics (OI-021).
- All Android backup and device transfer are blocked (OI-042).

## Risks and proposed controls
| ID | Risk | Control (proposal) | Residual |
|----|------|--------------------|----------|
| SEC-001 | Unlocked or stolen phone shows tenant data | Relies on the screen lock (Owner decision) | Accepted by the Owner |
| SEC-002 | Backup file shared by mistake or left in a chat or mail | Warning before the share menu; file name carries no personal data; temporary file removed after sharing | Landlord behavior |
| SEC-003 | Data copied by Android automatic backup or device transfer | `allowBackup` off and data-extraction rules excluding both (ADR-0002) | Not tested for a sideloaded APK |
| SEC-004 | Malformed or hostile file imported | Full validation before any write; atomic replacement; reject unknown versions (ADR-0004, FR-029) | Low |
| SEC-005 | Other apps trigger the alarm receivers or read the data | Receivers not exported except the two system events; database in private app storage | Low |
| SEC-006 | Notification text visible on the lock screen | Notifications show property, tenant, amount and date (FR-020). Lock-screen display is left to the phone setting (Owner decision, OI-043) | Accepted by the Owner |
| SEC-007 | Unexpected network use | Do not declare the Internet permission, so NFR-002 can be checked by inspection of the manifest | Low |
| SEC-008 | Debug builds or logs expose personal data | No personal data in logs; APK built in release mode for daily use | Low |

## Open points
Tenant phone numbers are personal data of third parties kept for the landlord's own use; the Owner has not asked for a legal review and none is made here.
