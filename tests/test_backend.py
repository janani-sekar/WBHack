import copy
import tempfile
import unittest
from pathlib import Path
from hospitality.coach import Coach
from hospitality.model import OllamaModel, ModelError, ASSESSMENT, REVIEW, validate


ASSESS = dict(strength_local='நேரத்தை விளக்கினீர்கள்.', improvement_local='அடுத்த படியை விளக்குங்கள்.',
              evidence_quote='twenty minutes', uncertain=False, answers_request=2,
              factual_accuracy=2, clarifies_unknowns=2, next_step=1)
REVIEW_OUTPUT = dict(translation_local='வழிகள் தெளிவாக இல்லை.', explanation_local='வழிகளை விளக்கலாம்.', uncertain=False,
                    themes=[dict(kind='concern', explanation_local='வழிகளை விளக்கலாம்.', evidence_quote='The directions were confusing.',
                                 skill='directions', training_relevant=True)])


class FakeModel:
    """Test fixture, never used by production server or real-model smoke test."""
    def __init__(self, output=None):
        self.output = output
        self.calls = []

    def generate(self, instruction, context, schema):
        self.calls.append(copy.deepcopy(context))
        result = self.output
        if result is None:
            result = ASSESS if schema == ASSESSMENT else REVIEW_OUTPUT if 'translation_local' in schema.get('properties',{}) else {'guest_message':'Can we do a short tasting?'}
        validate(result, schema)
        return copy.deepcopy(result), {'provenance':'test-fixture'}


class BackendTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.path = str(Path(self.tmp.name)/'test.sqlite3')
        self.model = FakeModel()
        self.coach = Coach(self.model, self.path)

    def tearDown(self):
        self.coach.close()
        self.tmp.cleanup()

    def test_memory_requires_consent_persists_and_can_be_deleted(self):
        with self.assertRaises(ValueError):
            self.coach.remember('coffee', 'காபி', 'menu')
        entry = self.coach.remember('coffee', 'காபி', 'menu', True)
        self.coach.close()
        self.coach = Coach(self.model, self.path)
        self.coach.start()
        self.assertEqual(self.model.calls[-1]['approved_phrases'][0]['preferred'], 'காபி')
        self.coach.forget(entry['id'])
        self.coach.start()
        self.assertEqual(self.model.calls[-1]['approved_phrases'], [])

    def test_no_progress_until_assessment_approved_and_rejection_reverses(self):
        session = self.coach.start(independent=True)
        turn = self.coach.respond(session['id'], 'The tasting is twenty minutes.')
        self.assertEqual(self.coach.progress()['skills'][0]['state'], 'not_assessed')
        self.coach.approve_assessment(session['id'], turn['id'], True)
        self.assertEqual(self.coach.progress()['skills'][0]['state'], 'demonstrated_in_practice')
        self.coach.approve_assessment(session['id'], turn['id'], False)
        self.assertEqual(self.coach.progress()['skills'][0]['state'], 'not_assessed')

    def test_uncertain_feedback_cannot_count(self):
        session = self.coach.start()
        self.model.output = {**ASSESS, 'uncertain':True}
        turn = self.coach.respond(session['id'], 'twenty minutes')
        with self.assertRaises(ValueError):
            self.coach.approve_assessment(session['id'], turn['id'], True)

    def test_fabricated_evidence_is_rejected_without_saving(self):
        session = self.coach.start()
        with self.assertRaises(ModelError):
            self.coach.respond(session['id'], 'Welcome!')
        self.assertEqual(self.coach.get('session',session['id'])['turns'], [])
        with self.assertRaises(ModelError):
            self.coach.understand_review('Everything was great!')
        self.assertEqual(self.coach.all('review'), [])

    def test_review_to_lesson_requires_approval_and_provenance(self):
        review = self.coach.understand_review('The directions were confusing.')
        with self.assertRaises(ValueError):
            self.coach.create_lesson(review['id'],0)
        lesson = self.coach.create_lesson(review['id'],0,True)
        session = self.coach.start(lesson_id=lesson['id'])
        self.assertEqual(session['skill'],'directions')
        self.assertNotIn('original_review', self.model.calls[-1])
        self.assertEqual(lesson['review_id'],review['id'])

    def test_operational_issue_does_not_become_lesson(self):
        output = copy.deepcopy(REVIEW_OUTPUT)
        output['themes'][0]['training_relevant'] = False
        self.model.output = output
        review = self.coach.understand_review('The directions were confusing.')
        with self.assertRaises(ValueError):
            self.coach.create_lesson(review['id'],0,True)

    def test_reset_preserves_business_facts(self):
        self.coach.remember('coffee','காபி','menu',True)
        self.coach.reset(True)
        self.assertEqual(self.coach.all('memory'), [])
        self.assertEqual(self.coach.context()['profile']['facts']['standard_tour_minutes'],60)

    def test_strict_schema_rejects_bool_score_extra_keys_and_bad_skill(self):
        for value in [{**ASSESS,'answers_request':True},{**ASSESS,'made_up':'yes'}]:
            with self.assertRaises(ModelError): validate(value,ASSESSMENT)
        bad = copy.deepcopy(REVIEW_OUTPUT)
        bad['themes'][0]['skill']='invented'
        with self.assertRaises(ModelError):validate(bad,REVIEW)

    def test_only_local_endpoint_and_explicit_model_allowed(self):
        for url in ['https://example.com','http://127.0.0.1.evil.test','http://user:pass@localhost','http://localhost/path']:
            with self.assertRaises(ValueError):OllamaModel(url)
        with self.assertRaises(ValueError):OllamaModel(model='cloud-model')

    def test_invalid_input_never_calls_model(self):
        for response in ['', 'a'*2001, 45]:
            with self.assertRaises(ValueError):self.coach.respond('missing',response)
        self.assertEqual(self.model.calls,[])


if __name__ == '__main__':
    unittest.main()
