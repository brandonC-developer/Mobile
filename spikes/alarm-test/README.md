# alarm-test — THROWAWAY spike for OI-039 / ADR-0003

Authorized by the Owner on 2026-11-01 only to run the device tests listed in ADR-0003. It is **not** product code and traces to no requirement. Keeping any of it is a new request and goes back through the Owner. Delete the folder when the results are recorded.

**Status:** written but never compiled or run. The versions in the Gradle files (AGP 8.5.2, Kotlin 1.9.24, Gradle 8.7, compileSdk 34) are the agent's choice and may need adjusting when Android Studio syncs.

## Set up
1. Install Android Studio (it brings the SDK and `adb`).
2. On the phone: Settings > About phone > tap Build number 7 times; then Developer options > USB debugging on. Connect by cable and accept the prompt.
3. Open this folder (`spikes/alarm-test`) in Android Studio, let Gradle sync, and run the `app` on the phone.

The app targets API 33 on purpose, as in the Android documentation for `SCHEDULE_EXACT_ALARM`. It has no icon and no design: a status block, buttons and a log (newest first). The log is kept on the phone and survives restarts.

## The seven tests
Write down each result (or copy the log) and give it to the agent to record in OI-039 and ADR-0003.

| # | Test | Steps | Record |
|---|---|---|---|
| 1 | Fresh install state | Install, open. Read the status block before pressing anything | `Notifications enabled`, `canScheduleExactAlarms` |
| 2 | Delay with screen off | Grant both permissions. Press "Schedule at +1, +2, +5, +10 min". Turn the screen off and leave the phone still | `FIRED` lines: the delay of each. Repeat later with battery saver on |
| 3 | Forced stop | Schedule, then Settings > Apps > Alarm test > Force stop. Wait past the time. Do not open the app | Whether `FIRED` appears (open the app afterwards to read the log) |
| 4 | Reboot | Schedule at +10 min, reboot, do not open the app | `SYSTEM EVENT: ...BOOT_COMPLETED`, `RESCHEDULE`, then `FIRED` |
| 5 | Permission revoked and granted | Schedule, then turn the exact-alarm permission off and on in "Alarms & reminders" | `SYSTEM EVENT: ...PERMISSION_STATE_CHANGED`, `RESCHEDULE`, whether alarms still fire |
| 6 | Alarm limit | "Limit test" with 50, 100, 200, 500, 600 (press Cancel all between runs) | The `LIMIT TEST` line: how many were scheduled and the error text |
| 7 | Permissions denied | Fresh install or revoke each permission; schedule | Whether scheduling still works, `notifications DISABLED` in `FIRED`, errors |

Optional, with `adb`: `adb shell dumpsys alarm | findstr alarmtest` lists the alarms the system holds for the app.
