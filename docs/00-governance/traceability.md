---
id: DOC-TRC
title: Identifiers and Traceability
status: Draft
version: 0.1
date: 2026-10-30
approved_by:
refs: [DOC-GOV]
---

# Identifiers and Traceability

## Identifiers
| Prefix | Element | Example |
|---|---|---|
| OBJ | Product objective | OBJ-001 |
| FR / NFR | Functional / non-functional requirement | FR-014 |
| UC | Use case | UC-003 |
| AC | Acceptance criterion | AC-FR-014-2 |
| ADR | Architecture decision record | ADR-0007 |
| VW | Architecture view | VW-001 |
| CMP | Component | CMP-004 |
| ENT | Data entity | ENT-002 |
| TC | Test case | TC-031 |
| RSK | Risk or technical debt | RSK-005 |
| CHG | Change | CHG-007 |
| OI | Open item (assumption, unknown, question, pending decision) | OI-001 |
| DOC | Document | DOC-GOV |

Rules:
- An ID is never reused or renumbered. A withdrawn element stays with status `Withdrawn`.
- Numbers are sequential and carry no meaning.
- Reference by ID; add a relative link to the file when it helps navigation.

## Trace chain
`OBJ → FR/NFR → UC → CMP ← ADR`, and `FR/NFR → AC → TC`.

Each element declares its links **backwards** in `refs` (an FR names its OBJ; a TC names the FR and AC it verifies). The matrix below is derived from `refs`, never the reverse.

## Completeness rules (checked in review)
1. Every approved FR/NFR has an originating OBJ and at least one AC.
2. Every AC has at least one TC.
3. Every CMP traces to at least one FR/NFR.
4. Every approved ADR cites the requirements that motivate it.

Automated link checking: pending, see OI-004.

## Traceability matrix
Empty until requirements exist.

| OBJ | FR/NFR | UC | CMP | ADR | AC | TC |
|---|---|---|---|---|---|---|
