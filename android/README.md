# Android frontend

Native Kotlin / Jetpack Compose frontend for the Hospitality Coach planning repo. Android 8+ (API 26), compile/target SDK 35, Java 17, Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21.

## Implemented

- First-run introduction and app language: **Español (default)**, தமிழ், English. Spanish also translates the app's screens; Tamil keeps English navigation and is used by the offline guide.
- **Matches the desktop web UI (`hospitality/ui`) through the local backend** (`hospitality.server`), with five tabs when the coach is connected:
  - **Home**: recommended next situation from `/learning-plan`, shortcuts to the other tabs.
  - **Practice**: all public `/curriculum` situations with skill filters and saved review lessons. A practice runs up to three guest messages (`/sessions`, `/responses`, `/next`), with a guest-language toggle (`/sessions/{id}/language`), a coach-feedback language toggle (`/sessions/{id}/coach-language`; scores are unchanged), a "without hints" mode, exercise facts, and goals. Feedback shows the five-criterion scorecard (including `customer_tone`), what worked, the next step, and the evidence quote, plus explicit save/dismiss before anything counts as progress. Unscored replies (`assessment_issue`: `checks_disagree`, `facts_unclear`, `interpretation_unclear`) are shown as grading problems, not failing grades, and cannot be saved. **Finish** shows the `/finish` recap and an editable business-notes draft (`/practice-drafts`, `/drafts/{id}`).
  - **Reviews**: up to five guest reviews → `/review-batches` (translation and findings) → "Practice similar" approves a lesson (`/batch-lessons`) and starts a practice with synthetic exercise facts.
  - **Progress**: learning plan per skill, learner memory with source quotes and "stop using this feedback", saved phrases.
  - **Settings**: practice settings (`/learning-settings`: guest language, message style, coach support). Coach language follows the app language (es/en).
- Automatic fallback to the fixed offline practice guide (three scenarios, Home / Phrases / Settings) when the backend or model is unavailable.
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

The home screen shows "AI coach connected" or "Offline practice guide". Expect several seconds to a minute per model call; a reply makes several model calls (assessment, fact check, then the next guest message), so the app waits up to 5 minutes per request. On a laptop without a GPU, `--model qwen3:1.7b` is much faster than `qwen3:4b-instruct`.

## Integration boundary and honest demo limits

`CoachApi` talks to the desktop backend; `ReferencePracticeEngine` is fixed, non-personalized reference content, not AI evaluation. On-device inference is still future work (see `docs/AGENT.md`): no inference runtime, model weights, translation engine, speech engine, review analysis, automatic messaging or bookings are included. AI feedback quality is provisional (see `docs/MODEL.md`). Spanish and Tamil coaching are not native-speaker validated. Guest dialogue is English.

Business facts are the synthetic values in the original operator fixture, shown explicitly in the UI. Do not use this build for real prices or availability. Persistent personalization means saved phrases, not model-weight learning; unexplored-first ordering is not skill assessment. Alternate prompts are practice content, not a held-out evaluation set once shown to a learner.

This is a single-profile prototype. Preferences are app-private but there is no app lock, secure profile separation or custom encrypted vault. Do not put private guest data into phrases. Ordinary draft replies survive rotation but are not written to preferences.

## Verification

Unit tests cover the offline guide (blank-response rejection, preservation of demo facts, Spanish/Tamil/English selection), the backend client against a fake loopback server (curriculum, session/turn/recap parsing, `assessment_issue`, feedback translations, review batches → lessons, learning plan, learner memory, settings writes with `approved`, no `Origin` header, error surfacing), and the scorecard/unscored wording rules shared with the web UI. A successful Android build and on-device UI testing must be recorded separately; source inspection alone does not establish them.

Device checklist: first-run language selection; all three practice/retry flows; rotate while typing; save phrase and force-stop/reopen; insert phrase into matching scenario; delete/reset; test large fonts and Tamil rendering; complete the offline loop in airplane mode; with the backend running, complete a three-reply practice with recap, a review → Practice similar flow, and both language toggles. Verify on the actual operator's device before making accessibility or performance claims.
