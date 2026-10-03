"""Real local-model smoke test with synthetic data; no fake inference fallback."""
import json
import platform
import sys
import tempfile
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from hospitality.coach import Coach
from hospitality.model import OllamaModel


def main():
    model = OllamaModel(constrained='--constrained' in sys.argv)
    report = {'platform':platform.platform(), 'python':platform.python_version(),
              'test_scope':'desktop local inference; NOT Android emulator or hardware validation',
              'inputs':'synthetic; Spanish prototype; automated evaluation only', 'health':model.health(),
              'network_scope':'adapter restricted to loopback; full device network isolation not verified'}
    with tempfile.TemporaryDirectory() as d:
        path = str(Path(d)/'smoke.sqlite3')
        coach = Coach(model,path)
        memory = coach.remember('coffee tasting','degustación de café','Use this preferred term for the tasting activity.',True)
        session = coach.start('duration')
        report['session'] = session
        turn = coach.respond(session['id'],'La degustación de café dura 20 minutos. ¿A qué hora les gustaría comenzar?')
        report['assessment'] = turn
        # Do not auto-approve an assessment as evidence of human review.
        report['progress_before_human_approval'] = coach.progress()
        report['review'] = coach.understand_review('We loved the coffee tasting, but the directions were confusing.')
        coach.close()
        coach = Coach(model,path)
        report['memory_survives_restart'] = coach.all('memory')[0]['id'] == memory['id']
        coach.forget(memory['id'])
        report['memory_deleted'] = coach.all('memory') == []
        coach.close()
    print(json.dumps(report,ensure_ascii=False,indent=2))


if __name__ == '__main__':
    main()
