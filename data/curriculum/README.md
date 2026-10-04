# Original synthetic hospitality curriculum

- `practice.json`: 18 teaching scenarios, facts, unknowns, anchored rubrics, critical errors, follow-ups, and author-drafted Spanish reference/weak responses.
- `holdout.json`: six reserved evaluation scenarios with changed facts and expected behavior. Not imported by the runtime or shown in the UI. They are public, so this is a data partition, not a secured benchmark.
- `review_cases.json`: eight bundles for testing translation, polarity, aggregation, operational-vs-training decisions and instruction-like input.

Every dialogue is newly authored synthetic material, not a scraped review or interview transcript. The IDs WB-BRIEF, ILO-RMCS and SUSTOUR-HOPS refer to conceptual design sources in `docs/TRAINING_RESEARCH.md`, not copied wording, endorsements or proof of observed demand. No source document's license is being extended to this pack. Do not represent this as institution-certified training, or as a validated Spanish translation corpus.

Difficulty levels are author judgments. Cases involving ingredients, access and weather test honest communication and escalation, not professional safety advice. Changing scenario facts in the exercise never changes the business profile. The app omits reference/weak responses and reserved cases from the teaching endpoint.

The training fixtures do not prove effectiveness. Native-speaker/operator validation is deferred for this prototype per the user's instruction. Country/community suitability remains unestablished.
