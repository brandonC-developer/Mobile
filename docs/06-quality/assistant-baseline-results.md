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

**Spike, throwaway.** Code: `app/src/test/java/app/apartment/mobile/spike/` (`AssistantBaseline.kt`, `AssistantBaselineTest.kt`). Run: `./gradlew :app:testDebugUnitTest --tests "*AssistantBaselineTest*"`; the report is written to `app/build/assistant-baseline.txt`. Stage: JVM only (stage 1 of the test policy). No model, no Android code. Owner decision 2026-11-02: the spike code is kept to repeat the measurement (with the Owner phrases and later with the models). It stays test-only, outside the product code, and may be deleted when the study ends.

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

## Addendum: the Owner's own phrases (2026-11-02, untuned)
File: `assistant-owner-phrases.psv` (9 phrases cut from one message; expected labels are the agent's reading, 4 marked uncertain). The rules were not changed; only the names Jorge, Pablo and Marco were added to the roster, because tenant names are data. Harness: `AssistantOwnerPhrasesTest`, report in `app/build/assistant-owner.txt`.

| Result | Value |
|--------|-------|
| Exact match, all 9 | 3/9 (33%) |
| Exact match, the 5 phrases with a certain label | 1/5 (20%) |
| Same rules on the agent-written set | 97.4% |

What failed and why:
- **Addressing by apartment, not by name** (O02 "apartamento 1 ya pago", O06 "apartamento 16 ya tiene un año"): the catalog assumed tenant names. The Owner points to the property. A reference resolver in the app (property name or tenant name, from the database) is needed; this is data lookup, not language understanding.
- **A question not in the catalog** (O05 "cuántos meses tiene aquí Marco", O06): how long a tenant has been in the rental. New intent `tenant_info` with field `duration` (the rental `startDate` exists, CHG-001).
- **Verb forms not anticipated** (O09 "el monto del María cambió a 150 000"): "cambió" in the past tense as a statement; answered as a read instead of a change. Harmless here, wrong all the same.
- **A destructive misreading** (O03 "elimina a Jorge, apartamento 3 ya se fue"): answered *delete rental*; under the Owner rule "quita means end" the reasonable action is *end*, which keeps history. This is the **harmful** kind of error and happened on real wording, not on the author's own.
- **Unclear scope** (O08 "eliminar información de Carlos"): delete the person or the rental? A real ambiguity, the assistant should ask.
- **Not tested but visible:** no accents, no capitals, no punctuation, typos ("del María"), several statements in one line, a dictated-sounding "n fecha". The authored set did not have this mix.

Conclusion (agent): the 97.4% was an artifact of the author writing both sides. On real wording the keyword rules cover about a third. That is the first honest evidence that either many more rules or a model is needed; it does **not** yet show a model would do better, and it shows that a good part of the problem (apartment lookup, new questions, ambiguity handling) belongs to the app, not to the model. Next: the Owner answers four label questions, the 9 phrases join the dev set, and the models are measured on the same phrases.

## Correction (Owner, 2026-11-02): names and apartments are data from the local database
The first run of the Owner phrases had tenant names written inside the rule code, which is wrong for the product: tenants and properties change, and the app reads them from the local database. The spike now takes a `Roster` (tenant names and property names) as an input, loaded in the tests from `docs/06-quality/assistant-test-roster.psv`, which stands in for the database. The rules contain no name. A property mentioned by its stored name ("apartamento 3") is resolved against that list.

Rerun: agent-written set unchanged (97.4% on test). Owner phrases **4/9** (was 3/9): O02 "apartamento 1 ya pagó" now passes because the property lookup works; the other failures are unchanged (O03 delete instead of end, O05 and O06 duration questions, O08 delete scope, O09 past-tense change). No other rule was added or tuned, so those five stay as findings. These 9 phrases are now part of the dev data and can no longer count as an untouched test.

Design consequences (proposal for the assistant requirements and the ADR):
1. **Entity resolution belongs to the app.** The model or rules extract the words that name a person or a place ("marco", "apartamento 16"); the app matches them to database rows, ignoring accents and case, and tolerating typos ("del María"). Ambiguous or missing matches produce a question with buttons.
2. **The model never needs the data to understand a request, and should not get it.** If a model is used, the prompt carries only the phrase (and at most the list of names to help spelling, to be decided); results come back as text spans and slots, not as rows.
3. **Test rosters come from a file**, so the same measurements can be repeated with other names, two tenants with one name, or an empty database.
