# guest-i-mate: Hack-Nation submission pack

Follows the final submission checklist: short description, 60-second demo video, 60-second tech video. Read only blockquotes aloud (~140 words per minute, so each video script is ~130–145 words). Demo footage = the Android app, screen-shared from the phone. Every review, guest and farm in the demo is synthetic; say so on screen. Longer narrative and sources: [VIDEO_BRIEF.md](VIDEO_BRIEF.md).

## 1. Short description (≈220 words)

guest-i-mate is a Small AI coach for people who run small tourism businesses, starting with Colombian coffee farms that host visitors. Tourism earned Colombia US$6.78 billion in 2019, 13.2% of its exports, but only 41.9% of rural households have Internet at home. Small hosts often know guests left happy without knowing what to improve, and human-led hospitality training is not available between visits.

guest-i-mate turns a host's own guest reviews into practice. The host pastes reviews, and a local Qwen model groups them into what guests valued and what caused trouble, keeping the original quotes visible. The host approves a lesson, then rehearses with a simulated guest who asks follow-ups based on what the host actually said. Feedback covers five criteria (answers the request, correct facts, handles unknowns, gives a next step, respectful tone) in Spanish, with English or Spanish guests.

The host stays in control. Nothing counts as progress until they accept it, uncertain grades are shown as unscored rather than guessed, and the app never messages customers. The model runs locally with no cloud fallback, and a fixed practice guide still works when the model is unavailable.

The Android app (Kotlin/Compose) talks to a local Python + LangGraph + Ollama backend; Qwen3 is Apache-2.0, 1.4–2.5 GB. Curriculum is 19 synthetic situations designed from ILO hospitality competency standards.

## 2. Demo video (max 60 s)

| Time | Show (phone screen-share) | Say |
|---|---|---|
| 0:00–0:08 | Title card: “guest-i-mate · Minca, Colombia”. Stat: “41.9% of rural households have Internet” (DANE 2024). | > Small farm hosts in rural Colombia get guest reviews, but no one helps them turn those reviews into better conversations, and most have no reliable Internet. |
| 0:08–0:22 | Reviews tab → three synthetic reviews → Explain in Spanish → themes with quotes. Caption “Synthetic reviews”. | > guest-i-mate reads the host's reviews on their phone, locally. Guests loved the coffee tasting, but two couldn't find the entrance, and the original quotes stay visible. |
| 0:22–0:42 | Approve arrival lesson → Practice → guest asks how to find the farm → host types reply → five-criteria feedback. Caption “Processing time shortened”. | > The host approves that as a lesson and rehearses it. The guest asks a follow-up based on what the host actually said, and the coach checks facts, clarity, a next step and tone, in Spanish. |
| 0:42–0:52 | Accept feedback → Progress tab → what the coach remembers. Optional: an “unscored” card. | > Nothing counts until the host accepts it. If the coach is unsure, it says so instead of guessing. |
| 0:52–0:60 | Airplane-mode icon on the phone; end card with the repo link. | > No cloud, no data bundle: a small local model helping hosts prepare for their next guest. |

## 3. Tech video (max 60 s)

| Time | Show | Say |
|---|---|---|
| 0:00–0:12 | Architecture diagram: Android app (Kotlin/Compose) → USB, `adb reverse` → loopback Python server → LangGraph agent → Ollama Qwen3 → SQLite. | > guest-i-mate is a Kotlin Compose Android app talking over USB to a loopback-only Python server. A LangGraph agent picks one bounded tool per request, and Qwen 3 runs locally in Ollama. |
| 0:12–0:24 | Model card: Qwen3 1.7B / 4B-Instruct, GGUF Q4_K_M, Apache 2.0, 1.36 GB / ~2.5 GB, temperature 0.15, JSON output. Ollama with Wi-Fi off. | > We use Qwen 3 at 1.7 or 4 billion parameters, four-bit quantized, under two and a half gigabytes, with structured JSON output and no cloud fallback. |
| 0:24–0:40 | Code/diagram: reply → feedback call + separate fact-check → agree = scored, disagree = `checks_disagree` unscored; quote validation. | > Every piece of feedback must quote the host's real words, and a separate fact-check runs against the exercise facts. If the two disagree, the reply is left unscored and nothing is saved without the host's approval. |
| 0:40–0:52 | `data/curriculum/` + ILO / Swisscontact / J-PAL citations; test runs: 46 backend, 12 Android passing. | > The curriculum is 19 synthetic situations designed from ILO competency standards. Forty-six backend and twelve Android tests pass. |
| 0:52–0:60 | Roadmap card: on-device LiteRT/llama.cpp, operator testing. | > Next: moving inference fully onto the phone and testing with real hosts. |

## Remaining checklist items

| # | Item | Where |
|---|---|---|
| 4 | 1-page report (PDF) | Not written yet |
| 5 | GitHub repository | Public repo link |
| 6 | Zipped code | `python3 scripts/package_demo.py` → `dist/guest-i-mate-demo.zip` (source only; no model weights, no private data) |
| 7 | Dataset | Synthetic curriculum in `data/curriculum/` (link the folder); no external dataset generated or redistributed |
