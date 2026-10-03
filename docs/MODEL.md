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
