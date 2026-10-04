# Improving the agent harness without training on the learner

## Literature and practical interpretation

- [GEPA: Reflective Prompt Evolution Can Outperform Reinforcement Learning](https://arxiv.org/abs/2507.19457) uses execution feedback to propose and compare prompt changes. Use it as a development-time optimizer, then ship a fixed artifact. It does not require adapting model weights on each phone.
- [Better Harnesses, Smaller Models](https://arxiv.org/abs/2607.08938) reports gains from adapting harnesses to small models across business tasks. The relevant hypothesis here is to move repeated rules and checks into code; the paper does not establish Qwen 1.7B hospitality performance or Android latency.
- [HarnessOpt-Bench](https://arxiv.org/abs/2608.06301) evaluates optimization against a held-out partition and fixed budgets. Our small public reserved set is only a starting split, not an equivalent secured benchmark.
- [Anthropic's agent-evaluation engineering guide](https://www.anthropic.com/engineering/demystifying-evals-for-ai-agents) distinguishes deterministic, model and human graders and recommends inspecting trajectories. We use executable state checks for consent/memory and separate model-quality cases. Vendor engineering guidance is not a learning-outcome trial.

The 2026 harness papers are recent research; results on their tasks should not be generalized to this device, language or domain without measurement.

## Our error-to-intervention map

| Observed failure | Harness change | How to judge it |
|---|---|---|
| Wrong practice category | Explicit scenario catalog and constrained action schema | Intent accuracy plus wrong-case rate |
| Repeated generic guest question | Authored question bank, model follow-ups | Coverage across cases and factual consistency |
| Invented feedback options | Supply bounded exercise facts and critical errors | Unsupported claims per response |
| Praise mislabeled / criticism lost | Separate review evidence, polarity and aggregation tasks | Per-theme precision/recall and source coverage |
| Model marks itself successful | Pending scores, independent state rules | No unapproved progress mutation |
| Same-day retries inflate progress | One accepted response per session; delayed distinct cases | State-transition tests |
| Larger context hurts the small model | Send current exercise and bounded relevant memory | Quality/latency/context ablation |

Implemented now: authored bank, isolated exercise facts, task-specific anchors, limited session evidence, local retention heuristic and tests. Review decomposition, optimizer-driven prompt search and rigorous evaluator calibration remain future work.

## Bounded optimization experiment

1. Freeze model digest, quantization, runtime, prompts, seeds if supported and dataset version.
2. Partition by scenario family and guest paraphrase, not random near-duplicate sentences. Keep final cases out of prompts and optimization feedback.
3. Establish baselines: existing broad prompt, task-specific rubric, fixed phrasebook/reference reply, then bounded agent. Evaluate each on the same cases and repeat runs to expose nondeterminism.
4. Classify failures before adding tools. Change one of prompt, schema, retrieval scope, control flow or verifier at a time.
5. Give the optimizer a fixed call/token budget and textual failure feedback. Do not let it edit expected outcomes, protected tests, approval logic or network policy.
6. Compare on development data; nominate one candidate before running the final reserved set. If those cases influence a repair, they become development data and require replacement.
7. Report quality, unsupported-claim rate, refusal/uncertainty rate, p50/p95 latency, memory footprint, download size and number of model calls separately. A single scalar can hide dangerous trade-offs.
8. Ship the selected static prompt/harness and model. Learner memory continues locally under explicit controls; no optimizer runs silently over private reviews.

The current four-case development grader script tests three false promises and one adequate answer. It is intentionally small and cannot establish benchmark accuracy. The six reserved cases live separately and are not loaded into the teaching app, but public repository access is not a secure evaluation boundary. A future optimization runner needs a restricted evaluator process.

GEPA is researched and recommended for a controlled experiment; it has NOT been installed or run in this change. A stronger development-time evaluator may help, but cloud evaluation of private learner data is not authorized or needed for this prototype. Offline deployment remains a separate requirement from how public synthetic development data is optimized.

## Measured development result

The broad rubric passed 1/4 author-defined checks: it incorrectly awarded full accuracy to invented booking/price, directions and photo permission. A second, narrow factual-support call now overrides unsupported answers to accuracy 0 and blocks uncertain assessments. With that change, the same four development checks passed 4/4. See `evaluation/rubric-development.json` and `evaluation/rubric-decomposed.json`. This is a single run on examples used during repair, not held-out accuracy or evidence of learning gains. The adequate-answer case still receives a redundant coaching suggestion, and other rubric dimensions can remain inflated. The six reserved cases have not been evaluated.
