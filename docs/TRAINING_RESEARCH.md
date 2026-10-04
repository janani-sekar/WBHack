# Hospitality training: evidence and design decisions

Research date: 3 October 2026. This document distinguishes sector relevance, training standards, causal evidence, and engineering recommendations. It is not an evaluation of our product's effectiveness. Colombia is the selected prototype context; see the [attributed operator story and evidence boundaries](COLOMBIA_CONTEXT.md). Spanish support and a published story do not establish validation with that community.

## What the World Bank brief actually prioritizes

The supplied concept note (pages 5–7 and Tourism Annex C, printed pages 19–20) describes a small operator managing informal visitor interest, language barriers and feedback. Its strongest AI opportunity is interpreting what visitors valued and what they wanted changed. A training product should close a business loop: visitor difficulty → focused rehearsal → more reliable response → check whether the experience improved. A listing or phrasebook is a necessary comparison, not an inferior alternative by assumption.

Noor and Ondera are fictional. We should not present invented farm questions as observed customer research. The new cases are explicit synthetic approximations of plausible business interactions. The original PDF stays local and is not redistributed.

## Source register and what each source supports

| ID / source | Evidence type and scope | Implication for our prototype |
|---|---|---|
| WB-BRIEF: supplied World Bank concept note, 2026 | Design brief, not causal evaluation | Link training to enquiries and feedback; preserve offline and human-decision constraints. |
| [ILO-RMCS: Regional Model Competency Standard, Tourism Industry](https://www.ilo.org/sites/default/files/wcmsp5/groups/public/@asia/@ro-bangkok/documents/publication/wcms_bk_pb_233_en.pdf) | Occupational competency framework. Units G1/G2 cover coaching and workplace training; G5 covers assessment. These are standards, not an RCT. | Use observable tasks, scenario-specific evidence and constructive retry feedback. Adapt the standards to the work context; simulated performance is not occupational certification. |
| [SUSTOUR-HOPS: Swisscontact, Indonesia](https://www.swisscontact.org/en/projects/sustour/competitiveness-and-market-access/hospitality-practice-on-sustainability-hops) | Provider description of training/coaching, not independent impact evidence | Training leads to a business action plan and subsequent coaching. Our review flow should produce a proposed improvement and later follow-up, not just sentiment. |
| [J-PAL vocational/skills synthesis](https://www.povertyactionlab.org/policy-insight/vocational-and-skills-training-programs-improve-labor-market-outcomes) | Synthesis of 28 randomized evaluations across settings | Practical experience, employer demand, certification and complementary support matter. An app cannot substitute for placements, finance, transport or functioning infrastructure. |
| [J-PAL / BRAC Uganda training evaluation](https://www.povertyactionlab.org/evaluation/effects-subsidized-trainings-young-workers-and-small-firms-evidence-uganda) | RCT: 1,714 workers, 1,538 SMEs; vocational versus firm training, 2012–17 | Both approaches built skills; vocational training gains were more sustained. This supports taking practice and assessment seriously, not a claim that a brief AI chat raises wages. |
| [J-PAL personalized instruction, Rajasthan](https://www.povertyactionlab.org/evaluation/impacts-computer-based-individualized-instruction-math-learning-india-0) | RCT: 1,528 grade 6–8 students, 2017; mathematics, not adult hospitality | Personalization helped lower-performing students, with no average learning effect. Adapt to demonstrated gaps and test the adaptation; do not assume every learner benefits. |
| [Pashler et al., Learning Styles](https://doi.org/10.1111/j.1539-6053.2009.01038.x) | Evidence review, 2008 | Fixed learning-style matching lacks an adequate evidentiary basis. Offer preferences and accessibility choices without diagnosing visual/auditory learner types. |
| [Dunlosky et al., effective learning techniques](https://www.psychologicalscience.org/publications/journals/pspi/learning-techniques.html) | Review, 2013; broad educational evidence | Practice testing and distributed practice support active recall and delayed rehearsal. Exact app intervals and adult tourism transfer remain design hypotheses. |
| [World Bank Indonesia tourism program](https://www.worldbank.org/en/news/feature/2025/03/19/indonesia-integrated-tourism-improving-livelihoods-for-thousands-in-lake-toba-and-lombok) | Program reporting and participant stories, 2025 | Training sits alongside infrastructure and market access. Use realistic local-business workflows; do not attribute program outcomes to AI. |
| [ILO rural community tourism, Bolivia](https://www.ilo.org/resource/news/community-based-rural-tourism-programme-new-methodology-boost-productive) | Training-of-trainers program description, 2023 | A relevant Spanish-language community-tourism context for later localization; not proof our Spanish prototype is suitable there. |

## Requested organizations: what could and could not be verified

The exact [Srishti Foundation website](https://srishti-foundation.org/) describes education, livelihood and empowerment work. I did not find a verifiable hospitality curriculum or evaluated tourism-training intervention on that site. It should not be cited as the source of our rubrics. Similarly named organizations cannot safely be treated as the same institution.

I could not identify a hospitality-training organization under the exact name “Indonesia Development Initiative.” A link has been requested. Swisscontact's SUSTOUR/HOPS and the World Bank's Indonesia tourism work are verified relevant alternatives; neither is claimed to be that organization.

## Curriculum design

The new pack contains 18 teaching cases, six reserved evaluation cases, and eight review bundles. All text and facts are newly authored synthetic fixtures. They cover customer needs across the journey:

- Before arrival: date ambiguity, price uncertainty, discounts, dietary requirements, access needs and time limits.
- Arrival: maps unavailable, wrong entrance, signal loss, split groups and late arrival.
- Experience: activity inclusions, pace, weather changes and consent for photography.
- Afterward: complaints, lost property, return visits and product suggestions.

Cases include a customer utterance, fixed facts, unknowns, follow-up prompt, difficulty level, specific required actions, critical errors, Spanish reference response and a weak response. Reference responses are author drafts, not a translation benchmark. The live teaching endpoint excludes reference and weak answers; the model receives only the current scenario, facts and rubric. Reserved evaluation content is never imported by the runtime.

The pack deliberately includes uncertainty. A good operator does not always know the price, calendar, route, ingredients or refund policy. Good performance includes saying what needs checking and agreeing on the next step. The model should not punish an honest limitation or invent a new service as coaching advice.

## Proposed learning loop

1. Give one short task without showing a full answer.
2. Assess against the actual customer request and exercise facts.
3. Show one supported strength and one correction, or acknowledge a sufficient response.
4. Let the learner retry; record whether help was visible.
5. Later, use a different situation testing the same skill without hints.
6. Revisit after a delay and link the lesson to a real business action when relevant.

This is our design synthesis from the sources, not a curriculum copied from an institution. Short sessions are a practical fit to constrained device access, not an evidence-based universal duration.

## Personalization: implemented versus proposed

Implemented in the desktop prototype:
- Approved vocabulary preferences remain local and editable/deletable.
- Rich cases isolate exercise facts from the operator's business profile.
- Recommendations rotate among less-repeated introductory cases within a selected skill.
- The learning plan computes criterion averages from one latest accepted response per session.
- First responses in different independent cases and a >=24-hour gap are required for `retained_in_simulation` status.
- Rejected or uncertain feedback is excluded. Provisional review intervals are one or three days.

Not yet implemented: reliable diagnostic placement, automated selection of micro-lessons for each error type, format adaptation, audio, robust difficulty calibration, live workplace outcome tracking or model-weight training. The current scheduler reports the weakest criterion but does not yet select a distinct drill optimized for that criterion. Levels above the introductory stage can be selected manually; the heuristic is intentionally limited.

Next learner preferences should be language, reading complexity, explanation length, time available and chosen support level. These are user controls, not inferred intelligence or learning styles. Guidance should fade after demonstrated success and return after failures. Evaluate this against a simple fixed sequence before claiming personalization helps.

## How to evaluate actual learning later

For the prototype, author-defined cases and automated checks are enough to explore feasibility; a fluent-speaker study is not a blocking requirement. For effectiveness claims, use a pretest, practice and delayed test with new situations. Compare a phrasebook, fixed exercises and adaptive coaching under equal time. Score task completion, unsupported promises, clarification and successful next steps separately. Record assistance, completion time and dropout without rewarding speed alone.

The app's own score is not an independent outcome. A future study should use a separate assessor and real workflow observations, with appropriate consent. Report language quality, learning transfer and business outcomes separately; neither a high chat score nor more sessions proves increased income.
