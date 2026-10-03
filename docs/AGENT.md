# Agent architecture and implementation status

## Intended product
One local hospitality agent serves the training chatbot and review workspace. It retrieves approved language preferences, business facts and learning history; selects a relevant exercise or review tool; observes the result; and adapts the next interaction. The side panel shows the evidence behind practice recommendations and progress.

## Implemented desktop slice
`hospitality/agent.py` uses **LangGraph 1.2.12**. The dependency environment is pinned in `requirements-agent.txt`.

1. Retrieve profile, approved phrases, derived progress, and up to ten approved review lessons from SQLite.
2. Qwen emits a structured choice: `start_practice`, `understand_review` (only when review text is explicitly supplied), or `ask_clarification`.
3. Validate tool and lesson identifiers against allowed values. No arbitrary code, SQL, file or network tools are exposed.
4. Execute one bounded tool and return its result. The whole graph is limited to six steps and ends after that tool.
5. Persist completed run decisions/results locally. LangSmith tracing is explicitly disabled.

This is an initial agent with model-selected actions, not a complete autonomous tutor. Within-session responses still use the existing explicit API. It does not yet resume an interrupted graph, stream responses, or autonomously retry tools. Completed runs and domain memory persist; graph checkpoints are not configured.

A real local inference trace is in [agent-smoke.json](../evaluation/agent-smoke.json). Unit tests cover retrieval, run persistence, prohibited tool choices, and invented lesson IDs. Separate model-quality failures remain documented in [MODEL.md](MODEL.md).

## Personalization and approval
- Vocabulary: operator-approved original/preferred phrase pairs are retrieved before planning and generation.
- Learning state: approved assessment history provides a rule-based baseline recommendation; the agent can select a relevant scenario based on the user's request and approved review lessons.
- Review feedback: explicit approval is required to create a lesson; uncertain/operational themes cannot be promoted.
- The agent has no tool that approves its own scores, changes business facts, or saves vocabulary without the explicit API approval flow.
- Deleting memory stops future retrieval, not historical transcript retention. Reset clears learning records; no model weights were trained.

## Android path
LangGraph/Python is the desktop reference, not the final Android deployment. **Google ADK for Kotlin** is the Android harness candidate: its official repository documents agents, tools, sessions and memory plus on-device LiteRT-LM integration. Qwen model-format/runtime compatibility is not established by that documentation and must be proven separately. The existing Ollama GGUF file cannot be assumed to work in LiteRT-LM.

Next implementation steps:
1. Fix a reviewed Tamil/English test set for tool routing, feedback fidelity, and review interpretation; select a model that passes it.
2. Define shared JSON tool contracts matching the existing business functions and consent boundaries.
3. Build a minimal Kotlin ADK + local inference spike; verify the chosen model and quantization load and call a single tool without network access. If Qwen requires a different inference runtime, implement/test the model adapter before committing to the harness.
4. Add persistent Android storage for memory, learner state, and interrupted sessions; port the behavioral tests.
5. Connect chatbot and side panel, then review-to-practice flow.
6. Test in an Android emulator with network disabled, process restart, memory/latency measurements and malformed tool calls. Emulator results still do not establish phone thermals, battery use or real-device speed.

Do not add cloud services to make the offline demo work. Review reply drafting remains stretch scope.

## Sources
- [LangGraph overview](https://docs.langchain.com/oss/python/langgraph/overview): stateful orchestration and persistence capabilities; using a capability requires configuring it.
- [Google ADK Kotlin](https://github.com/google/adk-kotlin): open-source Android/on-device harness candidate, not confirmation of this project's Qwen compatibility.

## Current prototype language decision

Spanish (`es`) is now the default, following the user's approval to use another language and proceed without a fluent-speaker study for the prototype. Existing Tamil references above describe the initial plan/results. This is automated/provisional evaluation, not native-speaker validation. The profile can select `es`, `ta` or `hi`; response keys are language-neutral (`strength_local`, `improvement_local`, `translation_local`, `explanation_local`, `reason_local`). Supported codes do not imply validated language quality. Historical evaluation JSON retains the original Tamil field names and outputs.
