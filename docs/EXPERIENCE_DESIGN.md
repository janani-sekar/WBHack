# guest-i-mate: a coach for everyday hosting

Design pass: 3 October 2026. Product name: guest-i-mate (formerly the working name "Welcome").

## Start with the operator's day

The supplied World Bank concept note (Tourism Annex C, pp. 19–20) describes Noor relying on word of mouth and a guide for translation, with little insight into what visitors valued after leaving. Noor is fictional. Our farm situations are synthetic design hypotheses, not interviews or validated lived-experience research.

The job is not “complete a course.” It is “feel ready to handle the next guest interaction, and learn something useful from the last one.” A busy operator may have a short gap between visits, a shared phone and intermittent connectivity. Design for a useful stopping point after one reply rather than a compulsory sequence or daily streak.

## Online references and what we borrow

- [Airbnb Resource Center](https://www.airbnb.com/resources/hosting-homes): organize around familiar hosting tasks—welcoming, messaging, guest reviews—rather than a technical feature list. This informs our information architecture, not an endorsement or copied asset.
- [Duolingo Roleplay](https://blog.duolingo.com/duolingo-max/): bounded, authored situations with a conversation followed by specific feedback. Keep the task clear before opening the chat; retain human review of AI feedback.
- [Brilliant](https://brilliant.org/): guided practice and visible learning gaps. Borrow progressive disclosure and a clear next step; its product claims are not evidence our model improves learning.
- [World Bank, Lake Toba and Lombok, 2025](https://www.worldbank.org/en/news/feature/2025/03/19/indonesia-integrated-tourism-improving-livelihoods-for-thousands-in-lake-toba-and-lombok): situate training in tourism livelihoods and business constraints. Do not imply training alone fixes infrastructure or guarantees income.

The user's example contributes progressive narrowing. For this prototype, the useful sequence is **business moment → guest situation → practice**, rather than occupation → job title. Only farm-visit content is supported today; showing hotel/restaurant selectors would imply curricula we have not built.

## User flow

1. **Your day:** one recommended practice and an alternative entry through a guest review. No required account, long onboarding, leaderboard or fabricated success metric.
2. **Choose a situation:** use familiar groupings: make the visit work, help guests find you, set clear expectations. Each card previews the guest's actual question.
3. **Practise:** a chat with a bounded guest situation; example facts and optional hints sit alongside it. English/Spanish guest dialogue, Spanish coaching; the UI itself is currently English. Choose independent mode before the first response.
4. **Reflect:** show one strength and one improvement. Scores and raw diagnostic output sit behind disclosure controls. Accept or reject feedback before it affects progress. The coach never sends a guest message.
5. **Choose practice preferences:** save guest language and clear/everyday messages. The Your words tab and phrase-saving controls have been removed.
6. **Apply feedback:** translate and interpret one guest review, approve a relevant lesson, open a richer scenario of that skill. The lesson-to-scenario match is skill-level, not an exact recreation of the review. Multi-review synthesis remains future work.
7. **Return:** recommendations rotate among situations and use the existing learning plan. Progress reports accepted evidence and delayed independent performance, not professional certification. The progress line is a categorical state indicator, not percent mastery.

## Visual direction

Warm paper, deep green navigation, restrained terracotta accents, editorial serif headings and readable system body text. The simplified home removes the hero illustration and promotional copy. Space and hierarchy do most of the work. Mobile navigation wraps into a compact top row; the practice rail stacks beneath the conversation. Reduced-motion preferences are respected, controls have visible keyboard focus, and meaning is not conveyed through color alone.

## Implemented and next

Implemented in the browser UI: home, 18-situation catalog, topic filters, scenario chat, optional hints, factual context, approved progress, review-to-practice, language/style preferences and three-exchange conversations. The existing agent API remains available; a technical “run agent” panel no longer occupies the operator's main navigation.

Next: full UI localization; carry a real operator's verified service details into suitable practice; a review-specific rehearsal generator with factual checks; resumable sessions on shared devices; accessibility testing with actual users; calibrated assessments. Audio should only be shown when it works, not as a decorative microphone.

## Observed walkthrough limits

The real-model walkthrough produced Spanish coaching but included a redundant suggestion, which was rejected successfully without progress credit. A longer review produced imperfect translation and uncertain classification; the UI correctly withheld lesson approval. Learners can choose a practice situation manually when the interpretation is not useful. These checks verify the interaction and guardrails, not translation quality. The small model still needs focused review-task evaluation and likely task decomposition before a reliable public demonstration.
