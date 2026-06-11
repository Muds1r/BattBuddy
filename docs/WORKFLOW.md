# BattBuddy — App Workflow

## Two features, two tabs

### Charging tab
Tracks each time the phone is plugged in.

| Event | What gets saved |
|---|---|
| **Plug in** | time + battery % |
| **Unplug** | time + battery % |
| **Calculated** | duration, total % gained, avg %/hr |

Open the app while charging to see live stats before you unplug.

### Usage tab
Tracks each time the phone is on battery.

| Event | What gets saved |
|---|---|
| **Unplug** | time + battery % |
| **Plug in** | time + battery % |
| **Calculated** | duration, total % dropped, avg %/hr |

Open the app anytime while on battery to see live drain since last unplug.

## The cycle

```
Plug in ──► [Charging tab: live + log] ──► Unplug ──► [Usage tab: live + log] ──► Plug in ──► ...
```

## Example

1. Unplug at **5:00 PM** at **80%** → usage session starts
2. Open app at **4:00 AM** at **62%** → shows **18% drop**, duration, avg %/hr (live)
3. Plug in at **7:00 AM** at **60%** → usage session saved to logs; charging session starts
4. Unplug at **9:00 AM** at **85%** → charging session saved (+25%, 2h); new usage session starts

## Build & install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Phone settings

- Open app once after install
- Battery → **Unrestricted**
- Don't force-stop the app

## Background

The app does not need to stay open. Plug/unplug listeners run in the background. Live % updates only appear when you open the app.
