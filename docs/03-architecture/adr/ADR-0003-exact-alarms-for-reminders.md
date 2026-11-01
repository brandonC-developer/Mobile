# ADR-0003 — Schedule reminders with exact alarms and rebuild them on every relevant event
- **Status:** Proposed
- **Date:** 2026-11-01
- **Decided by:** Owner
- **Approval evidence:** none yet. Only the Owner approves, with an `approve:` commit.
- **Verification pending:** device behavior (OI-039) is not tested; this decision is provisional until the prototype in "Verification plan" is run.

## Context
- Reminders go only to the landlord, are local, and must arrive at the chosen time with at most 5 minutes of delay (NFR-004, OI-017, OI-028). They work offline (NFR-001).
- The accepted scheme is a rolling window: the nearest reminders are scheduled up to a limit, and rescheduled on every app open and every rental change; a last reminder asks to open the app (FR-021, FR-022, OI-014).
- The app asks for the notification permission and the exact-alarm permission the first time a rental is created, and warns if either is denied (FR-023, FR-024).
- ADR-0001 proposes a native Kotlin app on Android 13 or later; ADR-0002 proposes Room as the store.

Facts from the Android documentation (see sources):
- The page on alarms says to use exact alarms only when a user-facing function needs precise timing, and to avoid them if possible. `setExactAndAllowWhileIdle()` fires "at a nearly precise time ... even if battery-saving measures are in effect". `setExact()` fires nearly on time only if other battery-saving measures are not in effect. `setAlarmClock()` is the most reliable but can affect battery.
- Alarms do not fire in Doze mode unless they are the allow-while-idle kind; they are deferred until Doze ends.
- By default all alarms are canceled when the device shuts down. To restore them, declare `RECEIVE_BOOT_COMPLETED` and reschedule in a receiver of `ACTION_BOOT_COMPLETED`; this works only if the user launched the app at least once.
- `SCHEDULE_EXACT_ALARM` is granted by the user in "Alarms & reminders". If it is revoked, the app stops and all its future exact alarms are canceled. The app should call `canScheduleExactAlarms()` before scheduling, send the user to `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`, listen to `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, and reschedule when it is granted again.
- On Android 14 and later, a new install that targets Android 13 or higher does not get `SCHEDULE_EXACT_ALARM` by default, and it is also denied after a backup and restore to such a device. Without the permission, the exact-alarm calls throw `SecurityException`. The page recommends degrading to inexact alarms if denied.
- `USE_EXACT_ALARM` is granted automatically and cannot be revoked, but is limited to alarm clock, timer and calendar-type uses and is subject to a Play policy.
- WorkManager periodic work has a flexible interval of at least 15 minutes, so it cannot meet a 5-minute tolerance.
- Android 13 makes `POST_NOTIFICATIONS` a runtime permission, off by default (cited in OI-039).
- Expo's documentation (a different stack, consulted for comparison) notes that some manufacturer builds defer even allow-while-idle alarms by minutes.
- Not found in the documentation consulted: the maximum number of alarms per app, and the effect of a force stop. Not documented for Android 13 devices: the default state of `SCHEDULE_EXACT_ALARM` on a fresh install.

## Decision
1. Schedule each reminder with `AlarmManager.setExactAndAllowWhileIdle()`, with a broadcast receiver that posts the notification from a notification channel.
2. Declare `SCHEDULE_EXACT_ALARM`. Before scheduling, call `canScheduleExactAlarms()`. If it is false, fall back to inexact allow-while-idle alarms, show the warning of FR-024, and keep working.
3. Rebuild the window of reminders (FR-021) from the database on: app open, rental created, edited, ended or deleted, reminder offsets or time changed, `ACTION_BOOT_COMPLETED`, and `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`. Rebuilding cancels the previous alarms and creates the new ones, so no stale reminder remains (AC-FR-021-2).
4. Compute the dates of reminders from the rental rule at rebuild time (FR-013); nothing is stored in advance.
5. The size of the window is a configuration value, set after the device test; a value is not chosen here.

## Alternatives considered
- **`USE_EXACT_ALARM`.** Discarded for now: intended for alarm, timer and calendar apps; a collection reminder app does not clearly fit. Since the app is installed by APK and not from Play, the store policy does not apply, but the permission's intended use does. Could be revisited if the test shows the user-granted permission is a problem.
- **`setAlarmClock()`.** Not chosen: most reliable, but it is meant for alarm clocks and affects battery. Keep as a fallback if the prototype shows allow-while-idle alarms arrive late.
- **WorkManager.** Discarded: 15-minute minimum flex interval exceeds the 5-minute tolerance.
- **Inexact alarms only.** Discarded: cannot guarantee NFR-004.
- **One alarm chained to the next.** Considered: a single pending alarm that, when it fires, schedules the next one, which avoids any alarm limit. Discarded as the base because one missed or killed alarm would silence all later reminders, contradicting the accepted rolling window (OI-014).
- **Push notifications from a server.** Discarded: needs a backend (OI-028, OI-021).

## Consequences
- Positive: meets NFR-004 if the permission is granted and the device delivers on time; works offline; self-healing, since every event rebuilds the schedule.
- Negative: the user must grant a special permission; revoking it cancels all exact alarms, so the app must rebuild after the user grants it again; a restore on Android 14 or later would need the permission requested again.
- Risk: manufacturer battery management may delay alarms beyond 5 minutes (not verified for the Owner phone).

## Verification plan (OI-039)
Prototype on the Owner Android 13 phone, to be run before this ADR is approved:
1. Fresh install: is `canScheduleExactAlarms()` true or false, and does the notification permission prompt appear?
2. Schedule reminders for several times with the phone idle and screen off; measure the delay of each against 5 minutes.
3. Force stop the app from settings; do scheduled reminders still arrive?
4. Reboot the phone without opening the app; do reminders arrive after the boot receiver reschedules them?
5. Revoke and grant the exact-alarm permission; are reminders rebuilt?
6. Schedule increasing numbers of alarms to find any practical limit.
7. Deny each permission; is the warning shown and the rest of the app usable?

## Refs
FR-013 · FR-018 · FR-019 · FR-021 · FR-022 · FR-023 · FR-024 · NFR-001 · NFR-004 · ADR-0001 · ADR-0002 · OI-014 · OI-017 · OI-039
Sources: [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms) · [Schedule exact alarms are denied by default (Android 14)](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms) · [Notification runtime permission (Android 13)](https://developer.android.com/about/versions/13/changes/notification-permission) · [Expo Notifications](https://docs.expo.dev/versions/latest/sdk/notifications/)
