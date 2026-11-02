---
id: DOC-OPS
title: Deployment and Operations
status: Draft
version: 0.1
date: 2026-11-02
approved_by:
refs: [NFR-006, NFR-002, ADR-0001, ADR-0004, RSK-017]
---

# Deployment and Operations

> **Status:** Draft written by the agent in milestone H6. Not approved. The app is installed by hand on one phone (NFR-006); there are no servers.

## 1. Build the installable file
Requires JDK 17 and the Android SDK (see [development-guide](../07-development/development-guide.md)).

```
JAVA_HOME="C:\Program Files\Java\jdk-17" ./gradlew :app:assembleRelease
```
The signed file is `app/build/outputs/apk/release/app-release.apk` (about 18 MB). Without the signing properties below, Gradle builds an **unsigned** file that Android refuses to install.

## 2. The signing key
- The release key is a file outside the repository: `C:\Users\<user>\.apartment-keys\apartment-release.jks` (alias `apartment`). It is never committed (`*.jks` is ignored).
- Its passwords are in the Gradle user file `C:\Users\<user>\.gradle\gradle.properties` as `APARTMENT_STORE_FILE`, `APARTMENT_STORE_PASSWORD`, `APARTMENT_KEY_ALIAS`, `APARTMENT_KEY_PASSWORD`.
- **Back up the key file and those four values somewhere safe, away from the phone and the computer** (for example a password manager). **If the key is lost, an update cannot be installed over the app: the app must be uninstalled first, which deletes its data.** Export a backup before that (section 5).
- A debug build is signed with a different, automatic key. A phone with a debug build cannot be updated with a release build, and the other way round, without uninstalling.

## 3. Install on the phone
Either way, Android 13 asks once to allow installs from the source you use.

**With the cable (recommended).** Turn on USB debugging, connect the phone, then:
```
adb install -r app/build/outputs/apk/release/app-release.apk
```
`-r` keeps the data when updating with the same key.

**Without the cable.** Copy `app-release.apk` to the phone (cable, shared folder), open it from the Files app and accept "install unknown apps" for that app.

## 4. First use
1. Open **Apartment**. Register a property and a tenant, then create the first rental.
2. After that first rental the app explains why it needs two permissions. Accept the notification permission, then in the system screen "Alarms & reminders" allow **Apartment**. Without them reminders may be late or missing, and the app shows a warning bar until they are granted.
3. Choose which reminders you want and at what time in **Ajustes**.
4. Recommended: in the phone's battery settings do not restrict the app. Verified on the Owner phone (moto g72, Android 13): reminders arrive on time with the screen off and in forced deep idle.

## 5. Operation
- **Your data lives only on the phone** (NFR-002). Nothing is sent anywhere. Android's own cloud backup and phone-to-phone transfer are blocked on purpose (OI-042).
- **Back up regularly**: Ajustes, Exportar copia, then share the file to a place you trust (mail to yourself, Drive). The file is **not encrypted** and holds names and phones.
- **New phone or reinstall**: install the app, then Ajustes, Importar copia, pick the file. Importing **replaces all current data** after asking.
- **Known limits** (verified, ADR-0003): a forced stop silences all reminders until the app is opened again; after a reboot reminders resume once the phone is unlocked; reminders due while the phone was off are not delivered.
- **Updating the app**: install the new file over the old one with the same key (section 3). Data is kept; the database migrates (ADR-0002).
- **Uninstalling deletes all data.** Export a backup first.

## 6. Checks before handing over a new file
1. `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest` passes, on the phone and the emulator.
2. `./gradlew :app:lintDebug` has no errors.
3. The release file installs on the phone and opens.
4. The documents are in sync (development-guide, QA checklist).

## 7. Verified on the Owner phone (2026-11-02)
- Release file 0.1.0 installed and opened on the moto g72, Android 13. Signing certificate SHA-256: `676d2d3f81f749c51e2fe8b4f5f8f295ffa650a8ccd801fbed46fe2f21c54503` (compare with `apksigner verify --print-certs` to check a file is signed with the right key).
- Reminders: screen off 1 s late; deep idle forced 0 s late; after a reboot and one unlock, delivered at the planned minute without opening the app; a forced stop silences them until the app is opened (TC-016, TC-019).
- Full test suite passes on the phone, also in airplane mode (TC-025). Largest font size checked (TC-030).
