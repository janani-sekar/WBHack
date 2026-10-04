import unittest
from datetime import datetime, timezone, timedelta
from hospitality.coach import Coach
from hospitality.curriculum import CASES
from test_backend import FakeModel


class CurriculumTests(unittest.TestCase):
    def setUp(self):self.c=Coach(FakeModel(),':memory:')
    def tearDown(self):self.c.close()
    def test_only_practice_cases_are_available(self):
        self.assertEqual(len(CASES),19)
        with self.assertRaises(ValueError):self.c.start(scenario_id='holdout_pin')
    def test_case_facts_are_isolated_from_business_profile(self):
        original=self.c.get('profile','default')
        s=self.c.start(scenario_id='booking_ambiguous')
        self.c.respond(s['id'],'twenty minutes')
        context=next(x for x in reversed(self.c.model.calls) if 'profile' in x)
        self.assertEqual(context['profile']['facts']['booking_status'],'unconfirmed')
        self.assertEqual(self.c.get('profile','default'),original)
        self.assertNotIn('reference_response_es',context['scenario'])
        self.assertIn('critical_errors',context['scenario'])
    def test_fact_verifier_overrides_overconfident_grading(self):
        class DisagreeingModel(FakeModel):
            def generate(self, prompt, context, schema):
                if 'verdict' in schema['properties']:
                    return {'verdict':'unsupported','reason':'Invented claim'}, {'provenance':'test-fixture'}
                return super().generate(prompt,context,schema)
        self.c.model=DisagreeingModel()
        s=self.c.start(scenario_id='booking_ambiguous',independent=True)
        t=self.c.respond(s['id'],'twenty minutes')
        self.assertEqual(t['assessment']['factual_accuracy'],0)
        self.c.approve_assessment(s['id'],t['id'],True)
        self.assertEqual(self.c.learning_plan()['skills'][2]['independent_cases'],0)
    def test_recommendation_rotates_within_level(self):
        first=self.c.learning_plan()['recommended_scenario_id']
        self.c.start(scenario_id=first)
        self.assertNotEqual(first,self.c.learning_plan()['recommended_scenario_id'])
    def test_same_session_retries_cannot_establish_retention(self):
        s=self.c.start(scenario_id='short_visit',independent=True)
        for _ in range(2):
            t=self.c.respond(s['id'],'twenty minutes');self.c.approve_assessment(s['id'],t['id'],True)
        row=self.c.learning_plan()['skills'][0]
        self.assertEqual(row['accepted_sessions'],1)
        self.assertEqual(row['independent_cases'],0)
        self.assertEqual(row['state'],'building_evidence')
    def test_two_cases_require_delayed_independent_evidence(self):
        for id in ['short_visit','tour_contents']:
            s=self.c.start(scenario_id=id,independent=True)
            t=self.c.respond(s['id'],'twenty minutes');self.c.approve_assessment(s['id'],t['id'],True)
        self.assertEqual(self.c.learning_plan()['skills'][0]['state'],'building_evidence')
        old=self.c.all('session')[0];old['turns'][0]['created_at']=(datetime.now(timezone.utc)-timedelta(days=2)).isoformat()
        self.c.put('session',old['id'],old)
        self.assertEqual(self.c.learning_plan()['skills'][0]['state'],'retained_in_simulation')
    def test_followup_does_not_erase_independent_opening_evidence(self):
        s=self.c.start(scenario_id='short_visit',independent=True)
        first=self.c.respond(s['id'],'twenty minutes')
        self.c.approve_assessment(s['id'],first['id'],True)
        self.c.next_guest(s['id'])
        followup=self.c.respond(s['id'],'twenty minutes')
        self.c.approve_assessment(s['id'],followup['id'],True)
        self.assertEqual(self.c.learning_plan()['skills'][0]['independent_cases'],1)
        self.c.approve_assessment(s['id'],first['id'],False)
        self.assertEqual(self.c.learning_plan()['skills'][0]['independent_cases'],0)
    def test_rejecting_evidence_reverses_adaptation(self):
        s=self.c.start(scenario_id='short_visit',independent=True)
        t=self.c.respond(s['id'],'twenty minutes');self.c.approve_assessment(s['id'],t['id'],True)
        self.c.approve_assessment(s['id'],t['id'],False)
        self.assertEqual(self.c.learning_plan()['skills'][0]['accepted_sessions'],0)
