---
id: DOC-TFA-001A
title: Draft catalog of assistant requests (chat first, voice later)
status: Draft (Owner decisions recorded)
version: 0.1
date: 2026-11-02
approved_by:
refs: [DOC-TFA-001, OI-049, UC-004, UC-005, UC-007, UC-008, UC-009, UC-010, NFR-002, NFR-005]
---

# Draft catalog of assistant requests

> Proposal by the agent for the Owner to correct. Nothing here is approved. Phrases are examples to be replaced by the Owner's own words; the measured test set (60 to 100 phrases) is built from them.

## Design rules (best practices, my proposal)
1. **Read first, write later.** Questions (reads) run at once. Anything that changes data is shown in plain words ("Mover el cobro de María del día 15 al día 20") and needs a tap on Confirmar. This reuses the confirmations the app already has.
2. **The model only translates.** Phrase in, structured request out (intent plus slots). The app finds the tenant, computes dates and totals with the existing code, and writes through the existing repository. The model never sees or edits the database.
3. **Ask when unsure.** Ambiguous name ("¿Cuál María: Apto 2 o Apto 5?") or a missing date produces a question with buttons, never a guess.
4. **Destructive actions need an extra step.** End or delete (UC-009, UC-010, UC-011) always require a typed or tapped confirmation, also by voice. No delete by voice alone.
5. **Say what it cannot do.** Unsupported phrase: a short Spanish message with examples of what works.
6. **Chat first.** A text box is silent, testable on the emulator and needs no microphone. Voice is the same box fed by speech recognition, so it is a later layer, not a second product.
7. **Voice caution (to verify, not measured).** My recollection is that the common Android speech recognizer may send audio to a Google service; that would conflict with NFR-002. Android also offers an on-device recognizer from API 31, whose Spanish quality and offline pack availability on the moto g72 are unknown. Treat voice as a separate assessment.

## Candidate requests
| # | Type | Example phrases (Spanish) | Intent | Existing use case | Risk |
|---|------|---------------------------|--------|-------------------|------|
| R1 | Read | "¿Qué cobro esta semana?" · "¿Quién paga el viernes?" | List collections for a range | UC-004 | Low |
| R2 | Read | "¿Cuánto cobro este mes?" · "¿Cuánto falta por cobrar?" | Total for a range | UC-004, UC-005 | Low |
| R3 | Read | "¿Qué cobro el 15?" · "Cobros de la próxima semana" | Collections for a date | UC-005 | Low |
| R4 | Read | "¿Cuánto paga María?" · "¿Cuándo paga Luis?" | Rental details | UC-008 | Low |
| R5 | Open | "Llama a María" · "Escríbele a Luis por WhatsApp" | Open dialer or WhatsApp (the landlord presses send) | UC-007 | Low |
| R6 | Write | "Marca como pagado a María" | Mark a payment as collected, if the app has that state | not in the approved use cases | Medium, needs a requirement |
| R7 | Write | "Cambia el día de pago de María al 20" · "Mueve el cobro de Luis al 5" | Edit payment day | UC-008 | Medium |
| R8 | Write | "Sube el alquiler de Luis a 250 000" | Edit amount | UC-008 | Medium |
| R9 | Write, destructive | "Termina el alquiler de María" · "Quita a Luis" | End a rental (history kept) | UC-009 | High, extra confirmation |
| R10 | Write, destructive | "Borra el alquiler de María" | Delete a rental | UC-010 | High, extra confirmation |

"Cuál quita" in the Owner's first message is read here as R9 (end) or R10 (delete); the Owner must say which one is meant, and whether "quitar" should end (keeps history) or delete.

## Suggested stages
- **Stage 1 (lowest risk):** R1 to R5 only. All are reads or hand-offs, no data changes, so a wrong parse is harmless.
- **Stage 2:** R7 and R8 with confirmation, after the measured accuracy passes.
- **Stage 3:** R9 and R10 with the extra confirmation, and R6 only if the Owner wants a paid state (a new requirement).

## Questions for the Owner
1. Keep, remove or reword each of R1 to R10; add the 3 phrases you would say most.
2. Is there a "paid / collected" mark today? (The approved requirements do not define one.) Do you want it?
3. For "quita": end the rental, or delete it?
4. Chat first and voice as a later study: agreed?

## Owner decisions (2026-11-02)
- R1 to R10 accepted as the candidate scope, in the three stages above.
- R6 (a paid mark) is wanted; it is a new requirement, logged as OI-050.
- "Quita" means **end the rental** (R9, history kept). R10 (delete) stays a separate, explicit phrase.
- Chat first; voice is a later, separate assessment.
