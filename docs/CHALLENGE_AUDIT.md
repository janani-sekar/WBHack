# Revised challenge audit

Reviewed 3 October 2026 against the supplied World Bank Small AI concept note: rules p.7, data p.8, deliverables pp.10–11, tourism pp.19–20. This is a development estimate, not an official score or a prediction of winning. Scores assume judges inspect the current implementation and the evidence already saved, not planned features.

## Current estimate: 66 / 100

| Criterion | Estimate | Evidence and remaining deduction |
|---|---:|---|
| Small AI fidelity | 15 / 25 | Real local Qwen inference and no cloud fallback. Process-isolated offline evidence covers the earlier 1.7B build; current demo uses 4B. No Android inference or measurements. Weight-file size alone does not establish RAM, latency, battery, or user access to suitable hardware. |
| Development relevance and impact | 16 / 20 | Colombia context, an attributed published family-farm story, and review findings connected to practical rehearsal. No firsthand operator validation or demonstrated workplace improvement. The real family's communication problems are not established. |
| Data grounding | 9 / 15 | Training sources, synthetic labels, fixed exercise facts and review quotes. Current pack: 18 teaching cases, six reserved cases, eight review bundles. Need a consolidated source/license/size/coverage manifest and stronger representative evaluation. Published context is not training data or user research. |
| Evidence it works | 8 / 15 | 43 software tests, HTTP checks and real-model development examples. Recent examples distinguish an unsupported promise from a complete answer and group two arrival complaints. Errors remain: review polarity/translation, fact-check rationale and grader instability. These examples are not a held-out accuracy result or proof of learning. |
| Clarity, design, inclusivity and AI value | 12 / 15 | Simpler navigation, homepage purpose and tab guide, prominent facts, criterion-level feedback, bilingual model interactions and a concrete review-to-practice workflow. Navigation is still largely English; no low-fluency usability test. Latest home HTML/JS and delivery checked; visual browser verification remains unavailable. |
| Scale, replication and next steps | 6 / 10 | Local source package, bounded workflows and inspectable memory. Installation requires Python/Ollama and separate weights. No tested distribution through a cooperative, phone deployment, support model or replication pilot. |
| Responsible AI | Conditional pass candidate | Consent before saving learning evidence, uncertainty, source quotes, synthetic labels and no automated customer messages. Need clear data/license documentation and an honest account of residual confident mistakes. This is not a guaranteed judge pass. |

Do not add points for phone compatibility merely because a quantized model looks small. The brief asks whether the tool works within user constraints. A desktop demonstration can be presented honestly with a phone deployment plan, but leaves an evidence gap.

## What improved

- Multiple distinct review texts can yield a shared, quote-backed communication issue.
- The operator chooses whether to turn that issue into practice; a new visitor opening is generated while exercise facts remain separate.
- Locally saved accepted assessments inform recommendations and visible learner memory. This is contextual personalization, not model-weight learning.
- The guest simulator sees conversation history rather than hidden exercise answers.
- Facts and tone are treated separately; an uncertain assessment is distinguished from learner failure.
- Full marks no longer generate a contradictory corrective instruction.
- Home explains the purpose and tabs with an original inline illustration and no network assets.

## Strongest positioning

“guest-i-mate helps a small tourism operator turn guest feedback into their next practice conversation, in their language and without an Internet connection after setup.”

Lead with learning from visitor feedback, a workflow explicitly named in Annex C. Training is how the operator acts on that insight. Avoid positioning this as a generic chatbot, an autonomous business manager, or a proven income intervention.

## Highest-value submission work, in order

1. **Freeze one complete demonstration.** Three labeled synthetic reviews → two quote-linked arrival complaints → operator-approved rehearsal → a concrete reply → clear feedback → saved evidence visible in Progress. Include one physical problem that the system does not pretend to fix with training. Show a real generated run; label edited or pre-recorded sequences and do not imply instantaneous inference.
2. **Evaluate frozen, unseen inputs.** Use the reserved cases and new reviews covering mixed praise/complaint, conflicting views, ambiguous timing and unsupported promises. Predefine expected facts/actions before running. Report missed issues, unsupported claims, correct source quotes, language errors, abstentions and latency separately. Use an independent assessor where possible; do not use the coach's own scores as proof it is accurate. Keep failures in the report.
3. **Compare against a useful simple baseline.** Give a phrasebook/checklist the same facts and reviews. Show where it already suffices, and where review aggregation, a contextual follow-up or personalized practice adds value. Compare task results under equal conditions; no invented percentage improvement.
4. **Make personalization visible.** Save feedback from a first attempt, show its source in memory, then show how the next recommendation changes. Explain that preferences and accepted evidence are stored locally and can be withdrawn. Do not call a stored preference fine-tuning or infer a fixed learning style.
5. **Close documentation and deployment gaps.** Consolidate data sources, dates, countries, licenses, sizes and coverage limits; report the exact model/quantization and provisioning footprint. Re-run Internet-blocked verification for the current 4B workflow. If a phone is unavailable, disclose that and give a concrete target-device test plan rather than claiming it was measured.
6. **Package the required submission.** Working code or accessible prototype link plus a 2–5 minute video; the brief says missing video excludes shortlisting. Verify portal-specific cutoff and fields. A localhost URL is only usable on the developer's machine. A source download is a code submission, not a hosted live demo.

## Suggested four-minute video

- 0:00–0:30: Published Colombian operator context, clearly attributed; explain that demo messages are synthetic.
- 0:30–0:50: One-sentence outcome and why a phrasebook alone cannot analyze scattered reviews and adapt practice.
- 0:50–2:30: The review-to-practice workflow; show the operator making the final decision.
- 2:30–3:10: Saved evidence, personalization and an uncertain/operational case handled honestly.
- 3:10–3:40: Measured evaluation, actual model/runtime and offline evidence with its scope.
- 3:40–4:00: Localization, remaining limitations and next validation/deployment step.

No score or strategy guarantees a win. The most valuable improvement now is a credible evidence package around one working loop, rather than expanding into more features.
