# BattBuddy — App Workflow

## Separate logs (important)

| Tab | Table | Starts when | Ends when | Saved to history when |
|---|---|---|---|---|
| **Charging** | `charge_sessions` | Plug in | Unplug | Unplug |
| **Usage** | `discharge_sessions` | Unplug | Plug in | Plug in |

These are **two different logs**. Charging data never appears in Usage and vice versa.

## Charging tab

1. **Plug in** → new charging session (time + %)
2. **While charging** → open app for live stats
3. **Unplug** → session saved to Charging **Logs** (duration, % gained, %/hr)

## Usage tab

1. **Unplug** → new usage session (time + %)
2. **While on battery** → open app for live drain stats
3. **Plug in** → session saved to Usage **Logs** (duration, % dropped, %/hr)

## Full cycle

```
Plug in  →  [Charging: live]  →  Unplug  →  [Charging: log ✓]
                                      ↓
                               [Usage: live]  →  Plug in  →  [Usage: log ✓]
                                      ↓
                               (repeat)
```

## Background tracking

BattBuddy detects plug/unplug automatically via system broadcasts and background checks every ~15 minutes (more often while a session is active). You do **not** need to open the app after unplugging.

## If logs look wrong or missing

1. Settings → BattBuddy → Battery → **Unrestricted**
2. Do not force-stop the app
3. Open the app once after install

## Build & install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
