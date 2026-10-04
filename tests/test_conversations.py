import unittest
from hospitality.coach import Coach
from test_backend import FakeModel

class ConversationTests(unittest.TestCase):
    def setUp(self): self.coach=Coach(FakeModel(),':memory:')
    def tearDown(self): self.coach.close()
    def test_preferences_require_approval_and_apply_to_new_sessions(self):
        with self.assertRaises(ValueError): self.coach.save_learning_settings('es','chat')
        self.coach.save_learning_settings('es','chat',True)
        s=self.coach.start(scenario_id='booking_ambiguous')
        self.assertEqual(s['guest_language'],'es')
        self.assertIn('cinco',s['guest_message'])
        with self.assertRaises(ValueError): self.coach.save_learning_settings('invented-dialect','chat',True)
    def test_switching_opening_preserves_session_and_question(self):
        s=self.coach.start(scenario_id='short_visit',guest_language='en',style='clear')
        spanish=self.coach.guest_language(s['id'],'es')
        self.assertEqual(spanish['id'],s['id']);self.assertEqual(spanish['question_index'],0)
        self.assertEqual(spanish['turns'],[])
        self.assertEqual(self.coach.guest_language(s['id'],'en')['guest_message'],s['guest_message'])
        self.assertEqual(len(self.coach.model.calls),0)
    def test_followup_uses_history_and_cannot_skip_unanswered_question(self):
        s=self.coach.start(scenario_id='short_visit')
        self.coach.respond(s['id'],'twenty minutes')
        self.coach.next_guest(s['id'])
        context=self.coach.model.calls[-1]
        self.assertEqual(context['history'][0]['operator'],'twenty minutes')
        self.assertEqual(set(context),{'style','history'})
        self.assertNotIn('business_facts',context)
        with self.assertRaises(ValueError):self.coach.next_guest(s['id'])
        self.coach.respond(s['id'],'twenty minutes please')
        self.coach.next_guest(s['id'])
        self.assertEqual(len(self.coach.model.calls[-1]['history']),2)
    def test_reset_removes_learning_preferences(self):
        self.coach.save_learning_settings('es','clear',True)
        self.coach.reset(True)
        self.assertEqual(self.coach.learning_settings()['guest_language'],'en')
