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

## If Usage tab is empty

1. Make sure you **unplugged** (usage only starts on unplug)
2. Open the app once on battery (recovers missed unplug events)
3. Settings → BattBuddy → Battery → **Unrestricted**
4. Do not force-stop the app

## Build & install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
