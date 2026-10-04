# Practice, transfer and personalization

## Evidence and its limits

[J-PAL's vocational training synthesis](https://www.povertyactionlab.org/policy-insight/vocational-and-skills-training-programs-improve-labor-market-outcomes) finds that program design and complements matter: practical experience, relevant skills and support can improve outcomes, but training is not uniformly effective. It does not establish that chatbot practice raises tourism income.

[IES's instruction and study practice guide](https://ies.ed.gov/ncee/wwc/PracticeGuide/1) recommends spaced learning, retrieval, combining examples with problem solving, and explanatory questions. This is educational evidence, not a hospitality chatbot trial. Apply it as a design hypothesis and evaluate delayed transfer.

[ILO tourism competency standards](https://www.ilo.org/sites/default/files/wcmsp5/groups/public/@asia/@ro-bangkok/documents/publication/wcms_bk_pb_233_en.pdf) emphasize observable workplace performance, coaching and practical demonstration. Standards specify useful competencies; they are not causal evidence of this app's effects.

## Resulting practice design

- A three-reply conversation rather than disconnected quiz questions. The guest receives earlier replies and reacts to clarification or a proposed next step.
- Bounded variation: missing group size, changed preferences, time pressure, or a misunderstanding. A guest can introduce a preference but cannot invent a business's price, availability or policy.
- Optional feedback after each response; the latest feedback opens at the end. Keeping feedback collapsed is a cognitive-load design choice, not a proven superior timing schedule.
- Clear and everyday-message versions. Everyday messages include incomplete wording and punctuation, without imitating ethnic accents or framing a dialect as a deficit.
- English/Spanish guest selection. Authored opening variants; model translation for generated follow-ups. Translation is experimental and may alter nuance. No claim of authentic regional dialect support.
- An independent first attempt and later different situations. Existing spacing is a prototype rule, not calibrated mastery or certification.

A future trial should compare fixed phrasebook practice, single-turn feedback and multi-turn practice under equal time. Assess new scenarios after a delay with an independent rater; the model's own score is not a learning outcome. Separate factual reliability, communication skill and language quality.

## Personalization that can be demonstrated now

1. Save Spanish + everyday messages in Practice settings. A new session uses those choices; they survive a server restart.
2. Accept or reject feedback. Only accepted, non-uncertain evidence affects the learning plan; recommendations rotate to less-practised situations and show the weaker rubric dimension.
3. Approve a relevant review theme. A related skill becomes a practice suggestion, with provenance back to that review.

Language settings, local history and approved lesson selection are personalization. They are not model-weight training or a fixed learning-style diagnosis. Legacy vocabulary memory remains available in the backend, but the phrasebook tab and phrase-saving workflow have been removed from the main UI at the user's request. Existing saved phrases have not been deleted.

## Model choice

[Ollama's qwen3:4b-instruct](https://ollama.com/library/qwen3:4b-instruct) is a 4.02B Q4_K_M model with roughly 2.5 GB weights; [Qwen's model card](https://huggingface.co/Qwen/Qwen3-4B-Instruct-2507) describes the instruction-tuned model. Weight size is not peak RAM: context, runtime and the OS add overhead. It is a candidate for adequately provisioned devices, not a verified low-end Android solution. The 1.7B model remains selectable. Compare actual outputs and latency before changing the default; do not infer domain quality from generic benchmarks.

## Measured prototype limitations (3 October 2026)

A synthetic three-exchange walkthrough on the local 4B model produced visitor-role follow-ups that changed timing and group size, and readable Spanish review translation. It still falsely treated a clarification question as an unsupported claim and mislabeled a directions complaint as positive. These are unresolved assessment-quality defects, not passing semantic results. Raw before/after outputs are in `evaluation/conversation-4b-baseline.json` and `evaluation/conversation-4b-revised.json`; this is a development example, not a held-out benchmark. Keep scores provisional and operator-approved.

The demo was launched explicitly with `--model qwen3:4b-instruct`; the CLI default stays 1.7B. The existing network-denied report covers the earlier 1.7B run, not a new 4B measurement. Neither model has been measured on an Android device.

## Feedback flow and memory update

The UI now names each interim assessment as an optional tip. After reply three it calls `/sessions/{id}/finish` and shows a separate conversation recap. Learners can finish early after at least one reply. The recap aggregates existing reply scores, selects feedback examples, and is labeled provisional; it is not a second holistic model assessment. Finishing never silently approves feedback. Full-score recaps suggest transfer practice rather than manufacture a gap.

English/Spanish coach language is independent of guest language. A session switch translates the explanation fields and caches both versions. Original evidence, grades and approval states remain unchanged. New-session defaults and brief/example feedback preferences are stored in SQLite. Review translation still follows the business-profile language; the session toggle applies to practice coaching only.

`Coach.learner_memory()` rebuilds and persists a compact memory from preferences and accepted, non-uncertain assessments. The LangGraph retrieval node receives it through `Coach.context()`, as does the coaching prompt. Each observation includes source session/turn IDs, quotes, and timestamps. The UI exposes this under Progress → What the coach remembers; retracting evidence removes it from future memory. Retraction preserves historical records. This is custom local SQLite memory integrated into LangGraph, not LangMem, vector memory, or weight fine-tuning. The current shared-profile demo is not multi-user.

Observed means identify tentative focus areas; all-full-score observations have no specific gap. Recommendation scheduling remains the existing transparent rule: due/less-practised skills, less-repeated cases, and delayed independent evidence. It does not statistically infer which learning method works best. A user chooses brief feedback versus examples, and the prompt uses that choice. Source quotes and recency are visible; diagnostic labels about intelligence, personality or learning style are not inferred.

## Retrieval scope and next evaluation

`hospitality/knowledge.py` contains three short reviewed paraphrases linked to ILO Tourism RMCS sections C8 and C10. It filters by skill, ranks eligible cards by lexical overlap, and supplies general teaching context to assessment. With one card per skill this is a minimal retrieval baseline, not a mature document-search system. There are no runtime web requests or embeddings; references can be inspected in feedback. Opening the external source requires connectivity.

Expand only with reviewed, appropriately licensed training material: chunk by competency; retain publisher, section, date, locale and version; retrieve by skill and language; optionally add multilingual embeddings when a larger corpus justifies them. Keep verified business facts separate. Evaluate source relevance, factual conflicts, unsupported advice, multilingual retrieval and no-match abstention against the same cases without retrieval. Retrieval is useful grounding, not proof of assessment quality.

## Audio recommendation (not implemented)

Start with local playback of guest messages and push-to-talk transcription. Let the learner inspect/edit the transcript before submitting it. Use downloaded speech models with [Sherpa-ONNX Android support](https://k2-fsa.github.io/sherpa/onnx/android/index.html); measure size, latency, language coverage and noise robustness before selecting models. Ordinary browser speech services should not be assumed offline. Do not grade accent or fluency from an unverified transcript. This remains proposed work, not a hidden microphone feature.

## Demo sequence

1. Save Spanish coaching and explanations with examples; keep English guest messages.
2. Complete three replies, inspect the recap, switch the coach language, and show unchanged assessment values.
3. Save feedback you agree with. Open Progress → What the coach remembers to show the quote and tentative focus.
4. Start another practice; the coaching context includes the remembered preferences and accepted observations.
5. Retract an inaccurate assessment from the memory panel and show its removal. A relevant approved guest-review lesson can also open practice of that skill.

Software tests establish persistence, provenance, retraction and score preservation. The single real-model smoke example in `evaluation/feedback-memory-smoke.json` is developmental evidence only: translation wording and advice still need review, and no learning-effectiveness claim is made.

## Guest perspective repair

The simulated visitor now receives only the visible conversation and message style. Exercise facts, grading rubrics, model feedback and learner memory are withheld. This prevents answer-key leakage such as asserting that lunch is excluded immediately after the operator says it is included. The visitor should ask about contradictory operator statements and preserve its own stated constraints. Informal style no longer requires a new misunderstanding or change of mind each turn. This reduces a demonstrated failure mode; it does not guarantee consistency. Synthetic development outputs are recorded in `evaluation/guest-tone-development.json`.

The practice UI puts example facts in a full-width highlighted panel above the conversation. Support options and progress are collapsed by default. Tone is a fifth assessment dimension and contributes to tentative learning focus; older records retain their original four scores.
