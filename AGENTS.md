# Instructions for AI Agents

Single source of rules for any AI agent working on this project. `CLAUDE.md` only points here.

## Source hierarchy (highest authority first)
1. Explicit, current instruction from the Owner.
2. Documents with `status: Approved` (approved ADRs > approved requirements > the rest).
3. Documents `In review` (guidance only, not binding).
4. Drafts (context only, never treated as fact).

If two sources contradict each other, do not pick one: report the conflict and ask the Owner to resolve it.

## Before acting
1. Read `docs/README.md` and the documents relevant to the task.
2. Identify the affected IDs (FR, NFR, UC, ADR, CMP, ENT, TC).
3. If something needed is not documented, declare it as missing information.

## Forbidden without explicit Owner authorization
- Creating, changing or deleting requirements on your own initiative.
- Contradicting or changing an approved ADR, or choosing a technology that is not approved.
- Implementing functionality that does not trace to an approved requirement.
- Setting `status: Approved` on anything, or presenting an assumption as a decision.
- Inventing quotes, URLs, versions, standard clauses or project facts.

## Required
- Cite the IDs you rely on (e.g. "per FR-014 and ADR-0003").
- Keep traceability: when you add or change an element, update its `refs`.
- Record doubts, assumptions and conflicts in `docs/09-management/pending-register.md` as proposals.
- Label statements as: verified fact / recommendation / approved decision / pending decision / assumption / unknown.
- Ask for clarification when an ambiguity affects a relevant decision (scope, security, data, architecture). For minor ambiguity, state your assumption and continue.
- Present documentation changes as a reviewable diff. Never treat them as approved.
- Write documentation in English.

## Conflict report format
Documents and IDs in conflict · short quote of each · impact · options.

## Governance
Statuses, versioning, approvals and change control: `docs/00-governance/governance.md`.
ID scheme and traceability rules: `docs/00-governance/traceability.md`.
