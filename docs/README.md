---
id: DOC-IDX
title: Documentation Index
status: Draft
version: 0.1
date: 2026-10-30
approved_by:
refs: []
---

# Documentation Index

Documentation is the source of truth of the project. Rules for AI agents: [`../AGENTS.md`](../AGENTS.md).

## Reading order and status
| Area | Document | Status |
|---|---|---|
| Governance | [governance](00-governance/governance.md) · [traceability](00-governance/traceability.md) · [standards note](00-governance/standards-note.md) · [templates](00-governance/templates/) | Draft |
| Context | [vision-scope](01-context/vision-scope.md) · [glossary](01-context/glossary.md) | Draft (v0.1, proposed) |
| Requirements | [functional](02-requirements/functional.md) · [non-functional](02-requirements/non-functional.md) · [use-cases](02-requirements/use-cases/) | Empty template |
| Architecture | [architecture](03-architecture/architecture.md) · [adr](03-architecture/adr/) · [diagrams](03-architecture/diagrams/) | Empty template |
| Design | [components](04-design/components.md) · [data-model](04-design/data-model.md) · [interfaces-integrations](04-design/interfaces-integrations.md) | Empty template |
| Security and privacy | [security-privacy](05-security-privacy/security-privacy.md) | Empty template |
| Quality | [test-plan](06-quality/test-plan.md) | Empty template |
| Development | [development-guide](07-development/development-guide.md) | Empty template |
| Operations | [deployment-operations](08-operations/deployment-operations.md) | Empty template |
| Management | [risk-register](09-management/risk-register.md) · [pending-register](09-management/pending-register.md) | Empty template / Draft |

Changes: [CHANGELOG](CHANGELOG.md).

## Context incorporation plan
No project context has been provided yet (OI-009). It is incorporated one document at a time, from abstract to concrete, each phase closed by Owner approval:

1. Skeleton installed (this phase).
2. Raw context received and logged in the pending register, without interpretation.
3. Vision, scope and glossary.
4. Requirements (FR, NFR, UC) with trace to objectives.
5. Architecture and stack, proposed as ADRs; nothing is approved without the Owner.
6. Design, data, security, tests.
7. Development, only with approved requirements and architecture.
