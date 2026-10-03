# Continual learning: three distinct mechanisms

Implementation update: approved local phrase memory and rule-based progress recommendations are implemented in the desktop backend. Actual classifier/adapter weight updates remain deferred.

The user wants the coach to improve with the operator's language and experience. Implement narrowly and describe precisely what changes.

## 1. Approved memory: required MVP
Save user-approved vocabulary, preferred phrases, spelling variants, business terminology, language choice, and teaching preferences in local storage. Retrieve relevant entries during coaching. Corrections must be explicitly confirmed before storage.

Example: the operator corrects the name of their coffee ceremony. The coach asks whether to remember it and uses the approved term in a later scenario after restarting offline.

This is persistent personalization, **not a language-model weight update**. A remembered phrase is not evidence that the system understands an entire dialect or pronunciation pattern.

Record: id, language tag, original phrase, approved replacement, context, scope, consent timestamp, version. Business facts are stored separately and cannot be overwritten by guest text or model suggestions. Do not retain raw guest identifiers or audio by default.

## 2. Adaptive lesson selection: required MVP
Maintain skills such as explaining duration, acknowledging constraints, asking clarifying questions, and giving a next step. Use recent confirmed performance to select the next practice scenario. A transparent rules-based scheduler is sufficient; label it as such. Avoid endlessly presenting one weak skill: include varied review scenarios and a reset option.

## 3. Actual model updates: bounded experiment
Add a tiny local classifier for approved phrase-to-intent mapping (duration, directions, availability, clarification, other). A linear model with character n-gram features can update from corrections with bounded stochastic-gradient steps. This is genuine model-parameter learning for a narrow task; it is not an LLM learning a new language.

Proposed flow:
1. Classifier predicts an intent; uncertain inputs go to clarification.
2. Operator explicitly supplies/approves the intended label.
3. Store the correction locally with consent; do not train on the model's own guesses.
4. Snapshot current weights. Train with bounded steps on a batch of approved corrections plus replay examples from old intents.
5. Evaluate on a separate validation set. Accept only if new-phrase performance improves without unacceptable regressions; otherwise roll back.
6. Keep final test paraphrases untouched until reporting. Do not use the same sentence for training and proof of generalization.
7. Persist version and update history. Deletion rebuilds from baseline plus retained approved examples, so deleted corrections are removed from derived weights as well as memory.

Reference implementation concept: scikit-learn SGDClassifier supports partial_fit, but a Python prototype is not evidence of Android deployment. Port inference/update math or select a compatible runtime and measure it on the target device.

## Later: language-model adapters
With enough consented, reviewed data, investigate periodic LoRA/adapters trained on suitable hardware, validated and versioned, then transferred to supported devices. Adapter/runtime compatibility and training memory must be verified. This is future work unless implemented and measured; it is not a promised on-phone capability.

## Acceptance evidence
- New session uses only explicitly approved vocabulary.
- Restart preserves personalization without network access.
- Unseen paraphrases improve for a learned intent; report old-intent retention too.
- One mistaken correction does not silently overwrite business facts.
- User can inspect, remove, reset, or decline learning.
- No accent, ethnicity, personality, or universal politeness judgments.

## Pitch wording
Implemented memory only: 'The coach personalizes practice using approved phrases and skill history stored on the device.'
Implemented and evaluated incremental model: 'A small intent classifier updates locally from approved corrections; we measured generalization and retention separately.'
Never: 'The model automatically learns any language from a few conversations.'

## Technical references
- https://scikit-learn.org/stable/modules/generated/sklearn.linear_model.SGDClassifier.html
- https://huggingface.co/docs/transformers/main/peft

## Agentic architecture update

Use the open-source LangGraph harness for the desktop reference implementation, with bounded Qwen tool selection, approved local memory, progress and review-derived lessons. The first agent entry point is implemented in `hospitality/agent.py`; existing HTTP routes remain explicit tools and are not yet a unified agent chat endpoint. See [agent design](AGENT.md) for the Android-native harness decision and remaining integration work. Personalization is stored context and curriculum adaptation, not model-weight training.

## Current prototype language decision

Spanish (`es`) is now the default, following the user's approval to use another language and proceed without a fluent-speaker study for the prototype. Existing Tamil references above describe the initial plan/results. This is automated/provisional evaluation, not native-speaker validation. The profile can select `es`, `ta` or `hi`; response keys are language-neutral (`strength_local`, `improvement_local`, `translation_local`, `explanation_local`, `reason_local`). Supported codes do not imply validated language quality. Historical evaluation JSON retains the original Tamil field names and outputs.
