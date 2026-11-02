---
id: DOC-UXF
title: UX and Interface Framework
status: Draft
version: 0.1
date: 2026-11-02
approved_by:
refs: [DOC-FR, DOC-NFR, DOC-UC, DOC-CMP]
---

# UX and Interface Framework

> **Status:** Draft proposed by the agent. Nothing here is a requirement. It is a design guide that turns recognized usability and behavior principles into concrete rules for this app. Anything that would change behavior stated in the FR is marked **Proposal** and needs an Owner decision. The visual mockups are in the roadmap page (an Artifact), not in this repository.

## Sources and their reliability
| Source | Used for | Checked |
|--------|----------|---------|
| [Nielsen, "10 Usability Heuristics for User Interface Design"](https://www.nngroup.com/articles/ten-usability-heuristics/) (published 1994, last reviewed 2024-01-30) | Review checklist | Primary page read 2026-11-02 |
| [Laws of UX](https://lawsofux.com/) | Hick, Fitts, Miller, Jakob, Peak-End, Doherty, Tesler, Postel, Zeigarnik | Definitions read on the site 2026-11-02; the site is a practitioner summary, not original research |
| [Fogg Behavior Model, Stanford Behavior Design Lab](https://behaviordesign.stanford.edu/resources/fogg-behavior-model) | Reminders as prompts | Primary page read; it names three elements (motivation, ability, prompt) and says they must converge at the same moment |
| [Google, touch target size](https://support.google.com/accessibility/android/answer/7101858) | Sizes | Primary page read: at least 48 by 48 dp, about 9 mm, separated by 8 dp or more |
| Material Design 3 (Android's design system) | Components, navigation, type | **Not verified from the primary site**; a search returned only secondary guides. Verify at m3.material.io before relying on specifics such as navigation bar item counts |
| Don Norman, *The Design of Everyday Things* (affordances, signifiers, feedback) | Signifiers and feedback | **Not verified**: only secondary summaries were found; the book was not read |

Limits: these are design heuristics and models, not proofs. They support decisions; they do not replace testing with the Owner on the phone.

## Who the interface is for
One landlord, one phone, a few properties, opening the app mostly to answer "what do I collect this week?" and to be reminded. Core tasks are short and repeated; setup tasks (properties, tenants, rentals) are rare. This favors a home screen that answers the weekly question at once and keeps setup out of the way.

## Principles applied to this app
| Principle | Rule for this app | Where |
|-----------|-------------------|-------|
| **Visibility of system status** (Nielsen 1) | The weekly summary names the week shown; a visible banner when a permission is denied; after saving a rental, show its next collection date and which reminders will fire | FR-016 · FR-024 · UC-003 |
| **Match with the real world** (Nielsen 2) | Landlord's words: "alquiler", "inquilino", "cobro", "día de pago"; amounts as ₡ with thousands separators; dates as "lunes 3 de noviembre" | NFR-007 · NFR-008 |
| **User control and freedom** (Nielsen 3) | Every destructive action has a cancel and a plain consequence. **Proposal:** a short "Deshacer" after deleting a single rental; FR-012 asks for confirmation instead, so this needs a decision | FR-006 · FR-012 · FR-028 |
| **Consistency and standards, Jakob's Law** (Nielsen 4) | Follow Android conventions: bottom navigation, floating action button, system share menu and file picker, back gesture | INT-006 · INT-007 |
| **Error prevention** (Nielsen 5) | Show occupied properties and busy tenants as unavailable in the pickers so the error of FR-008 rarely appears; numeric keyboard for amount and day; payment day limited to 1 to 31 | AC-FR-008-1 · AC-FR-007-2 |
| **Recognition over recall** (Nielsen 6) | Every collection shows property, tenant, date and amount together; no screen asks the landlord to remember another screen's data | FR-015 · FR-020 |
| **Aesthetic and minimalist design** (Nielsen 8) | Home shows only the week and its total; setup, settings and backup live elsewhere. No statuses, charts or counters the requirements forbid | FR-014 · FR-015 |
| **Error recovery** (Nielsen 9) | Messages say what is wrong and how to fix it, in Spanish, without codes: a rejected backup says why and that nothing changed | FR-029 |
| **Hick's Law** | Few choices per screen: four bottom destinations; four reminder switches; one primary action per screen | UC-012 |
| **Fitts's Law** | Primary action reachable by thumb at the bottom; targets at least 48 by 48 dp with 8 dp between them | Google guidance above |
| **Miller's Law** | Group the week by day; keep forms to the fields the requirements list (no more than five) | FR-007 |
| **Peak-End Rule** | The peak is the reminder: make it clear and useful at a glance. The end of a task is its confirmation: say what happened and what comes next, in one line | FR-020 · UC-003 |
| **Doherty Threshold** | Everything is local, so screens should respond in well under 400 ms; no spinners expected except on import and export of large files | NFR-001 |
| **Tesler's Law** | The app absorbs the complexity: the landlord types "día 31" and the app handles February | FR-013 |
| **Postel's Law** | Be liberal with phone numbers as typed (spaces, dashes, +506 or not) and strict in what the app stores and sends | OI-043 |
| **Fogg Behavior Model** | A reminder is a *prompt*. It works when motivation (rent is money) and ability (one tap to the week, one tap to WhatsApp) already exist. So reduce effort first: the notification opens the current week; contact is one tap. No tricks to raise motivation | UC-006 · UC-007 |

### Principles deliberately not used
- **Zeigarnik effect and Goal-Gradient Effect** (progress bars, streaks, unfinished-task nudges): the requirements forbid paid or overdue statuses (FR-014, FR-015), so there is no task to complete and nothing to gamify. Using them would also pull toward manipulative engagement, which Fogg's work warns against when not transparent.
- **Choice overload and notification volume:** at most four reminders per collection, all chosen by the landlord; no extra prompts to "re-engage".

## Interface rules
1. **Navigation.** Bottom bar with four destinations (a proposal; verify the item-count guidance in Material 3): *Semana* (home), *Calendario*, *Alquileres* (rentals, with tenants and properties reached from it), *Ajustes* (reminders and backup). Home is always *Semana*.
2. **Primary action.** One floating action button per list screen: new rental on *Alquileres*, new tenant or property inside their lists.
3. **Text.** Sizes in sp so they follow the phone setting; every screen checked at the largest setting (NFR-009). Body text no smaller than the platform default.
4. **Touch.** 48 by 48 dp minimum, 8 dp apart. Destructive actions are never next to a frequent action.
5. **Color.** Do not rely on color alone for meaning. Color is not used to show payment state, since there is none. Support the phone's light and dark theme (proposal; not required by any NFR).
6. **Language.** All text Spanish (NFR-007), plain and short. Buttons say what they do: "Guardar alquiler", "Eliminar inquilino y 3 alquileres".
7. **Empty states.** Each list says what it will hold and offers the first action. An empty week says so (AC-FR-015-3).
8. **Confirmations.** Reserve them for deletion and import. Do not confirm routine saves; confirm them with a line of feedback instead.
9. **Reminder text.** Property first, then tenant, amount and date, in one glance. Example: "Apto 3 · María Rojas · ₡250.000 · vence el lunes 3".

## Screens (summarized; mockups in the roadmap page)
| Screen | Use cases | Notes |
|--------|-----------|-------|
| Semana | UC-004 | Week header with previous and next, week total, entries grouped by day |
| Calendario | UC-005 | Month grid from today; select a date to list its collections |
| Alquileres | UC-003 · UC-008 · UC-009 · UC-010 · UC-007 | List of active rentals first, ended ones below; actions on the detail |
| Formulario de alquiler | UC-003 · UC-008 | Tenant, property, amount, payment day, optional end date |
| Inquilinos y propiedades | UC-001 · UC-002 · UC-011 | Simple lists with add and edit; no delete for properties |
| Ajustes | UC-012 · UC-013 · UC-014 | Reminder switches and time; export and import with their warnings |
| Notificación | UC-006 | Content defined above |

## How this will be checked
Use the Nielsen list as a review checklist on each screen before the test cases of [test-plan](../06-quality/test-plan.md) are run, and test with the Owner on the phone at the largest font size. The proposals marked above are logged in OI-044.
