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

**Accuracy on the phrase set is NOT measured yet** (see "What failed"). Everything below is size, memory, speed and a first look at the answers.

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

## What failed
- A full emulator run of 100 phrases ended after about 25 minutes with a native crash (`Segmentation fault` in `liblitertlm_jni.so`, 16:48). Results were written only at the end, so nothing was saved, and the computer ran hot for no result. Cause unknown; the phrase that triggered it is unknown. The test now appends every answer immediately and resumes after a crash.
- The phone disconnected from adb right before the planned full run (GPU backend), so the run did not start.

## Reading (agent)
1. As configured, this model is **not usable for an interactive chat** on the moto g72: 11 s at best, 40 to 57 s on the CPU, and it takes 1.2 to 2.2 GB of the free memory.
2. Speed is dominated by the model's computation, not by the length of the instructions (the short version was not faster).
3. The next information worth having is accuracy on the 100 phrases (resumable run on the phone GPU) and a smaller model (FunctionGemma 270M, about half the size) before concluding. If accuracy is poor as well, the rule parser with a better phrase coverage and a clarification step is the sensible option A for the product.

## Pending
Reconnect the phone; run all 100 phrases on the GPU backend with the new resumable test, score with `ModelScoreTest`; then FunctionGemma if the Gemma terms are accepted (its files are gated).
