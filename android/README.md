# Android frontend

Native Kotlin / Jetpack Compose frontend for the Hospitality Coach planning repo. Android 8+ (API 26), compile/target SDK 35, Java 17, Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21.

## Implemented

- First-run introduction and coaching language: **Español (default)**, தமிழ், English. Spanish also translates the app's screens; Tamil keeps English navigation.
- **AI practice through the local Qwen backend** (`hospitality.server`): model-generated guest question, feedback (`strength_local`, `improvement_local`, evidence quote, 0–2 checklist), explicit accept/reject before anything counts as progress, model follow-up questions, and approved phrases synced to backend memory.
- Automatic fallback to the fixed offline practice guide when the backend or model is unavailable (or English is selected, which the backend does not support).
- Three scenario cards loaded from the existing `data/sample/scenarios.json` assets.
- Guest prompt, editable reply, fixed reference guidance, rubric, retry and alternate prompt.
- Explicitly approved, editable phrases; local persistence and reuse in matching scenarios.
- Local practice counts, unexplored scenarios listed first, phrase removal and confirmed reset.
- `INTERNET` permission is used only for the loopback backend; cleartext HTTP is permitted only to `127.0.0.1`/`localhost` (`res/xml/network_security_config.xml`). No cloud services. Android backup disabled.

## Run

Open this `android` directory in Android Studio. Use JDK 17 and install Android SDK 35. Configure Gradle 8.9 as the local Gradle distribution if Studio requests a wrapper (the wrapper binary is not bundled). Alternatively use an installed Gradle 8.9: `gradle testDebugUnitTest assembleDebug` from this directory. The APK is `app/build/outputs/apk/debug/app-debug.apk`.

Build dependencies need an initial internet download. The offline practice guide works without a backend. No API key is needed.

### AI coach (local only)

The model runs on the development computer, not inside the APK. The app calls `http://127.0.0.1:8765` (editable in Settings), and `adb reverse` forwards that port to the computer over USB or to the emulator, so the backend's loopback-only checks stay intact:

```bash
ollama pull qwen3:1.7b            # once
python3 -m hospitality.server     # from the repo root
adb reverse tcp:8765 tcp:8765     # after each device connect / emulator boot
```

The home screen shows "AI coach connected" or "Offline practice guide". Expect several seconds to a minute per model call.

## Integration boundary and honest demo limits

`CoachApi` talks to the desktop backend; `ReferencePracticeEngine` is fixed, non-personalized reference content, not AI evaluation. On-device inference is still future work (see `docs/AGENT.md`): no inference runtime, model weights, translation engine, speech engine, review analysis, automatic messaging or bookings are included. AI feedback quality is provisional (see `docs/MODEL.md`). Spanish and Tamil coaching are not native-speaker validated. Guest dialogue is English.

Business facts are the synthetic values in the original operator fixture, shown explicitly in the UI. Do not use this build for real prices or availability. Persistent personalization means saved phrases, not model-weight learning; unexplored-first ordering is not skill assessment. Alternate prompts are practice content, not a held-out evaluation set once shown to a learner.

This is a single-profile prototype. Preferences are app-private but there is no app lock, secure profile separation or custom encrypted vault. Do not put private guest data into phrases. Ordinary draft replies survive rotation but are not written to preferences.

## Verification

Unit tests cover blank-response rejection, preservation of demo facts despite instruction-like input, Spanish/Tamil/English selection, and the backend client against a fake loopback server (scenario→skill mapping, `_local` parsing, no `Origin` header, language updates, error surfacing). A successful Android build and on-device UI testing must be recorded separately; source inspection alone does not establish them.

Device checklist: first-run language selection; all three practice/retry flows; rotate while typing; save phrase and force-stop/reopen; insert phrase into matching scenario; delete/reset; test large fonts and Tamil rendering; complete the offline loop in airplane mode; with the backend running, complete an AI round including accept/reject and a follow-up question. Verify on the actual operator's device before making accessibility or performance claims.
