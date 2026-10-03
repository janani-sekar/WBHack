# Hospitality Coach

An offline, local-language practice coach for small tourism operators.

**Status: working desktop AI backend with local Qwen inference, SQLite personalization, and automated tests. Android is the target; no Android app, frontend, emulator validation, or physical-device measurements yet. Initial model output failed semantic quality checks; Spanish is now the prototype default after a small automated comparison; language quality is still provisional.**

## Run the AI backend

Requires Python 3.10+ and Ollama. No Python package installation is needed.

```sh
ollama pull qwen3:1.7b
# Start Ollama first if it is not running: ollama serve
python3 -m hospitality.server
```

In another terminal:

```sh
curl http://127.0.0.1:8765/health
python3 scripts/smoke.py
python3 -m unittest discover -s tests -v
python3 scripts/check_api.py
```

Model provisioning needs internet once. Inference is restricted to a loopback endpoint with no cloud fallback. [API and examples](docs/API.md). [Model and test status](docs/MODEL.md). This is a desktop development harness, not an Android deployment.

## Run the bounded agent

The optional **LangGraph** harness retrieves approved local memory/progress, asks Qwen to select an allowed tool, executes it, and records the run locally. It currently supports starting personalized practice, understanding a supplied review, or asking for clarification. See [agent design and Android path](docs/AGENT.md).

```sh
python3 -m venv .venv
.venv/bin/pip install -r requirements-agent.txt
.venv/bin/python -m hospitality.agent "Help me practice explaining where guests should meet"
.venv/bin/python -m unittest discover -s tests -v
```

Dependencies and weights need initial online provisioning. Agent tracing is explicitly disabled. LangGraph runs on the desktop in this build; no claim is made that it runs inside an Android APK.

## The outcome
Help an operator rehearse a difficult guest conversation, understand what was missing, and handle a new similar situation more completely and accurately without assistance.

The operator is the learner. The product does not send guest messages or make bookings on their behalf.

## Core loop
Choose scenario → simulated guest asks → operator responds → coach gives one grounded suggestion → operator retries → next session adapts to approved language preferences and demonstrated skill gaps.

Example: a guest has only 30 minutes. The operator practices explaining a verified short coffee-tasting option, clarifying timing, and setting expectations. The coach checks completeness and factual consistency, not accent or an assumed universal etiquette standard.

## MVP boundaries
- Spanish coaching (prototype default; Tamil/Hindi experimental), English-speaking guest scenarios, one community, one accessible device, three scenario families.
- Typed interaction first; voice only after target-device and language validation.
- Fully offline core practice and feedback after installation and model provisioning.
- Operator-approved phrase memory and adaptive practice scheduling.
- Personalization uses approved phrase memory and rule-based practice selection. Model-weight updates are deferred.
- No cloud dependency, automatic guest messaging, payment processing, or autonomous booking.

## Read first
1. [Submission requirements](docs/SUBMISSION.md)
2. [Build and preparation plan](docs/PLAN.md)
3. [Continual learning design](docs/CONTINUAL_LEARNING.md)
4. [Architecture and model selection](docs/ARCHITECTURE.md)
5. [Evidence and evaluation](docs/EVALUATION.md)
6. [Pitch and demo script](docs/DEMO.md)
7. [Research sources and interview guide](docs/RESEARCH.md)

## Initial data
[data/sample/scenarios.json](data/sample/scenarios.json) and [data/sample/operator.json](data/sample/operator.json) are entirely synthetic planning fixtures in English. They are not real operator records, translations, training results, or evidence of community demand. See [data README](data/README.md).

The evaluation CSV is an empty results template. Do not report target thresholds as achieved results.

## Decisions before building
- [x] Select prototype language: Spanish (es), with English guest scenarios; changed from Tamil after automated screening.
- [ ] Identify a community for later real-world validation (not a prototype blocker).
- [ ] Confirm access to an operator and obtain voluntary consent for testing.
- [x] Select Android as the deployment target.
- [ ] Record an Android device specification and run emulator tests; no phone is available yet.
- [x] Implement a local Qwen3 1.7B adapter for desktop feasibility testing.
- [ ] Validate and integrate an Android inference runtime on an emulator, then real hardware.
- [ ] Verify the submission portal cutoff, timezone, upload fields, and link-access requirements.

## Challenge alignment
Prepared for World Bank Youth Summit × Hack-Nation, Small AI for Development, Tourism, October 3–4, 2026. This project is independent; no endorsement or partnership is claimed.

The challenge PDF is retained locally for reference and excluded from version control. Requirements here are paraphrased from sections 5–9 and Annex C. No official challenge document, signed download token, private participant data, or model weights are included.
