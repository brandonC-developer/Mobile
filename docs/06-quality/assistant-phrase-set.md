---
id: DOC-TST-AST
title: Assistant phrase set, method and use
status: Draft
version: 0.1
date: 2026-11-02
approved_by:
refs: [DOC-TFA-001, DOC-TFA-001A, OI-049, OI-050, NFR-007]
---

# Assistant phrase set

Data file: `assistant-phrase-set.psv` (pipe-separated, UTF-8, header row). 90 phrases, written by the agent, **not yet reviewed by the Owner**. All names are invented.

## Columns
`id` · `text` · `intent` · `slots` (key=value joined by commas; relative dates are kept as words, the app resolves them) · `style` (tags) · `split`.
`split`: `dev` (13 phrases, used to tune rules and prompts) and `test` (77, never used for tuning). Report accuracy on `test` only.

## How the set was built
- **Scope** comes from the Owner's catalog R1 to R10 (DOC-TFA-001A) plus "unmark paid" (needed to undo R6), clarification, unknown name, out-of-scope, cancel and confirm.
- **Structure** follows the usual intent-and-slot setup of public assistant datasets: the MASSIVE dataset (Amazon, 51 languages, 60 intents, 55 slots, CC BY 4.0) is the published reference for this format, with Spanish included ([paper](https://arxiv.org/pdf/2204.08582), [dataset](https://huggingface.co/datasets/AmazonScience/massive)). Its Spanish phrases were **not** copied; the domain here is different. Whether MASSIVE Spanish is Spain or Latin American Spanish was not verified.
- **Language variety.** The Owner's phone numbers use +506, so Costa Rican Spanish is assumed (assumption). Published surveys describe `vos` as the default informal pronoun there, with a rise of `usted` among younger speakers ([Michnowicz, Despain and Gorham](https://www.jbe-platform.com/content/books/9789027267009-ihll.10.12mic), [Costa Rica Spanish](https://costaricaspanish.net/the-use-of-vos-in-costa-rica)). So each action appears in several forms: tú ("llama"), vos ("llamá"), usted ("llámeme"), statements ("María ya pagó") and telegraphic text ("cobros de la semana"). The exact imperative forms are from the agent's knowledge, not from those sources; the Owner should correct any that sound wrong.
- **Spoken-language effects.** Practitioner and research material on voice interfaces reports hesitations, restarts and self-corrections as common, and politeness words ("por favor") as common ([overview of spoken behavior](https://arxiv.org/pdf/2410.03147), [disfluency note](https://waywithwords.net/blog/disfluencies-processed-in-training-voice), [politeness study](https://mirrorlab.mines.edu/publications/williams2020icsr/icsr_2020__wakewords-2)). The set therefore has tags `disfluency`, `self_correction`, `politeness`, `ellipsis`. The sources are mostly practitioner writing and English; they do not measure Costa Rican Spanish, so the mix is a proposal.
- **Numbers and dates** appear as digits, with a space ("250 000"), without separator ("275000"), as words ("veinticinco", "doscientos cincuenta mil"), with "mil", with the ₡ symbol, and as relative expressions ("pasado mañana", "fin de mes", "la otra semana").

## Composition
11 week, 6 month total, 8 by date, 8 tenant info, 4 call, 4 WhatsApp, 6 paid, 2 unpaid, 10 change day, 7 change amount, 6 end rental, 4 delete rental, 5 clarification or unknown name, 6 out of scope, 3 confirm or cancel. Reads are the biggest group because they are the first stage.

## Known limits
- 90 phrases give wide error bars (a single phrase is about 1.1 points). It is a screening set, not a benchmark.
- Written by one author: style bias. The Owner's own phrases should replace or extend it; real phrases weigh more than invented ones.
- Phrases P076 to P080 need test data (two tenants named María, no tenant named Pedro).
- No speech-recognition errors yet. Voice needs a separate set made from transcripts.

## Scoring (proposal)
Intent accuracy, slot F1 per slot, exact match (intent plus all slots), and **harmful error rate**: a write or delete executed on the wrong tenant or value. The pass line is zero harmful errors at the confirmation step, because writes are always shown first.
Targets from DOC-TFA-001 section 10: at least 90% intent and 85% slot accuracy.

## Next step
Run the rule-based baseline (option A) against the `test` split on the JVM, then the models.
