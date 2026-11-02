# Report template

Copy this structure into `docs/03-architecture/assessments/<topic>.md`. Keep it in English, short, and cite IDs. Tables beat paragraphs here because the Owner compares options.

```markdown
---
id: DOC-TFA-nnn
title: Technology fit assessment, <topic>
status: Draft
version: 0.1
date: YYYY-MM-DD
approved_by:
refs: [<requirement, ADR and OI ids it touches>]
---

# Technology fit assessment: <topic>

> **Status:** Draft by the agent. Evidence grades: measured, documented, reported, assumed (see the skill references).

## 1. Question and use cases
What the Owner asked, and the concrete use cases assumed.

## 2. Verdict
One line from the scale (Compatible / Compatible with changes / Conflicts / Unknown, measure first), then the three facts that decide it.

## 3. Fit against the project
| Rule (id) | Result (OK / needs change / conflict / unknown) | Evidence |
|-----------|--------------------------------------------------|----------|

## 4. Price
| Item | Cost | Grade | Source and date |
|------|------|-------|-----------------|
Estimate for this project, with its assumptions listed.

## 5. Software load
Added size, storage, memory, native code, permissions, dependencies and licences, build effect.

## 6. Processing power and optimization
Phone profile (from the probe) and what it implies; battery, memory, speed. Mark measured and unknown.

## 7. Options compared
| Option | Fit | Price | Load | Main risk |
|--------|-----|-------|------|-----------|

## 8. Advantages and disadvantages
Two short lists for the recommended option.

## 9. Risks
Each with likelihood, impact and mitigation (candidates for the risk register).

## 10. Test plan against the project
| Test | Measures | Device | Pass criterion | Status |
|------|----------|--------|----------------|--------|

## 11. Verified, unverified, sources
Facts verified (link), facts not verified, links.

## 12. Recommendation and decisions needed
What the agent recommends and exactly what the Owner must decide. No approval is implied.
```
