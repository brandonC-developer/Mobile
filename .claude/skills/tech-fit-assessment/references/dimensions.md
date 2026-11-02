# What to look for in each dimension

Use this as a checklist. A line you cannot fill is an "unknown", not a blank; unknowns drive the test plan.

## 1. Compatibility
- Minimum Android and API level of the technology against minSdk 33; target and compile SDK it needs.
- ABIs (arm64-v8a is what the Owner phone runs) and whether it needs the NDK or CMake, which this project does not use today.
- Kotlin, AGP, Gradle and JDK versions it requires against the project versions (`gradle/libs.versions.toml`); Compose compiler pairing.
- Each approved NFR and ADR it touches (see `project-profile.md`). Quote the ID and say OK, needs change or conflict.
- Existing libraries it duplicates or clashes with (Room, serialization, Compose).

## 2. Price
- Licence (name, link, whether it allows bundling in an app, notices to ship, restrictions that follow the app).
- Runtime cost: free, per request, per user, per month. Give the vendor page and the date read.
- One-time costs: training or fine-tuning, accounts, developer fees, hardware.
- Cost estimate for this project: assumptions first (requests per month, tokens, users), then the result. One user and one phone is the real scale.
- Effort: days to integrate and to keep up. Say it is the agent's estimate.

## 3. Software load
- Added size of the release file and of storage; extra files the app must carry (models, assets) and how they get onto the phone without Internet.
- Native libraries, extra permissions, services, receivers.
- Dependency tree and transitive licences.
- Build time and the effect on the test suites.

## 4. Optimization
- Memory at runtime (peak and steady) against the free memory of the phone.
- Battery per use and in the background; behavior under Doze and with the screen off (reminders must keep arriving, NFR-004).
- Startup and responsiveness (the project aims at screens that answer well under 400 ms).
- Storage growth over time.

## 5. Processing power
- Phone profile: chipset, cores, RAM, free storage, Android version, ABIs. Run `scripts/device_probe.sh`.
- Whether the technology uses the CPU only, the GPU or a neural unit, and whether that unit exists and is supported on this chipset.
- Measured speed on the phone. If none, write "unknown" and plan the measurement; never reuse a number measured on a different phone.

## 6. Privacy and security
- What leaves the phone and where it goes (NFR-002); what the technology logs or caches; permissions it asks for.
- Personal data involved (tenant names, phones) and whether it would be sent to any third party.

## 7. Maintenance and risk
- Last release and activity, open breaking changes, deprecations, version policy.
- Who controls it, licence changes, and how hard it is to remove later (lock-in).

## 8. Fit with the approved documents
- List every requirement, ADR, risk or open item the choice touches.
- Mark whether adopting it needs a change request (CHG), a new ADR, a new requirement, or nothing.
- This section is where the report becomes a decision for the Owner.

## Evidence grades
- **Measured**: you ran it on the project or the phone. Strongest.
- **Documented**: a primary source states it; give the link and date.
- **Reported**: a secondary source or a vendor claim; say so.
- **Assumed / unknown**: no evidence.
