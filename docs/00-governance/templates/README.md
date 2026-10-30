# Templates

Copy a template, fill it, set `status: Draft`. Only the Owner sets `Approved`.
Omit optional fields that add no traceability or usefulness.

Common metadata header (document-level files):

```markdown
---
id: <stable ID, see traceability.md>
title: <text>
status: Draft | In review | Approved | Obsolete
version: <MAJOR.MINOR>
date: <YYYY-MM-DD>
approved_by: <Owner, only when status = Approved>
refs: [<related IDs>]
---
```

| Template | Use |
|---|---|
| [vision-scope.md](vision-scope.md) | Vision and scope |
| [functional-requirement.md](functional-requirement.md) | One FR entry |
| [non-functional-requirement.md](non-functional-requirement.md) | One NFR entry |
| [use-case.md](use-case.md) | One UC file (complex flows only) |
| [architecture.md](architecture.md) | Architecture skeleton |
| [adr.md](adr.md) | One ADR file |
| [data-model.md](data-model.md) | One ENT entry |
| [test-plan.md](test-plan.md) | Test plan |
| [risk-register.md](risk-register.md) | Risk and debt table |
| [changelog-entry.md](changelog-entry.md) | Changelog entry |
