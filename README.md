# WorthBit

Android application for tracking the cost per use of belongings. Native Kotlin, Android 7.0+ (API 24), English and Italian.

- Application ID: `com.rainbow.priceperuse`
- Version: `1.5` (`versionCode` 11)
- Compile SDK: 36; target SDK: 36

## Build

Open this directory in Android Studio with Android SDK 36 and JDK 17+.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

A local SDK fallback is available through `python3 scripts/build_offline.py`. It runs the official Gradle build offline using cached dependencies and Android SDK tools. The debug APK is written to `deliverables/worthbit-debug.apk`. Keep the existing development signing key for updates to installed debug builds.

## Features

Add an item with its purchase price and one of 30 pixel icons. Record each use, undo the latest tap, edit items or delete them with confirmation. The home screen shows up to three cards; larger inventories open through All things / Tutte le cose.

Statistics show total purchase value, use counts, unused items, top-five charts and the three lowest costs per use. Prices are stored as whole cents; cost per use is rounded HALF_UP to two decimal places. Rankings compare exact ratios. Unused items have no cost per use.

Settings include English / Italiano, Lavender / Ocean / Peach themes, About, Privacy, version information and replayable onboarding. Splash and onboarding follow the selected palette. The launcher uses a separate adaptive icon.

## Storage

Private SharedPreferences file: `rainbow_inventory`. Items are stored as JSON with `id`, `name`, `cents`, `icon` and `uses`. Language, theme and onboarding completion are also local. No server, account, advertising or Internet permission is required. Cloud backup is disabled.

## Sources

- `Item.kt`: item data and per-use calculation.
- `PriceInput.kt`: price input parsing and validation.
- `ItemStore.kt`: local persistence.
- `CollectionStats.kt`: aggregate calculations and rankings.
- `ItemIcons.kt`, `RainbowCat.kt`, `AppTheme.kt`: icons, animation and palettes.
- `PixelInfoDialog.kt`, `AppInformation.kt`: information panels and localized content.
- `MainActivity.kt`: screen state, navigation, input and drawing.

Unit tests are in `app/src/test`. Emulator checks are in `scripts/qa/statistics_emulator.py`; they use an isolated package. Specify `ANDROID_SERIAL` when multiple emulators are connected.

## Verification records

The structural-change snapshot, declaration comparison, metadata comparison and check logs are in `docs/refactoring/`. The snapshot is an audit archive, excluded from application resources and source distribution. The report distinguishes pre-existing lint findings from changes in this work.

## Compatibility update

Gradle 8.14.6, AGP 8.13.2, Kotlin 2.3.21, annotations 26.1.0 and JUnit 4.13.2. Fragment is not a dependency. Universal DEX supports armeabi-v7a, arm64-v8a, x86 and x86_64; there are no native libraries.

Compatibility checks and emulator screenshots are recorded in `docs/compatibility/REPORT.md`. `scripts/qa/compatibility.py` checks isolated test data, English and Italian layouts at 411, 380 and 320 dp, and responsive UI. The earlier Android 5 results describe build 10; build 11 requires Android 7.0+.

## Codemagic

See [signing setup](docs/codemagic/README.md). Root `codemagic.yaml` is the one-time cloud upload-key generator; switch to the separate signed-bundle workflow after registering the signing identity.
