# Product and implementation plan v2

Updated 2026-10-03 from the team's agreed scope. Target: **Android**. No physical Android phone is currently available. Desktop resource-constrained tests are a development surrogate, not proof of Android compatibility or speed. Android emulator integration and eventual hardware testing remain explicit gates.

## Priority and completion criteria
| Priority | Feature | Done when |
|---|---|---|
| P0 | Offline hospitality training | Guest role-play, Tamil feedback, retry, and progress work locally |
| P0 | Review understanding | Original review, Tamil explanation, evidence-backed themes |
| P0 | Review-informed practice | Operator approves a theme and starts relevant practice |
| P0 | Personalization | Approved vocabulary and skill history survive restart; inspect/delete/reset work |
| P0 | Offline frontend | Chat with progress sidepanel/drawer calls on-device application logic |
| P1 | Draft review response | Editable, human-approved reply, only after P0 passes |
| Deferred | Inbound requests and platform integrations | Out of this iteration |
| Deferred | Model-weight training and speech | Not required for the core; separately evaluated later |

## Phase 1: AI feasibility and service contract
1. Provision Qwen3 1.7B locally; record exact model digest, size, quantization, and license.
2. Implement a replaceable local inference adapter and structured outputs.
3. Test real Tamil coaching and review explanation, not canned output.
4. Keep a desktop harness for iteration; do not present its Python/Ollama stack as an Android backend.
5. Run a limited-resource profile (two inference threads, short context, bounded output). Report actual host, settings, and latency. This does not emulate ARM scheduling, phone RAM pressure, thermals, or battery life.

## Phase 2: Hospitality AI
1. Create verified business facts and three scenario families: duration, directions, expectations.
2. Generate a short guest opening; accept Tamil text.
3. Assess against a small rubric; give one strength and one improvement in Tamil.
4. Require exact source evidence for assessments; surface uncertain outputs.
5. Allow retry and a follow-up guest turn.
6. Keep model assessments pending until operator approval; rejected/uncertain assessments do not count.
7. Compute progress transparently; practice results are not certified mastery.

## Phase 3: Reviews
1. Paste one review; retain original locally.
2. Translate and explain in Tamil; extract a bounded set of themes with source quotes.
3. Distinguish operational concerns from trainable skills.
4. Require operator approval before creating a lesson.
5. Use a controlled scenario associated with the skill; do not inject raw customer text into training instructions.
6. Keep review replies out of the critical path.

## Phase 4: Personalization
1. Ask before saving a preferred expression.
2. Store phrase, preferred form, context, language and approval timestamp locally.
3. Retrieve bounded preferences during relevant AI tasks; business facts take precedence.
4. Recommend practice based on approved attempts and skill coverage using transparent rules.
5. Verify restart persistence, deletion, reset, and no progress changes from rejected feedback.
6. Describe this as memory/curriculum personalization, not LLM retraining.

## Phase 5: Frontend and Android integration
1. Build Practice and Reviews screens plus business profile/settings.
2. Show Guest and Coach separately in chat, with original review next to Tamil interpretation.
3. Show progress in a sidepanel on wide layouts and drawer/tab on phones.
4. Integrate a supported Android local runtime through the same task contracts. Ollama/Python is only the desktop development harness.
5. Use an Android emulator for app lifecycle, layout, storage and runtime integration if tooling becomes available.
6. Disable emulator network and verify restart + coaching + reviews + personalization. A phone UI using host-computer inference is not on-device inference.
7. Record physical-device performance as unverified until actual hardware is tested.

## Phase 6: Evaluation and submission
1. Tamil-speaker review of outputs, including mixed-language and ambiguous inputs.
2. Compare practice with a static lesson; test new scenarios, not memorized prompts.
3. Record real model latency, errors and memory measurements where available.
4. Demonstrate review → approved topic → practice → feedback → retry → progress → restart persistence.
5. Package reproducible setup, model/data manifests, limitations, and the required 2–5 minute video.
6. Verify portal cutoff/timezone and submit with buffer.

## Current iteration
Build and test the AI backend first. Frontend, Android packaging/emulator tests, and native-speaker quality evaluation are subsequent work. A desktop smoke test is not an Android test.

## Agentic architecture update

Use the open-source LangGraph harness for the desktop reference implementation, with bounded Qwen tool selection, approved local memory, progress and review-derived lessons. The first agent entry point is implemented in `hospitality/agent.py`; existing HTTP routes remain explicit tools and are not yet a unified agent chat endpoint. See [agent design](AGENT.md) for the Android-native harness decision and remaining integration work. Personalization is stored context and curriculum adaptation, not model-weight training.

## Current prototype language decision

Spanish (`es`) is now the default, following the user's approval to use another language and proceed without a fluent-speaker study for the prototype. Existing Tamil references above describe the initial plan/results. This is automated/provisional evaluation, not native-speaker validation. The profile can select `es`, `ta` or `hi`; response keys are language-neutral (`strength_local`, `improvement_local`, `translation_local`, `explanation_local`, `reason_local`). Supported codes do not imply validated language quality. Historical evaluation JSON retains the original Tamil field names and outputs.
