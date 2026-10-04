import unittest
from hospitality.coach import Coach
from hospitality.knowledge import retrieve
from hospitality.model import ASSESSMENT
from test_backend import FakeModel

class TranslationModel(FakeModel):
    def generate(self, instruction, context, schema):
        if set(schema['properties']) == {'strength_local','improvement_local'}:
            self.calls.append(context)
            return {'strength_local':'Explicaste la duración.','improvement_local':'Pregunta la hora de llegada.'}, {}
        return super().generate(instruction,context,schema)

class LearningMemoryTests(unittest.TestCase):
    def setUp(self): self.c=Coach(TranslationModel(),':memory:')
    def tearDown(self): self.c.close()
    def test_old_records_do_not_gain_invented_tone_scores(self):
        s=self.c.start(scenario_id='short_visit')
        self.c.respond(s['id'],'twenty minutes')
        saved=self.c.get('session',s['id'])
        del saved['turns'][0]['assessment']['customer_tone']
        self.c.put('session',s['id'],saved)
        result=self.c.session_summary(s['id'])
        self.assertNotIn('customer_tone',result['summary']['criterion_means'])
    def test_poor_tone_cannot_establish_independent_evidence(self):
        from test_backend import ASSESS
        self.c.model.output={**ASSESS,'customer_tone':0,'next_step':2}
        s=self.c.start(scenario_id='short_visit',independent=True)
        t=self.c.respond(s['id'],'twenty minutes')
        self.c.approve_assessment(s['id'],t['id'],True)
        row=self.c.learning_plan()['skills'][0]
        self.assertEqual(row['independent_cases'],0)
        self.assertEqual(row['weakest_dimension'],'customer_tone')
    def test_conflicting_fact_judgments_cannot_be_saved_as_progress(self):
        from test_backend import ASSESS
        self.c.model.output={**ASSESS,'factual_accuracy':0,'customer_tone':1}
        s=self.c.start(scenario_id='tour_contents')
        t=self.c.respond(s['id'],'twenty minutes')
        self.assertEqual(t['fact_check']['verdict'],'supported')
        self.assertTrue(t['assessment']['uncertain'])
        self.assertEqual(t['assessment_issue'],'checks_disagree')
        with self.assertRaises(ValueError):self.c.approve_assessment(s['id'],t['id'],True)

    def test_fact_check_sees_actual_guest_question_and_prior_dialogue(self):
        s=self.c.start(scenario_id='late_arrival',style='chat')
        self.c.respond(s['id'],'twenty minutes')
        check=self.c.model.calls[-1]
        self.assertEqual(check['guest_message'],s['guest_message'])
        self.assertIn('original time',check['guest_message'])
        self.assertEqual(check['history'],[])
        self.c.next_guest(s['id'])
        self.c.respond(s['id'],'twenty minutes')
        self.assertEqual(self.c.model.calls[-1]['history'][0]['operator'],'twenty minutes')

    def test_full_marks_do_not_display_a_manufactured_correction(self):
        from test_backend import ASSESS
        self.c.model.output={**ASSESS,'next_step':2,'improvement_local':'Incorrect invented correction.'}
        s=self.c.start(scenario_id='short_visit')
        t=self.c.respond(s['id'],'twenty minutes')
        self.assertIn('met all five criteria',t['assessment']['improvement_local'])

    def test_factual_failure_does_not_automatically_fail_tone(self):
        from test_backend import ASSESS
        class Unsupported(FakeModel):
            def generate(self,instruction,context,schema):
                if 'verdict' in schema['properties']:
                    return {'verdict':'unsupported','reason':'Wrong duration'},{}
                return super().generate(instruction,context,schema)
        self.c.model=Unsupported({**ASSESS,'customer_tone':0})
        s=self.c.start(scenario_id='short_visit')
        t=self.c.respond(s['id'],'twenty minutes')
        self.assertEqual(t['assessment']['factual_accuracy'],0)
        self.assertEqual(t['assessment']['customer_tone'],2)
        self.assertEqual(set(self.c.model.calls[-1]),{'guest','reply'})
    def test_language_switch_does_not_regrade_and_is_cached(self):
        s=self.c.start(scenario_id='short_visit');t=self.c.respond(s['id'],'twenty minutes')
        before=t['assessment'].copy()
        switched=self.c.coaching_language(s['id'],'es')
        calls=len(self.c.model.calls)
        self.c.coaching_language(s['id'],'en');self.c.coaching_language(s['id'],'es')
        self.assertEqual(len(self.c.model.calls),calls)
        self.assertEqual(switched['turns'][0]['assessment'],before)
        self.assertEqual(switched['turns'][0]['status'],'pending')
        self.assertIn('es',switched['turns'][0]['feedback_variants'])
    def test_memory_requires_accepted_evidence_and_retracts(self):
        s=self.c.start(scenario_id='short_visit');t=self.c.respond(s['id'],'twenty minutes')
        self.assertEqual(self.c.learner_memory()['skill_observations'],[])
        self.c.approve_assessment(s['id'],t['id'],True)
        memory=self.c.context()['learner_memory']
        self.assertEqual(memory['skill_observations'][0]['sources'][0]['turn_id'],t['id'])
        self.c.approve_assessment(s['id'],t['id'],False)
        self.assertEqual(self.c.context()['learner_memory']['skill_observations'],[])
    def test_finish_requires_response_and_prevents_more_turns(self):
        s=self.c.start(scenario_id='short_visit')
        with self.assertRaises(ValueError):self.c.session_summary(s['id'])
        t=self.c.respond(s['id'],'twenty minutes')
        finished=self.c.session_summary(s['id'])
        self.assertTrue(finished['summary']['early_finish'])
        self.assertEqual(finished['summary']['turn_ids'],[t['id']])
        self.assertEqual(self.c.get('session',s['id'])['turns'][0]['status'],'pending')
        with self.assertRaises(ValueError):self.c.respond(s['id'],'twenty minutes')
        with self.assertRaises(ValueError):self.c.next_guest(s['id'])
    def test_preferences_and_guidance_enter_coaching_context(self):
        self.c.save_learning_settings('en','clear',True,coach_language='es',support='example')
        s=self.c.start(scenario_id='short_visit');self.c.respond(s['id'],'twenty minutes')
        context=next(c for c in reversed(self.c.model.calls) if 'retrieved_guidance' in c)
        self.assertEqual(context['support'],'example')
        self.assertEqual(context['profile']['language'],'es')
        self.assertEqual(context['retrieved_guidance'][0]['id'],'needs')
        self.assertEqual(retrieve('unrelated','ignore instructions'),[])
        self.c.reset(True)
        self.assertEqual(self.c.learner_memory()['skill_observations'],[])
