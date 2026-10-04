import copy
import unittest
from hospitality.coach import Coach
from hospitality.model import ModelError
from hospitality.feedback import groups_for
from test_backend import FakeModel


def finding(quote, topic='arrival', action='practice', polarity='negative'):
    return dict(topic=topic, action=action, polarity=polarity,
                explanation_local='Explain the meeting point.', evidence_quote=quote)


class ReviewModel(FakeModel):
    def generate(self, instruction, context, schema):
        if 'items' not in schema.get('properties', {}):
            return super().generate(instruction, context, schema)
        self.calls.append(copy.deepcopy(context))
        return copy.deepcopy(self.output or dict(translation_local=context['review'],
            uncertain=False, items=[finding(context['review'])])), {}


class FeedbackWorkflowTests(unittest.TestCase):
    def setUp(self):
        self.model = ReviewModel()
        self.c = Coach(self.model, ':memory:')

    def tearDown(self):
        self.c.close()

    def test_duplicate_texts_are_not_counted_as_more_guests(self):
        batch = self.c.review_batch(['Wrong gate.', ' WRONG   GATE. ', 'We could not find you.'])
        self.assertEqual(batch['duplicates_omitted'], 1)
        self.assertEqual(batch['groups'][0]['review_count'], 2)
        self.assertEqual(len(self.model.calls), 2)

    def test_fabricated_quote_aborts_batch(self):
        self.model.output = dict(translation_local='Wrong gate.', uncertain=False,
                                 items=[finding('This is invented.')])
        with self.assertRaises(ModelError):
            self.c.review_batch(['Wrong gate.'])
        self.assertEqual(self.c.all('review_batch'), [])

    def test_repair_request_cannot_be_confidently_positive(self):
        self.model.output = dict(translation_local='Repair it.', uncertain=False,
                                 items=[finding('Repair it.', 'facilities', 'improve', 'positive')])
        batch = self.c.review_batch(['Repair it.'])
        self.assertTrue(batch['reviews'][0]['analysis']['uncertain'])
        self.assertTrue(batch['groups'][0]['uncertain'])
        self.assertFalse(batch['groups'][0]['can_practice'])

    def test_problem_and_suggestion_are_not_opposing_guest_views(self):
        reviews = [dict(id='one', analysis=dict(uncertain=False, items=[
            finding('Broken sign.', 'facilities', 'improve', 'negative'),
            finding('Please repair it.', 'facilities', 'improve', 'suggestion')]))]
        self.assertFalse(any(g['different_views'] for g in groups_for(reviews)))
        reviews.append(dict(id='two', analysis=dict(uncertain=False, items=[
            finding('Sign is great.', 'facilities', 'preserve', 'positive')])))
        self.assertTrue(any(g['different_views'] for g in groups_for(reviews)))

    def test_uncertain_or_facility_findings_cannot_create_lessons(self):
        for uncertain, topic, action in [(True, 'arrival', 'practice'), (False, 'facilities', 'practice')]:
            self.model.output = dict(translation_local='Broken sign.', uncertain=uncertain,
                                     items=[finding('Broken sign.', topic, action)])
            batch = self.c.review_batch(['Broken sign.'])
            with self.assertRaises(ValueError):
                self.c.batch_lesson(batch['id'], batch['groups'][0]['id'], True)

    def test_approved_review_changes_question_not_exercise_facts(self):
        batch = self.c.review_batch(['Wrong gate.'])
        group = batch['groups'][0]
        with self.assertRaises(ValueError):
            self.c.batch_lesson(batch['id'], group['id'])
        lesson = self.c.batch_lesson(batch['id'], group['id'], True)
        normal = self.c.start(scenario_id='no_map')
        session = self.c.start(lesson_id=lesson['id'])
        self.assertEqual(session['scenario_id'], 'no_map')
        self.assertEqual(session['scenario']['facts'], normal['scenario']['facts'])
        self.assertEqual(lesson['sources'][0]['quote'], 'Wrong gate.')
        self.assertEqual(session['personalization']['review_focus'], lesson['reason_local'])

    def test_product_and_service_complaints_create_only_service_practice(self):
        quote = 'the coffee was really bad and the people were rude'
        self.model.output = dict(translation_local=quote, uncertain=False, items=[
            finding(quote, 'other', 'improve'), finding(quote, 'hospitality', 'practice')])
        batch = self.c.review_batch([quote])
        product, service = batch['groups']
        self.assertFalse(product['can_practice'])
        self.assertTrue(service['can_practice'])
        lesson = self.c.batch_lesson(batch['id'], service['id'], True)
        self.model.output = None
        session = self.c.start(lesson_id=lesson['id'])
        self.assertEqual(session['scenario_id'], 'service_recovery')
        self.assertEqual(session['personalization']['review_sources'][0]['quote'], quote)
        self.assertNotIn('directions', session['scenario']['rubric']['clarifies_unknowns']['2'])
        self.assertNotIn('coffee', str(session['scenario']['facts']))

    def test_rejected_and_uncertain_turns_do_not_drive_recap(self):
        session = self.c.start(scenario_id='short_visit')
        turn = self.c.respond(session['id'], 'twenty minutes')
        self.c.approve_assessment(session['id'], turn['id'], False)
        summary = self.c.session_summary(session['id'])['summary']
        self.assertEqual(summary['eligible_turn_ids'], [])
        self.assertEqual(summary['criterion_means'], {})
        self.assertIsNone(summary['focus'])

    def test_business_draft_is_explicit_and_never_updates_profile(self):
        session = self.c.start(scenario_id='short_visit')
        before = copy.deepcopy(self.c.get('profile', 'default'))
        with self.assertRaises(ValueError):
            self.c.practice_draft(session['id'])
        self.c.respond(session['id'], 'twenty minutes')
        self.c.session_summary(session['id'])
        draft = self.c.practice_draft(session['id'])
        self.assertTrue(draft['synthetic'])
        with self.assertRaises(ValueError):
            self.c.approve_draft(draft['id'], 'Edited')
        saved = self.c.approve_draft(draft['id'], 'Edited by operator', True)
        self.assertEqual(saved['status'], 'operator_approved')
        self.assertEqual(self.c.get('profile', 'default'), before)
