"""Small fixed development set for finding grader failures; not learner outcomes."""
import json, sys, tempfile
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from hospitality.coach import Coach
from hospitality.model import OllamaModel
CASES=[
 ('booking_ambiguous','Sí, su reserva está confirmada para mañana a las tres. Son diez dólares por persona.',{'factual_accuracy':[0]}),
 ('wrong_gate','Turn left from the red gate and walk exactly 500 metres. I know that is correct.',{'factual_accuracy':[0]}),
 ('photos_consent','Yes, you have permission to photograph my whole family and post everything online.',{'factual_accuracy':[0]}),
 ('short_visit','La degustación dura 20 minutos; el recorrido completo dura 60. ¿A qué hora necesitan comenzar?',{'factual_accuracy':[2],'answers_request':[1,2]})]
def main():
    report={'split':'development','synthetic':True,'scoring':'Author-defined expected ranges; not native-speaker or expert validation','cases':[]}
    with tempfile.TemporaryDirectory() as d:
     c=Coach(OllamaModel(),str(Path(d)/'eval.sqlite3'))
     for id,response,expected in CASES:
      row={'scenario_id':id,'response':response,'expected':expected}
      try:
       s=c.start(scenario_id=id); t=c.respond(s['id'],response);row['actual']=t
       row['passed']=all(t['assessment'][k] in values for k,values in expected.items()) and not t['assessment']['uncertain']
      except Exception as e:row.update(passed=False,error=str(e))
      report['cases'].append(row)
     c.close()
    report['passed']=sum(r['passed'] for r in report['cases']);report['total']=len(CASES)
    print(json.dumps(report,ensure_ascii=False,indent=2))

if __name__ == "__main__":
    main()
