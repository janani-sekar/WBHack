# AI backend API (desktop development harness)

Run `python3 -m hospitality.server` (optionally `--constrained` for a limited-resource desktop profile); listens only on 127.0.0.1:8765. Python standard library only. Ollama must be running with the selected model already downloaded: qwen3:1.7b (default), or `--model qwen3:4b-instruct`. No fake model fallback exists in the server. Tests use explicit fixtures only.

The service is serial and intended for one local developer. It is not an internet-facing service. The local testing UI is served at GET /. Same-origin browser requests are accepted; other origins and cross-site requests are rejected. Do not bind it to a public interface. Local data is not encrypted by the app; SQLite permissions are restricted where supported. Shared-device profile isolation is not yet implemented.

## Endpoints
| Method / path | JSON body or response |
|---|---|
| POST /agent | `request`, optional `review`; bounded LangGraph run (requires agent dependencies) |
| GET /health | Runtime, installed model, desktop/Android verification status |
| GET /curriculum | 18 synthetic teaching cases, exercise facts and rubrics; excludes reference answers and reserved cases |
| GET /learning-settings | Saved guest language and message style |
| POST /learning-settings | `guest_language`: en/es, `style`: clear/chat, `approved: true` |
| POST /sessions/{id}/language | `language`: en/es; translates the current message without advancing the conversation |
| GET /learning-plan | Accepted-session evidence, weak criteria, delayed-retention heuristic and recommended case |
| GET /scenarios | Three scenario families |
| GET /profile | Confirmed business facts; defaults are labeled synthetic |
| POST /profile | `name`, `facts` object, `approved: true` |
| POST /sessions | `skill`: duration/directions/expectations, optional `independent` boolean, `lesson_id`, or `scenario_id`; optional `guest_language`: en/es and `style`: clear/chat |
| GET /sessions/{id} | Current question, attempts, assessments |
| POST /sessions/{id}/responses | `response`: operator text; yields pending assessment |
| POST /sessions/{id}/next | `{}`; generate next guest turn after a response |
| POST /sessions/{id}/assessment | `turn_id`, `approved`: true/false; recomputes progress |
| GET /progress | Approved evidence, practice states, next suggested skill |
| POST /reviews | `review`: source text; Interpretation in the profile language and exact source quotes |
| GET /reviews | Locally stored reviews |
| POST /lessons | `review_id`, `theme_index` (zero-based), `approved: true` |
| GET /lessons | Approved review-based training topics |
| GET /memory | Inspect approved phrases |
| POST /memory | `original`, `preferred`, `context`, `approved: true` |
| DELETE /memory/{id} | Remove a phrase from future prompts |
| POST /reset | `approved: true`; remove sessions, reviews, lessons and memory, preserve profile |

All POST requests require application/json and at most 16 KB. No upload, messaging or cloud endpoint is implemented. Errors: 400 invalid input, 404 missing record, 503 unavailable/invalid model output. A failed model result does not become saved progress.

## Example workflow
```sh
curl -s http://127.0.0.1:8765/sessions \
  -H 'Content-Type: application/json' \
  -d '{"skill":"duration"}'
# Copy the returned session id into SESSION_ID below.
curl -s http://127.0.0.1:8765/sessions/SESSION_ID/responses \
  -H 'Content-Type: application/json' \
  -d '{"response":"காபி சுவைத்தல் 20 நிமிடங்கள் ஆகும். நீங்கள் எப்போது தொடங்க விரும்புகிறீர்கள்?"}'
curl -s http://127.0.0.1:8765/reviews \
  -H 'Content-Type: application/json' \
  -d '{"review":"We loved the coffee tasting, but the directions were confusing."}'
```

The example is synthetic; language quality is evaluated automatically for this prototype. Assessment approval is an operator decision; do not automatically approve model judgments. An exact evidence quote validates traceability, not the correctness of the interpretation.

Memory deletion removes future preference retrieval but does not erase historical session text; reset removes all learning/review records. SQLite deletion is logical, not guaranteed forensic erasure. No model weights are updated.

Profile updates optionally accept `language`: `es` (default), `ta`, or `hi`. Localized output fields now use `_local`, not `_ta`. The prototype defaults to Spanish. Language codes are experimental, not a promise of quality.

### Coaching flow and learner memory

- `GET /learner-memory`: locally cached preferences and tentative observations with source turn IDs. Rebuilt from accepted evidence on read.
- `POST /sessions/{id}/coach-language`, `{"language":"en"}` or `es`: cached feedback translation, preserving original scores, evidence and approvals.
- `POST /sessions/{id}/finish`, `{}`: finish after at least one response, return a provisional aggregate recap. Further replies/follow-ups are rejected; assessment approval and translation remain possible.
- `POST /learning-settings` additionally accepts `coach_language` (en/es) and `support` (brief/example). Omitted fields preserve existing preferences. Session coaching-language changes do not alter the saved default.

### Reviews into practice

- `POST /review-batches`: `reviews` (1–5 strings, each <=1,800 characters), optional `language` (`en`/`es`). Translates each distinct text and groups quote-linked observations. Duplicate normalized texts do not inflate counts. Counts are review texts, not verified people.
- `GET /review-batches`: locally saved batches, including originals and interpretations.
- `POST /batch-lessons`: `batch_id`, `group_id`, `approved: true`. Only a non-uncertain communication finding can become a lesson. The lesson preserves sources and selects a specific scenario.
- `POST /sessions` with `lesson_id` starts that scenario with a generated opening about the approved issue. Exercise facts remain synthetic and unchanged.
- `POST /practice-drafts`: `session_id`, optional `language` (`en`/`es`). Finished sessions only. Creates editable notes from allowlisted exercise facts; does not infer the operator's real business details. Labels are translated; free-text fact values retain their original language.
- `POST /drafts/{id}`: `text`, `approved: true` saves the operator's edits locally. It does not send anything or update the business profile.
- `GET /drafts`: saved notes.

Recaps exclude uncertain and rejected assessments. Rejecting an assessment after finishing recomputes the saved recap. Pending, non-uncertain feedback may appear provisionally in a recap but never enters learning progress without approval.

Reply records include `assessment_issue` (`checks_disagree`, `facts_unclear`, `interpretation_unclear`, or null). This describes an AI assessment problem, not learner ability. The UI distinguishes an unscored reply from “Needs practice,” “Almost there,” and “Handled well,” with visible criterion results. An unscored recap preserves the conversation and offers exercise goals and a retry; it does not assign a failing grade. The fact-check receives the actual guest question and prior dialogue. Existing assessments are not retroactively regraded. Full marks produce a transfer-practice suggestion rather than an invented correction.

Known limits: exact quotes prove traceability, not correct sentiment or translation. Contradictory action/polarity labels are flagged uncertain. This cannot detect every semantic error. Review observations require human checking before practice. No scraping, review-platform access, or identity verification is implemented.
