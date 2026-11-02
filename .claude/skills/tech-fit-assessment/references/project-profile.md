# Project profile: Apartment Mobile (shortcut, verify against the documents)

Written 2026-11-02 from the approved documents and the Owner phone. If a document has changed, the document wins; update this file when it does.

## What the product is
Offline Android app for one landlord: properties, tenants, rentals, monthly collections, local reminders, WhatsApp and call shortcuts, JSON backup. Kotlin, Jetpack Compose with Material 3, Room, kotlinx.serialization, exact alarms (ADR-0001 to ADR-0005). Installed by APK on one phone, personal use, Spanish only.

## Rules a technology can break
| Rule | Meaning for a new technology |
|------|------------------------------|
| NFR-001 | Every function works without a connection. |
| NFR-002, SEC-007 | Data stays on the phone; the app declares no Internet permission and the manifest is tested (TC-026). Nothing can be downloaded or sent by the app. |
| NFR-003, NFR-005 | Data survives restarts; an import replaces everything or nothing. A new component must never write data by itself. |
| NFR-004, ADR-0003 | Reminders on time (5 minutes) with exact alarms; verified on the phone with the screen off and deep idle. |
| NFR-006 | Android 13 or later, installed from an APK, no store account. |
| NFR-007, NFR-008 | All text in Spanish; amounts in whole colones. |
| NFR-009 | Text follows the phone font size; checked at the largest size. |
| NFR-010, OI-040 | No app lock; backup file not encrypted. |
| ADR-0001 | Native Kotlin; native C or C++ code would be new. |
| ADR-0002, OI-042 | Room on SQLite is the only store; Android backup and device transfer are blocked. |
| AGENTS.md | Nothing is built that does not trace to an approved requirement; the agent never approves. |

## Current numbers
- Release file about 18 MB (`app/build/outputs/apk/release/app-release.apk`); 74 unit tests and 60 device tests.
- Toolchain: Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, JDK 17, Compose BOM 2024.06.00, minSdk 33, targetSdk 33.

## Owner phone
Motorola moto g72, Android 13, chipset MediaTek Helio G99 (2 Cortex-A76 and 6 Cortex-A55 cores, Mali-G57 MC2), 6 or 8 GB RAM (confirm with `scripts/device_probe.sh`), serial in `adb devices`. A free-RAM check showed the development computer has about 6 GB free of 16 GB when many tools are open, which matters for emulators and builds.

## Tools to test against the project
Unit and device tests, the delivery test, the scenario setup test and the emulator are described in `docs/07-development/development-guide.md`. The emulator is Android API 37, not the target 13; speed and battery questions need the real phone.

## Where decisions go
Open question or proposal: `docs/09-management/pending-register.md`. Approved changes: a CHG entry in `docs/CHANGELOG.md` and an Owner commit `approve:`. Progress page: `docs/09-management/roadmap.html`, updated after every commit.
