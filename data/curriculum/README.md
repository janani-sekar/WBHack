# Original synthetic hospitality curriculum

- `practice.json`: 18 teaching scenarios, facts, unknowns, anchored rubrics, critical errors, follow-ups, and author-drafted Spanish reference/weak responses.
- `holdout.json`: six reserved evaluation scenarios with changed facts and expected behavior. Not imported by the runtime or shown in the UI. They are public, so this is a data partition, not a secured benchmark.
- `review_cases.json`: eight bundles for testing translation, polarity, aggregation, operational-vs-training decisions and instruction-like input.

Every dialogue is newly authored synthetic material, not a scraped review or interview transcript. The IDs WB-BRIEF, ILO-RMCS and SUSTOUR-HOPS refer to conceptual design sources in `docs/TRAINING_RESEARCH.md`, not copied wording, endorsements or proof of observed demand. No source document's license is being extended to this pack. Do not represent this as institution-certified training, or as a validated Spanish translation corpus.

Difficulty levels are author judgments. Cases involving ingredients, access and weather test honest communication and escalation, not professional safety advice. Changing scenario facts in the exercise never changes the business profile. The app omits reference/weak responses and reserved cases from the teaching endpoint.

The training fixtures do not prove effectiveness. Native-speaker/operator validation is deferred for this prototype per the user's instruction. Country/community suitability remains unestablished.

## Situation completeness review

All 19 practice situations were reviewed for usable facts, a concrete next step, question/translation consistency, and aligned grading criteria. Prices, schedules, and policies are fictional exercise data, not claims about a Colombian business. Unknowns are intentional only when learners can ask the guest or consult a named role; bare unknown placeholders have been removed. Every case includes a customer-tone rubric. Pricing and booking reference replies were also checked with local Qwen; see `evaluation/situation-audit-development.json` for development results, not an independent quality benchmark. Existing saved sessions retain their original facts; start a new session to use the revised curriculum.
