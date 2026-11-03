---
id: DOC-TST-AST-R2
title: Assistant on-device model, measurements so far (option B1)
status: Draft
version: 0.1
date: 2026-11-02
approved_by:
refs: [DOC-TFA-001, DOC-TST-AST, DOC-TST-AST-R1, OI-049, RSK-015]
---

# On-device model measurements, so far

**Spike, throwaway, on its own branch `spike/local-model`** (commit `7ce534b`), not on `main`: it changes the build (a test-only runtime dependency in the debug app and a compiler flag), which the product should not carry. Model: Qwen3-0.6B, file `Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm` (343 490 560 bytes, Apache-2.0), runtime LiteRT-LM 0.19.0 (`litertlm-android`). Both devices ran the separate debug app `app.apartment.mobile.debug`; the release app was not touched. The model file was pushed with adb; the app downloads nothing.

Accuracy on the 100-phrase set was measured on 2026-11-02 (section "Accuracy on the 100 phrases"). It fails the targets.

## Measured facts
| Item | Result | Where |
|------|--------|-------|
| Compatibility with the project's Kotlin | LiteRT-LM 0.19.0 pulls Kotlin 2.4.0 metadata; the project uses Kotlin 1.9.24 and failed to compile ("incompatible version") until the flag `-Xskip-metadata-version-check` was added. A clean integration needs a Kotlin upgrade (also touches the Compose compiler; related to RSK-015) | build |
| Native library size | `liblitertlm_jni.so`: 22.7 MB for arm64-v8a, 26.8 MB for x86_64 (the guide lists no ABIs; both are present in the package) | APK content |
| Debug app size | 24.0 MB before, 83.1 MB after adding the runtime with both ABIs, unshrunk. The real release delta was not measured; an arm64-only release would carry at least the 22.7 MB library | build |
| Model load | CPU 4.3 to 6.1 s; GPU 13.7 s; emulator 2.4 to 8.2 s | phone / emulator |
| Memory after load (PSS) | CPU 1.2 to 1.6 GB; GPU 2.2 GB; peak 1.7 GB (CPU), 2.2 GB (GPU) | phone |
| Memory available on the phone before the run | about 2.2 to 2.6 GB of 5.86 GB | phone |
| Time per answer, CPU backend | 38 to 57 s (3 samples with the long instructions, 6 with the short ones: 38.6 to 49.1 s) | moto g72 |
| Time per answer, GPU backend | 10.9 to 13.3 s (3 samples) | moto g72 |
| Time per answer on the computer's CPU (emulator) | 13 to 19 s; not representative of the phone | emulator |
| Heat and battery of the phone | 29 to 31 °C and battery level unchanged at 67% after about 5 minutes of CPU inference (level has 1% resolution) | moto g72 |
| Heat of the computer during emulator runs | 78 to 83 °C package sensor with the CPU at 58 to 76% load, for about 25 minutes | development computer |

Targets proposed in DOC-TFA-001: answer in under 3 s, no out-of-memory, under 1% battery per 20 requests. **The speed target is missed by roughly 4x (GPU) to 15x (CPU) with this model and runtime settings.** Memory use is 50 to 100% of what the phone has free.

## First look at the answers (4 to 9 samples; not a score)
- Intent was right on every sample, but all samples were easy "what do I collect this week" questions.
- Some answers came in the model's "thought" channel instead of the normal text, even with the `/no_think` switch; the scorer reads both.
- The JSON was sometimes wrapped in code fences.
- The model **invented slots**: with the long instructions it sometimes added `property: 3` and `tenant: maria` that were in the examples, not in the message; with the short instructions it filled every key with nonsense (amount 500, range "200-800"). Shorter instructions did not make it faster (38 to 49 s) and made the output worse.

## Accuracy on the 100 phrases (measured 2026-11-02, moto g72, GPU backend, Qwen3-0.6B 343 MB int4, long instructions)
Scored by `ModelScoreTest` (branch `spike/local-model`) against the same labels and roster as the rule baseline. Answers in the "thought" channel or inside code fences were accepted as JSON, so the scoring is lenient.

| Group | Intent correct | Intent and all slots | Invalid JSON | Harmful writes (a wrong write intent) |
|-------|----------------|----------------------|--------------|---------------------------------------|
| Authored, test split (77) | 33.8% (26) | 16.9% (13) | 14 | 21 |
| Authored, dev split (13) | 61.5% (8) | 30.8% (4) | 1 | 2 |
| Owner phrases (10) | 30.0% (3) | 10.0% (1) | 0 | 4 |

For comparison, the rule baseline reached 97.4% on the authored test split (optimistic) and 6 of 10 on the Owner phrases, with 1 harmful error.

Time per answer over the 100 phrases: median 15.4 s; some answers took minutes (p90 340 s, max 368 s) because the model kept generating after the JSON and invented extra "Human:" turns. Load 13.4 s; peak memory 2.2 GB (PSS).

What the answers show:
- It copies slots from the instruction examples (property 3, tenant María, day 20, amount 250000) into phrases that do not mention them.
- It fills fields with "none", "no_think" or the phrase's own words.
- Typical wrong writes: "paga el 25" read as a question, "cambia el día" read as a change of amount, "marca como pagado" read as end_rental, WhatsApp read as call.
- 14 of 77 test answers were not valid JSON.
- The phone's USB link dropped four times during GPU load; the run was resumed from the saved answers, so the data is complete (100 of 100).

## What failed
- A full emulator run of 100 phrases ended after about 25 minutes with a native crash (`Segmentation fault` in `liblitertlm_jni.so`, 16:48). Results were written only at the end, so nothing was saved, and the computer ran hot for no result. Cause unknown; the phrase that triggered it is unknown. The test now appends every answer immediately and resumes after a crash.
- The phone disconnected from adb right before the planned full run (GPU backend), so the run did not start.

## Reading (agent)
1. As configured, this model is **not usable for an interactive chat** on the moto g72: 11 s at best, 40 to 57 s on the CPU, and it takes 1.2 to 2.2 GB of the free memory.
2. Speed is dominated by the model's computation, not by the length of the instructions (the short version was not faster).
3. Accuracy is also poor (34% intent on the test split, 21 wrong writes in 77 phrases), so speed and memory are not the only blockers. This says this small general model with prompt-only instructions fails; it does not say larger or fine-tuned models would. FunctionGemma 270M is a base for fine-tuning (58% to 85% reported after tuning) and would need training data.
4. The sensible option for the product is the rule parser with wider phrase coverage, name and apartment resolution from the database, and a clarification question. These measurements do not justify a model. Saved by not adding it: a runtime dependency, a Kotlin upgrade, about 23 MB of library and 2 GB of memory.

## Pending
Owner decision on OI-049: close the model path and build the rule-based assistant (agent recommendation), or measure one more model (FunctionGemma needs the Owner to accept the Gemma terms). The branch `spike/local-model` can be deleted or kept as evidence.
