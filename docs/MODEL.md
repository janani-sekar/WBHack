# Model and validation manifest

## Installed desktop candidate
- Local inference provider: Ollama 0.34.0, loopback HTTP only, proxies/redirects disabled in the application adapter.
- Requested tag: `qwen3:1.7b`.
- Observed digest: `8f68893c685c3ddff2aa3fffce2aa60a30bb2da65ca488b61fff134a4d1730e7`.
- Observed installed size: 1,359,293,444 bytes (about 1.36 GB decimal).
- Format: GGUF; quantization: Q4_K_M; license shown by runtime: Apache 2.0.
- The local metadata reports 2.0B parameters and 40960 context despite the 1.7b tag. These are recorded as observed rather than silently assuming the model-card figure. Pin/verify digest before reproducing results; mutable tags can change.
- Model weights are not committed to Git. `ollama pull` is a one-time online provisioning step; the app does not download a missing model automatically.

## Inference settings
- Thinking disabled; temperature 0.15; structured JSON outputs.
- Default context 4096 tokens; output budget 1200 tokens.
- `--constrained`: CPU-only (`num_gpu=0`), two threads, 2048-token context. This is only a desktop resource-constrained surrogate, NOT an Android emulator, RAM cap, thermal simulation, or battery benchmark.
- Endpoint and model identifiers are restricted. No cloud fallback is implemented.
- Exact evidence quotes are validated; schema failures/truncation are surfaced instead of saved as valid feedback. This does not guarantee semantic correctness or eliminate prompt injection.

## Tests and remaining gates
- Unit tests use labeled fake inference to test application behavior, not language quality.
- HTTP checks use isolated temporary state and do not require model inference.
- `python3 scripts/smoke.py` exercises the actual local model with synthetic Tamil/English examples. `--constrained` selects the limited-resource profile.
- Local endpoint restriction is not equivalent to OS-wide airplane-mode/network-isolation verification.
- No Android runtime integration, APK, emulator test, physical-device benchmark, native-speaker Tamil evaluation, or learning-outcome study yet.
- Review reply generation, speech, inbound requests and weight training are not implemented.

## References
- https://ollama.com/library/qwen3:1.7b
- https://huggingface.co/Qwen/Qwen3-1.7B
- https://docs.ollama.com/api/chat
- https://docs.ollama.com/capabilities/structured-outputs

## Observed desktop results (2026-10-03)

Ten application unit tests and the HTTP integration checks passed. The real local-model smoke completed guest generation (3.079 s), assessment (7.957 s), and review interpretation (8.797 s), and confirmed memory persistence/deletion. These are individual desktop measurements, not a benchmark or Android estimates.

**Language-quality gate FAILED.** The review translation did not preserve meaning, a negative directions clause was labeled positive, and coaching mostly repeated the learner response. The review was marked uncertain, so it cannot be promoted to a lesson. Model uncertainty is not a reliable detector of every bad output. Earlier development runs also hit output truncation and invalid evidence quotes; tighter prompts and source-constrained quotes fixed those integration failures, not semantic quality.

See [raw synthetic run](../evaluation/desktop-smoke.json). No output was approved as human-validated learning progress. Next: compare Tamil-capable candidates on a fixed, native-reviewed evaluation set before choosing the Android model. This candidate is useful for wiring the system, not ready for operator use.

The CPU-only/two-thread run also completed, with 11.486 s opening, 80.089 s assessment, and 74.831 s review interpretation. Tamil quality still failed. See [constrained run](../evaluation/desktop-constrained.json). This latency is not acceptable for the intended UX and cannot predict Android latency.

The LangGraph agent completed a real model-selected practice tool call; [trace](../evaluation/agent-smoke.json). Thirteen application/harness tests now pass. Harness routing does not resolve the language-quality failure.

## Language screen

After the user allowed another language, two identical translation examples were screened in Spanish, Hindi and Tamil. Spanish was more intelligible but still had grammatical and meaning errors; Tamil and Hindi showed serious repetition/meaning loss. [Raw outputs](../evaluation/language-screen.json) and `scripts/compare_languages.py` make this reproducible. This is not a language benchmark. Spanish is the next candidate to validate with a fluent speaker/community; the existing application remains Tamil until a supported language is selected and coaching/review prompts and schemas are updated together.

## Current prototype language decision

Spanish (`es`) is now the default, following the user's approval to use another language and proceed without a fluent-speaker study for the prototype. Existing Tamil references above describe the initial plan/results. This is automated/provisional evaluation, not native-speaker validation. The profile can select `es`, `ta` or `hi`; response keys are language-neutral (`strength_local`, `improvement_local`, `translation_local`, `explanation_local`, `reason_local`). Supported codes do not imply validated language quality. Historical evaluation JSON retains the original Tamil field names and outputs.

The [Spanish full workflow](../evaluation/spanish-smoke.json) completed with a much clearer review translation/summary. Theme classification still failed (criticism labeled positive), and coaching suggested unnecessary specificity. This remains a known model-quality issue; the uncertain review cannot become a lesson. These limitations remain even though application/harness tests pass.
