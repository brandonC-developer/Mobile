---
id: DOC-GOV
title: Documentation Governance
status: Draft
version: 0.1
date: 2026-10-30
approved_by:
refs: [DOC-TRC]
---

# Documentation Governance

## Roles
- **Owner:** the only approver of documents, requirements and decisions (decision D4, 2026-10-30).
- **Hats** (responsibility areas, not people): product, architecture, quality, security, operations, documentation. All currently held by the Owner.
- **AI agents:** may draft, propose and review. Never approve.

## Language and format
Documentation is written in English, in Markdown, and kept in Git. File names, folders and IDs are in English.

## Document statuses
| Status | Meaning | Who sets it |
|---|---|---|
| Draft | Being written | Author |
| In review | Awaiting Owner review | Author |
| Approved | Current source of truth | Owner only, with evidence |
| Obsolete | Replaced or withdrawn; kept for history | Owner |

Evidence of approval: a commit by the Owner whose message cites the approved ID, e.g. `approve: ADR-0001`. Without evidence the maximum status is `In review`.

## Metadata header
Every document starts with: `id`, `title`, `status`, `version`, `date`, `approved_by`, `refs`.

## Versioning
- Git is the version control system.
- Document version `MAJOR.MINOR`: MAJOR when meaning or scope changes, MINOR for compatible clarifications or additions.
- Approvals are recorded in `docs/CHANGELOG.md`.
- Branching and pull-request policy: pending, see OI-002.

## Change control
- **Requirements:** a change to an approved FR/NFR is recorded as `CHG-nnn` with reason, impact on UC, ADR, CMP, TC, and approval. Update requirements first, then derived elements.
- **Architecture:** an approved ADR is never edited. A new ADR supersedes it and the old one becomes `Superseded`. `architecture.md` is updated after the ADR.
- **Conflicts between documents:** resolved using the source hierarchy in `AGENTS.md`.

## Review and obsolescence
- Review at the end of each milestone and at each major change. Exact cadence: pending, see OI-003.
- Obsolete documents are marked `Obsolete`, point to their replacement and are not deleted.

## Pending information
Every assumption, unknown or open decision goes in `docs/09-management/pending-register.md` with an `OI-nnn` ID and is marked in text as `[OI-nnn]`. No silent assumptions or loose TODOs. An assumption becomes fact or decision only with approval evidence.

## Rigor labels
Use consistently: verified fact · recommendation · approved decision · pending decision · assumption · unknown.
