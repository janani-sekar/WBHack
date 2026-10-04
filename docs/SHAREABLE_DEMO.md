# A submission link without on-phone inference

The supplied concept note asks for a working prototype with code or a link, plus a 2–5 minute video. Verify the actual submission portal before filing. A GitHub repository and video can therefore be the submission links; a publicly hosted interactive app is an additional convenience. A remote-server demonstration does not by itself satisfy the brief's offline fidelity requirement.

## Recommended submission package

- Repository: https://github.com/srsambara/WBHack (publish the latest local work before submitting).
- A 2–5 minute video showing guest feedback → approved lesson → practice → feedback approval → personalized practice, plus the offline verification.
- A downloadable source archive with installation instructions and a manifest. Build with `python3 scripts/package_demo.py`.
- The real offline test report and honest limitations. The Mac runs Qwen locally; the Android client may use `adb reverse`. No claim of on-phone inference.

The source archive contains no model weights, personal databases, original challenge PDF or credentials. Judges must install dependencies and the named model before local execution. It is a source package, not a standalone executable.

## Optional interactive web link

A production HTTPS frontend can talk to Qwen running on a dedicated server. Keep the model service private. Provision enough RAM/compute, preload weights, measure latency and keep it awake for judging. Separate judge sessions and their storage, cap input sizes and inference concurrency, add request limits, and provide a reset of each visitor's demo data. Use synthetic inputs by default.

This requires a deployment adapter: the current Python HTTP server deliberately only accepts localhost and is single-profile. Do not expose the developer server or the existing database through a tunnel and call it a multi-user product. A host/domain/account and resource budget have not been selected; no public app deployment or paid resources have been created.

The hosted version must say that inference is server-hosted and needs connectivity. Submit the local offline version and evidence alongside it. A static site can show a recorded walkthrough, but cannot run our Python/Ollama backend by itself and should never label canned outputs as live AI.

## Suggested 3-minute walkthrough

0:00 — Introduce the operator's problem and label the farm example synthetic.
0:20 — Open Guest feedback and interpret the example review.
0:55 — Approve one communication lesson and open related practice.
1:20 — Reply to the guest, show the factual check, and accept or reject the feedback.
1:55 — Switch the guest language; show saved practice preferences and accepted progress evidence.
2:20 — Show the network-denied inference report and distinguish desktop from Android.
2:40 — State measured limits and the next validation steps.
