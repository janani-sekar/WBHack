# AI backend API (desktop development harness)

Run `python3 -m hospitality.server` (optionally `--constrained` for a limited-resource desktop profile); listens only on 127.0.0.1:8765. Python standard library only. Ollama must be running with qwen3:1.7b already downloaded. No fake model fallback exists in the server. Tests use explicit fixtures only.

The service is serial and intended for one local developer. It is not an internet-facing service. Browser Origin requests are currently rejected; frontend integration is future work. Do not bind it to a public interface. Local data is not encrypted by the app; SQLite permissions are restricted where supported. Shared-device profile isolation is not yet implemented.

## Endpoints
| Method / path | JSON body or response |
|---|---|
| GET /health | Runtime, installed model, desktop/Android verification status |
| GET /scenarios | Three scenario families |
| GET /profile | Confirmed business facts; defaults are labeled synthetic |
| POST /profile | `name`, `facts` object, `approved: true` |
| POST /sessions | `skill`: duration/directions/expectations, optional `independent` boolean or `lesson_id` |
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
