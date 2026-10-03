# Android frontend

Native Kotlin / Jetpack Compose frontend for the Hospitality Coach planning repo. Android 8+ (API 26), compile/target SDK 35, Java 17, Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21.

## Implemented

- First-run introduction and Tamil/English coaching preference.
- Three scenario cards loaded from the existing `data/sample/scenarios.json` assets.
- Guest prompt, editable reply, fixed reference guidance, rubric, retry and alternate prompt.
- Explicitly approved, editable phrases; local persistence and reuse in matching scenarios.
- Local practice counts, unexplored scenarios listed first, phrase removal and confirmed reset.
- No Internet permission or network client. Android backup disabled.

## Run

Open this `android` directory in Android Studio. Use JDK 17 and install Android SDK 35. Configure Gradle 8.9 as the local Gradle distribution if Studio requests a wrapper (the wrapper binary is not bundled). Alternatively use an installed Gradle 8.9: `gradle testDebugUnitTest assembleDebug` from this directory. The APK is `app/build/outputs/apk/debug/app-debug.apk`.

Build dependencies need an initial internet download. The installed app's reference practice flows do not. No API key or backend is needed.

## Integration boundary and honest demo limits

`PracticeEngine` is the seam for a future on-device model; `ReferencePracticeEngine` is fixed, non-personalized reference content, not AI evaluation. Replace it with a bounded async model service before claiming offline AI. No inference runtime, model weights, translation engine, speech engine, review analysis, automatic messaging or bookings are included. Tamil coaching drafts require local review. Navigation remains English.

Business facts are the synthetic values in the original operator fixture, shown explicitly in the UI. Do not use this build for real prices or availability. Persistent personalization means saved phrases, not model-weight learning; unexplored-first ordering is not skill assessment. Alternate prompts are practice content, not a held-out evaluation set once shown to a learner.

This is a single-profile prototype. Preferences are app-private but there is no app lock, secure profile separation or custom encrypted vault. Do not put private guest data into phrases. Ordinary draft replies survive rotation but are not written to preferences.

## Verification

Unit tests cover blank-response rejection, preservation of demo facts despite instruction-like input, and language selection. A successful Android build and on-device UI testing must be recorded separately; source inspection alone does not establish them.

Device checklist: first-run language selection; all three practice/retry flows; rotate while typing; save phrase and force-stop/reopen; insert phrase into matching scenario; delete/reset; test large fonts and Tamil rendering; complete the loop in airplane mode. Verify on the actual operator's device before making accessibility or performance claims.
