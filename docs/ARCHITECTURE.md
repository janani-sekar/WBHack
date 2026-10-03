# Proposed architecture and feasibility gates

Language decision: Tamil (ta) coaching, with English guest scenarios proposed. Validate Tamil script rendering, colloquial/register variation, code-switching, and mixed Tamil/English input with a Tamil speaker. Do not infer speech-recognition quality from text quality.

Status: a desktop Python/Ollama backend now implements the task contracts with Qwen3 1.7B. Android remains the deployment target; Python/Ollama is not represented as the Android runtime. See API.md and MODEL.md.

## Offline core
Local interface → scenario state → response interpretation → fact/rubric checks → one coaching suggestion → retry → approved memory and skill history.

Store business facts, scenario rubrics, progress, and approved phrases locally. Keep raw learner response separate from instructions. Visitor comments and imported content are untrusted data, never control instructions.

## Recommended implementation path
- Android-first prototype if the target operator actually has access to Android; Kotlin/local storage is a plausible route, not a locked requirement.
- First build a text-based bounded scenario engine with reviewed prompts and deterministic checks for numerical facts.
- Use a compact trained classifier or compatible small language model for interpreting varied responses. Benchmark the selected language and device before selecting the final runtime.
- If generative output is used, constrain it to supplied facts, a short rubric, and one suggestion. Surface uncertainty and let the operator reject feedback.
- Do not force generative AI into arithmetic or availability checks.
- Add local speech recognition/synthesis only after separate language and device validation; text remains a fallback.

## Device/model manifest to fill
Device model / OS / physical RAM / available storage / access schedule / model ID and revision / license / quantization / model bytes / app bytes / provisioning method / cold-start seconds / p50 and p95 feedback latency / peak memory / session energy measurement method / supported language and reviewed dialects.

Proposed engineering targets, not challenge-prescribed limits or achieved results: text feedback p95 under 5 seconds on the selected device; bounded learner turns; no mandatory network requests after provisioning. Set a model/storage budget from the actual available phone capacity rather than inventing a universal limit.

## Offline proof
Install and provision once; show model size and transfer path. Disable Wi-Fi and cellular data. Force-close/reopen. Complete an unseen scenario, approve a phrase, restart, and reuse it. Inspect requests or device network behavior. A cached UI alone is not offline AI.

Receiving remote messages, downloading updates, and syncing are online operations. Training-role-play itself should need none of them. Initial download cost must be disclosed.

## Shared-device considerations
Use separate local profiles where needed. Explain deletion and device-backup behavior. Avoid exposing one operator's phrases or records to another. Device storage protection and app locking must be implemented/tested before claiming encryption/privacy guarantees.

## Model/runtime caveat
Google's MediaPipe LLM guide describes high-end device optimization. A successful desktop or flagship demo is not proof of performance on an entry-level phone.
https://developers.google.cn/edge/mediapipe/solutions/genai/llm_inference/android

## Agentic architecture update

Use the open-source LangGraph harness for the desktop reference implementation, with bounded Qwen tool selection, approved local memory, progress and review-derived lessons. The first agent entry point is implemented in `hospitality/agent.py`; existing HTTP routes remain explicit tools and are not yet a unified agent chat endpoint. See [agent design](AGENT.md) for the Android-native harness decision and remaining integration work. Personalization is stored context and curriculum adaptation, not model-weight training.

## Current prototype language decision

Spanish (`es`) is now the default, following the user's approval to use another language and proceed without a fluent-speaker study for the prototype. Existing Tamil references above describe the initial plan/results. This is automated/provisional evaluation, not native-speaker validation. The profile can select `es`, `ta` or `hi`; response keys are language-neutral (`strength_local`, `improvement_local`, `translation_local`, `explanation_local`, `reason_local`). Supported codes do not imply validated language quality. Historical evaluation JSON retains the original Tamil field names and outputs.
