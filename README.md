# Hospitality Coach

An offline, local-language practice coach for small tourism operators.

**Status: initial planning and submission materials. No working AI application, trained model, measured results, or validated local-language support is included yet.**

## The outcome
Help an operator rehearse a difficult guest conversation, understand what was missing, and handle a new similar situation more completely and accurately without assistance.

The operator is the learner. The product does not send guest messages or make bookings on their behalf.

## Core loop
Choose scenario → simulated guest asks → operator responds → coach gives one grounded suggestion → operator retries → next session adapts to approved language preferences and demonstrated skill gaps.

Example: a guest has only 30 minutes. The operator practices explaining a verified short coffee-tasting option, clarifying timing, and setting expectations. The coach checks completeness and factual consistency, not accent or an assumed universal etiquette standard.

## MVP boundaries
- Tamil coaching (user-selected), English-speaking guest scenarios, one community, one accessible device, three scenario families.
- Typed interaction first; voice only after target-device and language validation.
- Fully offline core practice and feedback after installation and model provisioning.
- Operator-approved phrase memory and adaptive practice scheduling.
- Optional true model learning: a tiny phrase-intent classifier updated only from approved corrections, evaluated on unseen paraphrases.
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
- [x] Select prototype language: Tamil (ta), with English guest scenarios.
- [ ] Name the actual community and Tamil-language reviewer.
- [ ] Confirm access to an operator and obtain voluntary consent for testing.
- [ ] Record the actual phone, OS, RAM, free storage, access hours, and connection conditions.
- [ ] Select a model/runtime only after an offline feasibility spike.
- [ ] Verify the submission portal cutoff, timezone, upload fields, and link-access requirements.

## Challenge alignment
Prepared for World Bank Youth Summit × Hack-Nation, Small AI for Development, Tourism, October 3–4, 2026. This project is independent; no endorsement or partnership is claimed.

The challenge PDF is retained locally for reference and excluded from version control. Requirements here are paraphrased from sections 5–9 and Annex C. No official challenge document, signed download token, private participant data, or model weights are included.
