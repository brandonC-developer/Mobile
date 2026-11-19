---
id: DOC-TRC
title: Identifiers and Traceability
status: Draft
version: 0.2
date: 2026-11-19
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
Derived from document refs on 2026-11-19 (29 FR, 10 NFR; all completeness rules 1–4 verified).

| OBJ | FR/NFR | UC | CMP | ADR | AC | TC |
|---|---|---|---|---|---|---|
| OBJ-001 | FR-001 | UC-001 | CMP-001, CMP-002 | ADR-0002 | AC-FR-001-1, AC-FR-001-2 | TC-001 |
| OBJ-001 | FR-002 | UC-001 | CMP-001, CMP-002 | ADR-0002 | AC-FR-002-1 | TC-033 |
| OBJ-001 | FR-003 | UC-001 | CMP-001, CMP-002 | ADR-0002 | AC-FR-003-1, AC-FR-003-2 | TC-002 |
| OBJ-001 | FR-004 | UC-002 | CMP-001, CMP-002 | ADR-0002 | AC-FR-004-1, AC-FR-004-2 | TC-003, TC-032 |
| OBJ-001 | FR-005 | UC-002 | CMP-001, CMP-002 | ADR-0002 | AC-FR-005-1 | TC-003 |
| OBJ-001 | FR-006 | UC-011 | CMP-001, CMP-002, CMP-004 | ADR-0002, ADR-0004 | AC-FR-006-1 to AC-FR-006-4 | TC-004 |
| OBJ-001 | FR-007 | UC-003 | CMP-001, CMP-002 | ADR-0002 | AC-FR-007-1 to AC-FR-007-3 | TC-005, TC-032 |
| OBJ-001 | FR-008 | UC-003 | CMP-001, CMP-002 | ADR-0002 | AC-FR-008-1 to AC-FR-008-3 | TC-006 |
| OBJ-001 | FR-009 | UC-008 | CMP-001 | — | AC-FR-009-1 | TC-007 |
| OBJ-001 | FR-010 | UC-008 | CMP-001, CMP-002, CMP-004 | ADR-0003 | AC-FR-010-1, AC-FR-010-2 | TC-008 |
| OBJ-001 | FR-011 | UC-009 | CMP-001, CMP-002, CMP-003, CMP-004 | ADR-0003 | AC-FR-011-1 to AC-FR-011-3 | TC-009 |
| OBJ-001 | FR-012 | UC-010 | CMP-001, CMP-002, CMP-004 | ADR-0002, ADR-0003 | AC-FR-012-1 to AC-FR-012-3 | TC-010 |
| OBJ-001 | FR-013 | UC-003 | CMP-001, CMP-003, CMP-004 | ADR-0003, ADR-0004 | AC-FR-013-1 to AC-FR-013-3 | TC-011, TC-031 |
| OBJ-002 | FR-014 | UC-005 | CMP-001, CMP-003 | — | AC-FR-014-1 to AC-FR-014-3 | TC-012 |
| OBJ-002 | FR-015 | UC-004 | CMP-001, CMP-003 | — | AC-FR-015-1 to AC-FR-015-3 | TC-013 |
| OBJ-002 | FR-016 | UC-004 | CMP-001, CMP-003 | — | AC-FR-016-1 to AC-FR-016-3 | TC-014, TC-035 |
| OBJ-002 | FR-017 | UC-004 | CMP-001, CMP-003 | — | AC-FR-017-1 | TC-013 |
| OBJ-003 | FR-018 | UC-006, UC-012 | CMP-001, CMP-004 | ADR-0003 | AC-FR-018-1, AC-FR-018-2 | TC-015 |
| OBJ-003 | FR-019 | UC-012 | CMP-001, CMP-004 | ADR-0003 | AC-FR-019-1 | TC-016 |
| OBJ-003 | FR-020 | UC-006 | CMP-001, CMP-005 | — | AC-FR-020-1 | TC-017 |
| OBJ-003 | FR-021 | UC-006, UC-015 | CMP-002, CMP-004, CMP-006 | ADR-0001, ADR-0003 | AC-FR-021-1, AC-FR-021-2 | TC-018 |
| OBJ-003 | FR-022 | UC-006, UC-015 | CMP-004, CMP-005 | ADR-0003 | AC-FR-022-1 | TC-018 |
| OBJ-003 | FR-023 | UC-003 | CMP-009 | ADR-0001, ADR-0003 | AC-FR-023-1, AC-FR-023-2 | TC-020 |
| OBJ-003 | FR-024 | UC-003 | CMP-001, CMP-009 | ADR-0003 | AC-FR-024-1, AC-FR-024-2 | TC-020 |
| OBJ-004 | FR-025 | UC-007 | CMP-001, CMP-008 | — | AC-FR-025-1, AC-FR-025-2 | TC-021 |
| OBJ-004 | FR-026 | UC-007 | CMP-001, CMP-008 | — | AC-FR-026-1, AC-FR-026-2 | TC-021 |
| OBJ-005 | FR-027 | UC-013 | CMP-001, CMP-007 | ADR-0002, ADR-0004 | AC-FR-027-1, AC-FR-027-2 | TC-022 |
| OBJ-005 | FR-028 | UC-014 | CMP-001, CMP-007 | ADR-0002, ADR-0004 | AC-FR-028-1 to AC-FR-028-3 | TC-023 |
| OBJ-005 | FR-029 | UC-014 | CMP-001, CMP-007 | ADR-0002, ADR-0004 | AC-FR-029-1, AC-FR-029-2 | TC-024 |
| OBJ-001, OBJ-002, OBJ-003 | NFR-001 | — | CMP-004 | ADR-0001, ADR-0003 | Airplane mode verification | TC-025 |
| OBJ-005 | NFR-002 | — | CMP-002, CMP-007 | ADR-0001, ADR-0002, ADR-0004 | Manifest & network flow review | TC-026 |
| OBJ-001 | NFR-003 | — | CMP-002 | ADR-0002 | Persistence across close & reboot | TC-027 |
| OBJ-003 | NFR-004 | UC-006 | CMP-004, CMP-006 | ADR-0001, ADR-0003 | <= 5 min delay on device | TC-016, TC-019 |
| OBJ-005 | NFR-005 | UC-014 | CMP-002, CMP-007 | ADR-0002, ADR-0004 | Atomic all-or-nothing import | TC-023, TC-024 |
| OBJ-001 | NFR-006 | — | CMP-001 to CMP-009 | ADR-0001, ADR-0005 | Android 13+ APK install | TC-028 |
| OBJ-001 | NFR-007 | — | CMP-001 | — | Spanish-only UI review | TC-029 |
| OBJ-001 | NFR-008 | — | CMP-001, CMP-002, CMP-003 | ADR-0002 | Whole colones (₡) without cents | TC-029 |
| OBJ-001 | NFR-009 | — | CMP-001 | ADR-0005 | Largest phone font-size check | TC-030 |
| OBJ-005 | NFR-010 | — | CMP-007 | — | No app lock; unencrypted backup warning | TC-034 |
