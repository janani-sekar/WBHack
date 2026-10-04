# Hospitality Coach

An offline, local-language practice coach for small tourism operators.

**Prototype setting: Colombia.** Our [country context and real operator story](docs/COLOMBIA_CONTEXT.md) distinguish published accounts of family coffee tourism from the fictional farm, guest messages and reviews used in the demo. No operator endorsement or field validation is claimed.

**Status: working desktop AI backend with local Qwen inference, SQLite personalization, and automated tests. Android is the target. The [Android frontend](android/README.md) connects to this local backend over `adb reverse` (Spanish default, offline fallback); on-device inference and physical-device measurements remain pending. Initial model output failed semantic quality checks; Spanish is now the prototype default after a small automated comparison; language quality is still provisional.**

## Run locally (browser demo)

Requires **Python 3.10+**, Git, and [Ollama](https://ollama.com/download). The browser demo uses the Python standard library: no Node build, API key, or Python packages are required. The separate LangGraph agent instructions are below.

1. Install Python and Ollama, then open the Ollama application. If running it from a terminal instead, run `ollama serve` and leave that terminal open.
2. Clone the repository and download the model (internet required for this first setup):

   ```sh
   git clone https://github.com/srsambara/WBHack.git
   cd WBHack
   ollama pull qwen3:4b-instruct
   ```

3. Start the app from the repository folder:

   ```sh
   python3 -m hospitality.server --db data/private/ui-demo.sqlite3 --model qwen3:4b-instruct
   ```

   On Windows, use `py -3` instead of `python3`.

4. Open **http://127.0.0.1:8765/**. Keep Ollama and the server running; Ctrl+C stops the server.

**Try it:** choose a situation in Practice and reply to the guest. Or paste a review into Guest feedback, select **Practice similar**, then open the Practice tab → **From your reviews**. Progress uses feedback you approve. Personalization is stored locally in SQLite; it does not retrain model weights.

The Qwen download is about **2.5 GB**. Allow additional memory for the model runtime and operating system; speed depends on your computer. This prototype has been developed on an 8 GB Mac, but that is not a validated minimum requirement. Android/on-phone inference is not verified. After setup, core practice and review understanding use local inference with no cloud fallback.

**Troubleshooting:**
- Coach unavailable: make sure Ollama is running and `ollama list` includes `qwen3:4b-instruct`.
- Port 8765 in use: stop an older server, or add `--port 8766` and open that port instead.
- Slow response: leave the page open while the model runs; the first request may take longer while loading weights.
- For a separate learner, use another database path with `--db data/private/another-learner.sqlite3`.

To check the backend, open http://127.0.0.1:8765/health. See [API documentation](docs/API.md) and [model limitations](docs/MODEL.md).

## Run the bounded agent

The optional **LangGraph** harness retrieves approved local memory/progress, asks Qwen to select an allowed tool, executes it, and records the run locally. It currently supports starting personalized practice, understanding a supplied review, or asking for clarification. See [agent design and Android path](docs/AGENT.md).

```sh
python3 -m venv .venv
.venv/bin/pip install -r requirements-agent.txt
.venv/bin/python -m hospitality.agent "Help me practice explaining where guests should meet"
.venv/bin/python -m unittest discover -s tests -v
```

Dependencies and weights need initial online provisioning. Agent tracing is explicitly disabled. LangGraph runs on the desktop in this build; no claim is made that it runs inside an Android APK.

## Offline evidence and richer practice

[Offline verification](docs/OFFLINE.md) now includes an actual process-isolated test: internet denied, localhost permitted, real Qwen inference. [Training research](docs/TRAINING_RESEARCH.md), [rubrics](docs/RUBRIC.md), and [harness evaluation plan](docs/HARNESS_EVALUATION.md) document the next iteration.

The desktop UI offers 19 synthetic customer situations, anchored rubrics and an independent-practice mode. Six evaluation cases and eight review bundles live in [the curriculum pack](data/curriculum/README.md). They are not real customer transcripts. Model quality and learning effectiveness remain separate from software tests.

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
- Operator-approved assessment memory and adaptive practice scheduling.
- Personalization uses approved assessment memory and rule-based practice selection. Model-weight updates are deferred.
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

## Redesigned browser demo and submission package

Open http://127.0.0.1:8765 after starting the backend. The **Host with Confidence** interface offers a situation catalog, guest conversations with optional hints, review-to-practice, English/Spanish guest selection, and progress. It uses the actual local backend.

See [experience design](docs/EXPERIENCE_DESIGN.md) for the operator flow and references, and [submission packaging](docs/SHAREABLE_DEMO.md) for hosting options and limitations. Build a source-only archive with `python3 scripts/package_demo.py`; output: `dist/welcome-hospitality-demo.zip`. This is not yet a hosted public app.

Practice now supports three guest exchanges, clear/everyday-message preferences and optional coaching. The Your words tab has been removed. For the larger local model, run `ollama pull qwen3:4b-instruct` then `python3 -m hospitality.server --model qwen3:4b-instruct`. See [practice design and measured limits](docs/PRACTICE_METHOD.md).
