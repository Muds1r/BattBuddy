# BattBuddy

Personal Android app for Pixel — logs charging and battery usage sessions.

**Made by Muds1r**

## Features

- **Charging tab:** plug %, unplug %, duration, total % gained, avg %/hr
- **Usage tab:** unplug %, plug %, duration, total % dropped, avg %/hr
- Live stats when you open the app mid-session
- Works in the background — no need to keep the app open

See [docs/WORKFLOW.md](docs/WORKFLOW.md) for the full flow.

## Requirements

- JDK 17
- Android SDK 35

## Build

```bash
cp local.properties.example local.properties
# Edit local.properties with your Android SDK path

./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Install on phone

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or copy the APK to your phone and enable **Install unknown apps**.

## Phone settings

1. Open the app once after install
2. Settings → Apps → **BattBuddy** → Battery → **Unrestricted**
3. Do not force-stop the app

## Background behavior

Plug/unplug events are logged automatically. Swiping the app away is fine — only **force stop** or **restricted battery** will block logging.

## License

Personal project — not published on Play Store.
