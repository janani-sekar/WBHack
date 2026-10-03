# Evaluation plan

Status: no study has been run and no metrics are claimed. Use evaluation/results-template.csv for measurements.

## A. Does the operator learn?
Use a static phrasebook/lesson as the baseline, with comparable content and practice time. If possible recruit 3–5 consenting operators for a formative pilot; disclose convenience sampling and small sample size. If only teammates test, say so and do not describe it as operator validation.

Give equivalent but different pre/post scenarios, counterbalance scenario order where feasible, and keep post-test scenarios unseen during practice. Have a local-language reviewer score without knowing the condition where practical.

Rubric, 0–2 each (absent, partial, complete): answer the guest's request; state accurate relevant facts; clarify unknowns; give an appropriate next step. Total 0–8. Separately record factual errors, needed assistance, completion time, and learner self-reported confidence. Confidence alone is not skill evidence.

Record short-term improvement honestly; no claim of long-term retention or revenue impact without follow-up.

## B. Does personalization work?
Test before correction, after approval, after restart, and after deletion. Use held-out paraphrases and separate retention cases. Compare frozen baseline, memory-only personalization, and updated classifier if built. Report sample counts and all failures; improvements on the memorized sentence alone are not generalization.

## C. Does it work offline on the actual device?
Record full device/model manifest. Complete at least 10 varied interactions after airplane-mode restart. Measure cold start, model/app size, feedback latency, peak RAM, and failures. Ten cases establish a small demo check, not population-level reliability.

## D. Does it fail appropriately?
Test contradictory business facts, unclear language, unknown dialect words, an incorrect user correction, a guest request outside available services, and review text that says 'ignore your rules'. It should clarify, decline unsupported feedback, or use a reviewed fallback. No made-up price, duration, or availability.

## Data discipline
Maintain separate training, validation, and final test records. Keep participant-derived data private and out of Git. Public examples must be consented/de-identified or synthetic and labeled. Document language coverage, translations and reviewer status, sources/licenses, and exclusions.
