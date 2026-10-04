# Offline verification

## Result

The desktop prototype completed real inference with external networking denied for the test worker, backend, fresh Ollama process and its model runner. Localhost communication remained allowed. The report is `evaluation/offline-verification.json`; it includes model outputs, not just mocked responses.

The test verified:
- An outbound TCP connection to a public IP was rejected with EPERM by the process sandbox.
- The local UI HTML was served.
- LangGraph selected and executed a practice action.
- A learner response received actual local-model feedback.
- A customer review received an interpretation.
- A language preference was written to and read from local SQLite.

This is evidence of offline execution, not of output correctness. Incorrect translations and scores can still be generated offline. The normal UI loads no external fonts, scripts or assets; browser-level whole-device airplane-mode testing was not performed. Android/emulator, battery and memory validation are separate unfinished gates.

## Reproduce on this Mac

With model weights, Ollama and the Python virtual environment already installed:

```sh
.venv/bin/python scripts/verify_offline.py > /tmp/offline-result.json
```

The verifier uses macOS `sandbox-exec`, starts isolated services on temporary loopback ports, disables Ollama cloud use, and cleans up its processes and temporary test database. It does not change system Wi-Fi settings or the running demo's database. No model download occurs in this test. The current script uses the installed `/usr/local/bin/ollama` path.

Internet is required for initial software/model installation and fetching new models or code. It is not required for the measured core runtime. Model files are still about 1.36 GB: offline operation does not establish that sideloading over a weak connection is convenient or that an entry-level Android phone has enough RAM.

## Device-level follow-up

To demonstrate the current Mac UI manually, load the app, disable Wi-Fi through the normal system UI, reload the localhost page, then start a new practice task and analyze a new review. Keep Ollama and the backend running. This has not been performed here because it would disconnect unrelated work. For the final Android build, repeat with airplane mode, cold launch and installed weights; report actual RAM, latency, storage and battery separately.
