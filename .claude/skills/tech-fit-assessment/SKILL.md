---
name: tech-fit-assessment
description: Evaluates whether a technology, library, framework, SDK, model, service or tool fits this project (Apartment Mobile) before anything is built. Covers price and licence, compatibility with the approved requirements and ADRs, software load (app size, dependencies, build, maintenance), optimization (battery, memory, storage), processing power of the Owner phone, and a test plan to measure it against the project. Use it whenever the Owner asks if something "can be used", "would work", "is compatible", "how much does it cost", "how heavy is it", "will the phone handle it", or wants to compare options or implement a new technology, even if the words "assessment" or "report" are never used (for example "se puede usar X en el telefono", "que tan pesado es", "cuanto cuesta", "es compatible con el proyecto").
---

# Technology fit assessment

Purpose: give the Owner a decision-ready answer on a technology, checked against this project and this phone, instead of a generic opinion. A technology that is fine in general can still break a rule of the project (for example the app has no Internet permission) or run badly on the Owner phone, so the check is always against the project, with evidence.

## Before anything else
1. Read `docs/README.md`, then the documents that decide fit: `docs/02-requirements/non-functional.md`, the approved ADRs in `docs/03-architecture/adr/`, `docs/09-management/risk-register.md` and `docs/09-management/pending-register.md`. `references/project-profile.md` is a shortcut to what these said when it was written; the documents win if they differ.
2. Follow `AGENTS.md`: Approved documents outrank drafts; never implement anything that does not trace to an approved requirement; present documentation as proposals; label every statement as verified fact, recommendation, approved decision, pending decision, assumption or unknown.
3. If the request names several candidates, assess each one with the same dimensions so the comparison is fair. If it is vague, state the use cases you are assuming and continue; ask only if the answer would change the verdict.

## The eight dimensions
Assess each one, and write "unknown" where there is no evidence. `references/dimensions.md` lists what to look for and what counts as evidence.
1. **Compatibility**: Android 13+, ABIs, build toolchain, each approved NFR and ADR, other dependencies.
2. **Price**: licence, per-use cost, development and maintenance effort, any hardware or account. Give the date and source of every number.
3. **Software load**: size added to the installable file and to storage, native code, new permissions, transitive licences, build time.
4. **Optimization**: battery, memory, startup, storage, behavior in the background and under Doze.
5. **Processing power**: what the Owner phone can do. Probe it with `scripts/device_probe.sh` when the phone is connected; never assume speeds.
6. **Privacy and security**: where data goes, new attack surface (`docs/05-security-privacy/security-privacy.md`).
7. **Maintenance and risk**: project activity, breaking changes, licence drift, lock-in.
8. **Fit with the approved documents**: which requirement or ADR would need a change request or a new ADR. This is what turns a "yes, technically" into a decision for the Owner.

## How to research
Prefer primary sources: official documentation, the licence file, the repository, the vendor pricing page. Secondary articles only point you to the primary source. Fetch the primary page and quote the number; if a page cannot be read, say so and mark the fact unverified instead of filling the gap from memory. Do not invent prices, versions, sizes or benchmarks. When you estimate a cost, show the assumptions so the Owner can change them.

## Test against the project
An assessment that stops at reading is only half done. Add a test plan the Owner can approve:
- what to measure (size added, memory, latency, accuracy, battery), on which device, with which pass criteria;
- the cheapest experiment that answers it (a throwaway spike, a benchmark, a manual check), reusing the project test tools (`docs/07-development/development-guide.md`);
- what you already measured and what is pending. If the phone is connected, run `scripts/device_probe.sh` and include its output; if the technology can be tried cheaply, try it and report the numbers.
Throwaway spikes need the Owner's authorization and stay labeled as such; keeping any of the code is a new request.

## Output
Write the report with `references/report-template.md` as `docs/03-architecture/assessments/<topic>.md` (English, status Draft, with an id `DOC-TFA-nnn`). Then:
- add or update one row in `docs/09-management/pending-register.md` linking the report;
- add a short Spanish summary to the roadmap page (advantages, disadvantages, price, compatibility, what is still to measure) following the roadmap rule in `docs/07-development/development-guide.md`;
- answer the Owner in Spanish with the verdict first, the three facts that decide it, what is unknown, and the decision you need from them.
The Owner decides; a recommendation is not an approval, and a commit by the agent never starts with `approve:`.

## Verdict scale
- **Compatible**: no rule broken, measured or well documented.
- **Compatible with changes**: works if a named requirement, ADR or setting changes (say which, as a proposal).
- **Conflicts**: breaks an approved rule with no reasonable change.
- **Unknown, measure first**: the deciding fact is missing; say which test would settle it.
