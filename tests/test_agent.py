import unittest
from hospitality.coach import Coach
from hospitality.model import ModelError
try:
    from hospitality.agent import HospitalityAgent
except ImportError:
    HospitalityAgent = None


class RoutingModel:
    def __init__(self, tool='start_practice', lesson='none'):
        self.tool, self.lesson, self.contexts = tool, lesson, []
    def generate(self, prompt, context, schema):
        self.contexts.append(context)
        if 'tool' in schema['properties']:
            return dict(tool=self.tool, skill='directions', lesson_id=self.lesson,
                        reason='Practice the meeting point.', clarification='none'), {'provenance':'test-fixture'}
        return {'guest_message':'Where should we meet?'}, {'provenance':'test-fixture'}


@unittest.skipIf(HospitalityAgent is None, 'Install requirements-agent.txt for harness tests')
class AgentTests(unittest.TestCase):
    def test_uses_approved_memory_and_persists_run(self):
        m=RoutingModel(); c=Coach(m, ':memory:')
        try:
            c.remember('coffee','காபி','preferred word',True)
            r=HospitalityAgent(c).run('Help with directions')
            self.assertEqual(r['result']['skill'],'directions')
            self.assertEqual(len(m.contexts[0]['approved_phrases']),1)
            self.assertIn('learner_memory',m.contexts[0])
            self.assertEqual(c.all('agent_run')[0]['id'],r['id'])
            self.assertEqual(r['trace'][-1],'end')
        finally: c.close()
    def test_disallowed_tool_does_not_execute(self):
        for tool in ('send_message','remember','understand_review'):
            c=Coach(RoutingModel(tool), ':memory:')
            try:
                with self.assertRaises(ModelError): HospitalityAgent(c).run('Do it')
                self.assertEqual(c.all('session'),[])
                self.assertEqual(c.all('memory'),[])
            finally: c.close()
    def test_unapproved_lesson_rejected(self):
        c=Coach(RoutingModel(lesson='invented'), ':memory:')
        try:
            with self.assertRaises(ModelError): HospitalityAgent(c).run('Practice')
        finally: c.close()
