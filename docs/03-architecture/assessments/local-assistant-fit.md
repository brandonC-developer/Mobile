---
id: DOC-TFA-001
title: Technology fit assessment, local assistant with a very small language model
status: Draft
version: 0.1
date: 2026-11-02
approved_by:
refs: [NFR-001, NFR-002, NFR-005, NFR-006, NFR-007, ADR-0001, ADR-0003, ADR-0005, OI-049, RSK-007, SEC-007]
---

# Technology fit assessment: local assistant with a very small language model

> **Status:** Draft by the agent, written with the `tech-fit-assessment` skill (`.claude/skills/tech-fit-assessment/`). Evidence grades: **measured** (run on the project or the phone), **documented** (primary source, linked), **reported** (secondary source), **assumed/unknown**. Nothing here is approved and nothing was built.

## 1. Question and use cases
The Owner asked whether a very small language model can run on the phone, offline, to understand short Spanish requests such as: which collections fall this week, change a date, remove or end a rental. Assumed use cases, to be confirmed (OI-049):
- UC-A (read): "which payments are this week", "what do I collect on the 15th".
- UC-B (change): "move María's payment day to the 20th".
- UC-C (remove): "end or delete the rental of Luis". The Owner's words "cuál quita" are ambiguous and need a clarification.

## 2. Verdict
**Unknown, measure first.** It is technically possible and costs nothing in licences, and nothing found conflicts with a hard rule if the model file is loaded by the landlord instead of downloaded. The three facts that decide it are unmeasured: Spanish accuracy of the 270M to 600M models, speed and memory on the Helio G99, and the size added to the app.

## 3. Fit against the project
| Rule (id) | Result | Evidence |
|-----------|--------|----------|
| NFR-001 works offline | OK | Local inference needs no connection (documented, LiteRT-LM and llama.cpp run on the device). |
| NFR-002 data stays on the phone; SEC-007 no Internet permission | **Needs change in how the model arrives** | The app cannot download a model (no Internet permission). The model file must be bundled in the APK or loaded from a file picked by the landlord, like a backup. Cloud inference conflicts outright. |
| NFR-005 and data integrity | OK if designed so | The model must never write data. It only turns a phrase into a structured request; the app validates it, asks the existing confirmations and runs the existing repository operations. |
| NFR-006 Android 13+, APK, no store account | OK | llama.cpp builds for `android-28` and `arm64-v8a`; LiteRT-LM has a stable Kotlin API (documented). The Kotlin guide states no minimum API level (documented: not specified), so compatibility with minSdk 33 is to be confirmed by building. |
| NFR-007 all text in Spanish | **Unknown, main risk** | FunctionGemma card lists no languages and says its safety evaluation used English prompts only; Gemma 3 claims 140+ languages without a per-size benchmark; Qwen3-0.6B claims 100+ languages without naming Spanish. |
| NFR-004 and ADR-0003 reminders | OK | Assistant runs only while the app is open; no background load. Must be re-tested after integration. |
| ADR-0001 native Kotlin | Needs a decision | LiteRT-LM Kotlin API fits. llama.cpp adds C++ through JNI and the NDK, which the project does not use today. |
| ADR-0005 Compose | OK | A chat or command box is an ordinary Compose screen. |
| Approved requirements | **Needs new requirements** | None covers an assistant. It needs requirements, a use case set and an ADR before any build (AGENTS.md). |

## 4. Price
| Item | Cost | Grade | Source and date |
|------|------|-------|-----------------|
| LiteRT-LM runtime | Free, Apache-2.0 | documented | [repository](https://github.com/google-ai-edge/LiteRT-LM), read 2026-11-02 |
| llama.cpp runtime | Free, MIT | documented | [repository](https://github.com/ggml-org/llama.cpp), 2026-11-02 |
| Qwen3-0.6B weights | Free, Apache-2.0 | documented | [model card](https://huggingface.co/Qwen/Qwen3-0.6B), 2026-11-02 |
| Gemma 3 270M and FunctionGemma weights | No fee in the terms; bundling counts as distribution and brings conditions: pass the terms and a copy to recipients, mark modified files, ship a notice file, respect the Prohibited Use Policy | documented | [Gemma Terms](https://ai.google.dev/gemma/terms), 2026-11-02. Not legal advice; Gemma 4 has a separate licence. |
| Fine-tuning for Spanish commands | Free on a no-cost Colab T4 according to Google's example, availability not guaranteed; about 1 to 3 days of the agent and Owner time (assumption) | reported / assumed | [Google blog](https://developers.googleblog.com/own-your-ai-fine-tune-gemma-3-270m-for-on-device/) |
| New hardware | None if the phone is enough (unknown). Gemini Nano needs a different phone. | assumed | See section 7 |
| Cloud alternative, for comparison only (conflicts with NFR-002) | `gemini-2.5-flash-lite` $0.10 per 1M input and $0.40 per 1M output tokens; `gemini-3.1-flash-lite` $0.25 and $1.50; a free tier exists | documented | [Google pricing](https://aistudio.google.com/docs/pricing), 2026-11-02 |
Estimate for the cloud comparison, assumptions mine: 600 requests per month, 500 input and 100 output tokens each, which is 0.30M input and 0.06M output tokens: about **$0.05 per month** with the cheaper model and **$0.17** with the other. Price is therefore not the obstacle for any option; the obstacle is the rule that nothing leaves the phone.

## 5. Software load
| Item | Finding | Grade |
|------|---------|-------|
| Model file | Qwen3-0.6B official GGUF: only one file, Q8_0, **639 MB** (measured from the Hugging Face listing, 2026-11-02). Gemma 3 270M about 240 to 320 MB quantized (reported by Google material). FunctionGemma and Gemma LiteRT files sit in gated repositories (HTTP 401 without accepting the Gemma terms), so their size is not verified | measured / reported / unknown |
| Added size of the runtime libraries | Unknown, to measure (the release file is about 18 MB today) | unknown |
| Runtime memory | Google aims at "under 300 MB" for the 270M model after quantization; other models unknown | documented / unknown |
| Native code and build | LiteRT-LM: Gradle artifact `com.google.ai.edge.litertlm:litertlm-android` (the guide uses `latest.release`, to be pinned), loads a `.litertlm` model from a file path, so sideloading fits NFR-002; the first load can take up to 10 seconds (documented); the GPU backend needs two `uses-native-library` manifest entries, `libOpenCL.so` and `libvndksupport.so`, with `required="false"`, which are not permissions. llama.cpp: the latest release has no prebuilt Android files (its only asset is a text file) and the NDK and CMake are **not installed** on this computer (measured), so using it means installing them and building | documented / measured |
| Permissions | None added if the model is a local file | assumed |
| Licences to carry | Apache-2.0 and MIT notices, plus the Gemma notice file if Gemma is used | documented |
| Test suites | New accuracy tests (a Spanish phrase set) plus device tests of memory and speed | assumed |

## 6. Processing power and optimization
Owner phone: Motorola moto g72, MediaTek Helio G99 with 2 Cortex-A76 and 6 Cortex-A55 cores, Mali-G57 MC2, 6 or 8 GB RAM, Android 13 ([GSMArena](https://m.gsmarena.com/motorola_moto_g72-ampp-11910.php), reported; probe pending, see section 10).
- Speed in tokens per second on this phone: **unknown**. No source measured this chipset; do not reuse numbers from other phones.
- Neural unit: LiteRT-LM lists NPU support for Qualcomm and MediaTek chipsets as early access; whether the G99 qualifies is unknown, assume CPU only.
- llama.cpp acceleration listed on Android goes up to `SME2` for Arm CPUs, which this chipset does not have; plain CPU kernels apply.
- Battery: one request is a short burst; a figure from a web article (Pixel 9 Pro, 25 conversations for 0.75% of the battery) could not be confirmed on a Google primary page, so it is **reported, not verified**, and it is another phone.
- Usage while the app is open only; no background work, so Doze and reminders are untouched.

## 7. Options compared
| Option | Fit | Price | Load | Main risk |
|--------|-----|-------|------|-----------|
| A. Spanish rule-based parser, no AI | Compatible; Spanish fully under control | Free; development effort | Kilobytes | Rigid phrasing; grows with every new phrase |
| B1. Qwen3-0.6B or Gemma 3 1B through LiteRT-LM (Kotlin) | Compatible with changes (new requirements, ADR, model file by picker) | Free licences | Hundreds of MB model, unknown runtime | Spanish accuracy and speed unknown |
| B2. FunctionGemma 270M fine-tuned for Spanish through LiteRT-LM or llama.cpp | Compatible with changes; fine-tuning step needed | Free; fine-tuning effort | About 0.3 GB | Not a chat model; base scores moderate; Spanish data to build |
| C. Gemini Nano through AICore and the ML Kit Prompt API | **Conflicts** for this phone | Free API | None in the app | Supported devices are a short list of recent phones (Pixel 10, some Xiaomi 15 and Samsung S26 models); the moto g72 is not listed; the device lists differ between page snapshots |
| D. Cloud model | **Conflicts** with NFR-002 and SEC-007 | About $0.05 to $0.17 per month | None | Tenant names and phones would leave the phone; needs the Internet permission |

## 8. Advantages and disadvantages (recommended path: A first, then B measured)
**Advantages**
- Free licences and no per-use cost; works offline and keeps tenant data on the phone.
- Natural phrasing and, later, voice, without a keyword list.
- Mature options exist (LiteRT-LM with a stable Kotlin API, llama.cpp with an Android example, SmolChat as an Apache-2.0 reference).
- The design where the app computes the answers and the model only parses keeps amounts and dates exact.

**Disadvantages**
- Spanish quality of 0.27B to 0.6B models is unproven; wrong parsing is likely at the edges.
- FunctionGemma's own relevance and irrelevance scores (61.1 and 73.7 on its benchmark) mean it can call the wrong function or call one when it should not; confirmations are mandatory.
- Adds hundreds of MB, native code or a new runtime, memory pressure on a 6 GB phone, and a licence notice.
- Needs new requirements, an ADR and tests; fine-tuning for Spanish is real work.
- Runtime libraries move fast (LiteRT-LM v0.19.0 listed a breaking C API change), so upkeep is higher than the current stack.
- The model cannot be downloaded by the app; the landlord must load a file once.

## 9. Risks
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Poor Spanish accuracy makes the assistant frustrating | Medium | Medium | Measure first (section 10); keep rule-based fallback |
| A wrong change is applied | Medium | High | Never write without the existing confirmation dialogs; show what will change in words; allow undo (OI-044 b was not adopted, revisit) |
| Slow answers on the Helio G99 | Medium | Medium | Measure; prefer the smallest model that passes |
| App size and RAM grow too much | Medium | Medium | Load the model as a file; unload after use |
| Licence obligations missed (Gemma) | Low | Medium | Ship the notice file; prefer Apache-2.0 models if equal |
| Runtime API churn | Medium | Low | Pin versions; isolate behind one interface |

## 10. Test plan against the project
| Test | Measures | Device | Pass criterion (proposal) | Status |
|------|----------|--------|---------------------------|--------|
| Device probe `device_probe.sh` | Chipset, cores, RAM, free storage | moto g72 | Free RAM above the model plus 1 GB | Pending: the phone was disconnected; the emulator could not answer |
| Spanish phrase set | Intent and slot accuracy | none (offline set) | At least 90% intents and 85% slots for the chosen option | Pending: needs the Owner's 6 to 10 real phrases to build 60 to 100 |
| Option A baseline | Accuracy and cost of rules | JVM | Reaches the same targets without a model, or shows its limits | Pending: needs the Owner phrases |
| Models B1 and B2 on phone | Tokens per second, peak RAM, time to answer, battery per 20 requests | moto g72 | Answer in under 3 s on the phone; no out-of-memory; battery under 1% per 20 requests (all proposals) | Pending |
| Size added | Release file and storage | build machine | Under 60 MB without the model; model loaded by file | Pending |
| Regression | Existing 74 unit and 60 device tests, TC-026 manifest (no Internet) | emulator and phone | All pass | Pending |
Throwaway spike code needs the Owner's authorization and is deleted afterwards unless the Owner asks to keep it.

## 11. Verified, unverified, sources
**Verified (documented):** FunctionGemma is a 270M function-calling model, not a chat model, 32K context, tagged call format, accuracy 58% to 85% after fine-tuning on Mobile Actions ([card](https://huggingface.co/google/functiongemma-270m-it), [docs](https://ai.google.dev/gemma/docs/functiongemma)); LiteRT-LM supports Android with a stable Kotlin API and lists FunctionGemma, Gemma3-1B, Qwen2.5 0.5B and 1.5B, Qwen3-0.6B ([overview](https://developers.google.com/edge/litert-lm/overview)); Gemma 3 270M and 1B have 32K context and the family claims 140+ languages ([model card](https://ai.google.dev/gemma/docs/core/model_card_3)); Gemini Nano access goes through AICore and ML Kit ([Android](https://developer.android.com/ai/gemini-nano), [ML Kit](https://developers.google.com/ml-kit/genai)); llama.cpp Android build notes ([guide](https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md)); SmolChat is Apache-2.0 and embeds llama.cpp through JNI ([repository](https://github.com/shubham0204/SmolChat-Android)); licences and prices as in section 4.
**Not verified:** Spanish accuracy of every candidate; speed, memory and battery on the moto g72; added app size of the runtime; RAM of this phone variant; compatibility of the LiteRT-LM Kotlin library with minSdk 33, Kotlin 1.9.24 and AGP 8.5.2 (the guide states no minimums); sizes of the FunctionGemma and Gemma files; the battery figure for Gemma 3 270M; whether the Helio G99 neural unit is usable.

## 12. Recommendation and decisions needed
Practical note from the 2026-11-02 checks: LiteRT-LM is the cheaper first try for a spike because it needs no NDK, while llama.cpp needs the NDK and CMake installed first. Recommendation (not an approval): do not start with a model. Build or prototype the rule-based baseline for the few requests that matter, and measure the two small models on the phone with a Spanish phrase set; decide with an ADR after the numbers. Keep the model away from the data and behind the existing confirmations.
Decisions needed from the Owner: (1) which 6 to 10 requests the assistant must understand, including what "cuál quita" means; (2) whether voice input is wanted; (3) authorization for a throwaway spike on the phone; (4) reconnecting the phone so the probe and the speed tests can run.
