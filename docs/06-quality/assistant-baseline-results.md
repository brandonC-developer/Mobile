---
id: DOC-TST-AST-R1
title: Assistant rule baseline, results (option A)
status: Draft
version: 0.1
date: 2026-11-02
approved_by:
refs: [DOC-TFA-001, DOC-TST-AST, OI-049]
---

# Rule baseline results

**Spike, throwaway.** Code: `app/src/test/java/app/apartment/mobile/spike/` (`AssistantBaseline.kt`, `AssistantBaselineTest.kt`). Run: `./gradlew :app:testDebugUnitTest --tests "*AssistantBaselineTest*"`; the report is written to `app/build/assistant-baseline.txt`. Stage: JVM only (stage 1 of the test policy). No model, no Android code. The Owner decides whether the spike code stays.

## Results (phrase set of 90, split test = 77, dev = 13)
| Measure | Test split | Dev split | Target (DOC-TFA-001) |
|---------|-----------|-----------|----------------------|
| Intent accuracy | 97.4% (75/77) | 100% | at least 90% |
| Exact match (intent and all slots) | 97.4% | 100% | n/a |
| Slot F1 | 98.4% | 100% | at least 85% |
| Harmful errors (a write predicted wrongly) | 1 | 0 | 0 |

Failures on the test split:
- **P048** "Me equivoqué, María no ha pagado": the rules answered *mark as paid* instead of *unmark*. Cause: the rule for "pagado" fires before the negation "no ha pagado" is read. **A harmful error**: it would have shown a wrong confirmation. It is not fixed on purpose (see the validity note); it is the kind of case the confirmation step must catch.
- **P077** "Escríbele a María Gómez": no rule for the verb "escribir" without the word WhatsApp, so the answer was *out of scope*. Harmless.

## Validity: read this before trusting the numbers
- **Same author.** The agent wrote the phrases and then the rules while looking at them. The `test` split was never used to tune, but the author knew its contents, so the vocabulary was covered on purpose. The 97.4% is **optimistic** and is not evidence about real speech.
- **Small and invented.** 90 phrases (about 1.1 points per phrase) and no phrase from the Owner yet.
- **Closed vocabulary.** The rules know five invented names and fixed words. Real tenants have many names, nicknames and misspellings; speech recognition would add errors.
- **What the result does show:** for the ten catalog requests in clear typed Spanish, keyword rules are enough to reach the targets *when the wording is anticipated*, and the main danger is not accuracy but negation and unexpected wording, which is exactly where a model could help and where it could also fail.

## Consequences for the decision
1. A rule parser is a serious option A, not a straw man. It costs about 200 lines, no memory and no new permissions.
2. The honest comparison needs **phrases nobody tuned for**: the Owner's own 10 to 15 phrases, or a second author. Until then no claim that a model is needed or not needed is supported.
3. Writes must keep the confirmation screen; P048 shows why.
4. Next measurement: the Owner's phrases through the same harness, then the models (LiteRT-LM with Qwen3-0.6B) on the same set, emulator first, then the phone.
