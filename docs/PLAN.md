# Concrete preparation and build plan

Use elapsed hours so the plan can fit the time actually remaining. Confirm the portal deadline immediately. This is a proposed 24-hour effort budget, not a promise about the event cutoff. If time is shorter, drop voice and review import before reducing verification.

| Phase | Hours | Deliverable | Exit condition |
|---|---:|---|---|
| Ground the problem | 0–2 | One operator profile, recent incident, device inventory, named language reviewer | Real workflow and accessible device established; assumptions labeled |
| Offline feasibility | 2–4 | Model/runtime spike on actual device | Two meaningful local-language exchanges offline; measure latency, RAM, storage |
| Core practice loop | 4–10 | Scenario → response → grounded feedback → retry | Three complete scenarios; no internet calls required |
| Personalization | 10–13 | Approved phrase memory, editable profile, practice scheduler | Correction persists after restart; deletion and reset work |
| Learning experiment | 13–15 | Optional tiny classifier update | Held-out improvement and retention tested; otherwise label experimental and disable |
| Evaluation | 15–19 | Baseline comparison, device results, error log | Actual results recorded; no fabricated performance claims |
| Packaging | 19–22 | Reproducible README, model/data manifest, 3–4 minute video | Clean install and model provisioning independently repeated |
| Submission buffer | 22–24 | Portal submission and receipt | Every link works for intended judges; save confirmation |

## P0: must work
1. Choose one of three scenarios: welcome/explain offering; time-constrained request; directions clarification.
2. Show a simulated guest prompt in an appropriate language.
3. Accept typed response in the named local language.
4. Evaluate against explicit requirements and verified operator facts.
5. Give one actionable suggestion with an evidence reference; allow disagreement.
6. Retry with a varied guest follow-up.
7. Save only approved preferences and progress locally.
8. Show memory contents, deletion, and reset controls.

## P1: only after P0 passes
- A small incremental classifier that learns approved local phrase-to-intent examples.
- A few consented or clearly synthetic reviews that select future practice topics.
- Voice input/output with supported language and device measurements.

## Out of scope
Automatic guest communication, universal etiquette scoring, accent grading, new-language acquisition from a few corrections, autonomous booking, payment processing, full LLM retraining on a basic phone, and claims of increased income from a weekend demo.

## Ownership
Assign people to product/community validation, implementation, and evaluation/pitch. These are responsibilities, not a requirement for a particular team size. A solo builder should follow the phases serially and keep P1 small.

## Decision gates
- No real local-language reviewer: recruit one or choose a language the team can reliably validate; do not invent fluent-language claims.
- No smartphone accessible to the user: redesign around a verified existing shared device and actual access schedule, or select another evidence-backed operator. A new phone purchase is not the brief's default.
- Language model fails on device: use a narrower learned intent/skill classifier with reviewed scenario templates and deterministic factual checks. AI must still influence feedback meaningfully.
- Speech fails: keep text and disclose the literacy/access limitation.
- Learning regresses: retain approved memory, disable model update, report the result honestly.
