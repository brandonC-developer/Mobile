---
id: DOC-IDX
title: Documentation Index
status: Draft
version: 0.2
date: 2026-11-19
approved_by:
refs: []
---

# Documentation Index

Documentation is the source of truth of the project. Rules for AI agents: [`../AGENTS.md`](../AGENTS.md).

## Reading order and status
| Area | Document | Status |
|---|---|---|
| Governance | [governance](00-governance/governance.md) · [traceability](00-governance/traceability.md) · [standards note](00-governance/standards-note.md) · [templates](00-governance/templates/) | Draft (`traceability` v0.2) |
| Context | [vision-scope](01-context/vision-scope.md) · [glossary](01-context/glossary.md) | `vision-scope`: In review 0.2 (CHG-004; previously Approved 0.1) · `glossary`: Approved 0.1 |
| Requirements | [functional](02-requirements/functional.md) · [non-functional](02-requirements/non-functional.md) · [use-cases](02-requirements/use-cases/) | `functional` & `non-functional`: In review 0.2 (CHG-004) · `use-cases`: Approved 0.1 (UC-001 to UC-015) |
| Architecture | [architecture](03-architecture/architecture.md) · [assessments](03-architecture/assessments/local-assistant-fit.md) · [adr](03-architecture/adr/) · [diagrams](03-architecture/diagrams/) | `architecture`: In review 0.2 (CHG-004) · `ADR-0001` to `ADR-0005`: Approved · `local-assistant-fit`: Draft 0.2 · `assistant-requests-draft`: Draft 0.1 · `diagrams`: empty |
| Design | [components](04-design/components.md) · [data-model](04-design/data-model.md) · [interfaces-integrations](04-design/interfaces-integrations.md) · [ux-framework](04-design/ux-framework.md) | `components` & `interfaces-integrations`: In review 0.2 (CHG-004) · `data-model` & `ux-framework`: Approved 0.1 |
| Security and privacy | [security-privacy](05-security-privacy/security-privacy.md) | Approved 0.1 |
| Quality | [test-plan](06-quality/test-plan.md) · [assistant-phrase-set](06-quality/assistant-phrase-set.md) · [assistant-baseline-results](06-quality/assistant-baseline-results.md) · [assistant-model-results](06-quality/assistant-model-results.md) | `test-plan`: In review 0.2 (CHG-004) · assistant evaluation docs: Draft 0.1 |
| Development | [development-guide](07-development/development-guide.md) | Draft 0.1 |
| Operations | [deployment-operations](08-operations/deployment-operations.md) | Draft 0.1 |
| Management | [risk-register](09-management/risk-register.md) · [pending-register](09-management/pending-register.md) · [roadmap page](09-management/roadmap.html) (Artifact source, updated after every commit) | `risk-register`: In review 0.2 (CHG-004) · `pending-register`: Draft 0.5 · `roadmap.html`: Spanish dashboard |

Changes: [CHANGELOG](CHANGELOG.md).

## Project phase status
1. **Phases 1–2 (Skeleton & Context):** Done (`CTX-001`, `CTX-002`, `OI-008`–`OI-044`).
2. **Phases 3–6 (Vision, Requirements, Architecture, Design, Security, Test Plan):** Approved in Owner commit `a1bc3ba` (2026-11-02).
3. **Phase 7 (Development, Milestones H0–H6 + `CHG-003`):** Implemented in `app/` and verified on emulator and Owner phone (`moto g72`, Android 13); `CHG-001`, `CHG-002`, `CHG-003` and `ADR-0005` approved in Owner commit `f9fcf3f` (2026-11-02).
4. **Current pending items:** Editorial sync `CHG-004` (in review), installing the post-`CHG-003` release APK on the phone (`TC-035`), open product decisions `OI-049` (local assistant) and `OI-050` (paid mark), and governance items `OI-001`–`OI-007`.
