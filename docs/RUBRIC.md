# Task-specific learner rubric

Version 1.1. Original prototype rubric informed by the research register; not an official ILO/World Bank credential.

The previous generic score encouraged plausible-sounding replies. Each new scenario now supplies its own required actions and 0/1/2 anchors to the assessor. New assessments use five dimensions; historical records without tone keep it unassessed:

| Dimension | 0 | 1 | 2 |
|---|---|---|---|
| Address the request | Misses or reverses the main need | Handles only part | Handles the scenario's stated need |
| Accuracy and limits | Contradiction, invented promise or unsupported assurance | No contradiction, but relevant limit omitted | Verified facts and relevant limits preserved |
| Clarification | Guesses a missing detail or ignores a needed question | Notices gap without useful clarification/check | Resolves the scenario's information gap appropriately |
| Next step | None, or unauthorized commitment | Vague action | Concrete permitted action, check or completion |
| Customer-facing tone | Mocking, dismissive, blaming, insulting, or narrates an answer instead of addressing the guest | Abrupt, impersonal, or contextually awkward wording | Respectful, natural, attentive reply suitable for the situation |

These are general descriptions; `practice.json` contains the task-specific anchors. No score for accent, personality, deference, native-like grammar or stereotyped cultural behavior. Clear meaning matters; confidence does not compensate for wrong facts.

Example: “Can four of us come tomorrow afternoon, and how much is it?” Availability and price are unknown. A strong response asks for date/time, states what needs checking and keeps the booking unconfirmed. A fluent “You are booked at 3 for ten dollars each” must fail accuracy. Conversely, a concise honest response need not be artificially lengthened to earn points.

Critical errors include fabricated bookings/prices, unverified ingredient/accessibility guarantees, blanket consent for other people's photos and invented refunds. A second, narrow model call checks factual support against exercise facts. An unsupported verdict overrides accuracy to 0 and replaces misleading praise; an uncertain verdict blocks progress credit. This uses the same local Qwen model, so it is not an independent evaluator and detection remains fallible. This is NOT yet a deterministic safety classifier. Low accuracy prevents retained-learning status, but operator acceptance alone does not make a bad score reliable.

Every assessment is pending until accepted or rejected. Exact quote validation checks traceability, not semantic correctness. One quote for the overall response is still a limitation: the next evaluator revision should return evidence per dimension and a separate missing-evidence status, while keeping missing text from becoming a fabricated quote.

Feedback must describe the learner's actual response, give at most one useful change and avoid inventing a service. If the response is already sufficient, move to a new task rather than inventing an error. This requirement is in the new prompt and measured by development cases; it is not assumed achieved.

Reviews need a separate rubric: preserve polarity, names/numbers and uncertainty; link themes to exact source spans; count independent reviews correctly; distinguish one suggestion from a pattern; separate infrastructure failures from communication gaps. Eight bundles cover praise+criticism, broken facilities, sarcasm, disagreement, prompt injection, sparse evidence, unrelated businesses and private contact information. Multi-review aggregation is test data for the next implementation stage; the current review endpoint still handles one review.

Tone is scored separately from factual truth. Brief/informal replies can receive full credit; prescribed honorifics and apologies are not required. When tone falls short, coaching should name the wording issue and offer a guest-facing rewrite. Independent evidence now requires at least 9/10, factual accuracy 2 and tone at least 1; this remains a prototype heuristic. Historical records are not retroactively assigned tone points.

The lunch regression exposed disagreement between the main grader and fact verifier. When the main grader assigns accuracy 0 but the verifier says supported, the assessment is now marked uncertain and cannot count toward progress. `evaluation/tone-fact-separation.json` records the raw development disagreement before this deterministic safeguard, not a passing end-to-end semantic result.
