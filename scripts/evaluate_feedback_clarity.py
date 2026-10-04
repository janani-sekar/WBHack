"""Synthetic development examples, not an independent quality benchmark."""
import sys,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from hospitality.coach import Coach
from hospitality.model import OllamaModel
c=Coach(OllamaModel(model='qwen3:4b-instruct'),':memory:')
out=[]
for reply in ['yes you can do the whole tour','The full tour takes 60 minutes, so we cannot finish it at the original time after a late start. We could discuss the 20-minute tasting or check whether an extension is possible. Which would you prefer?']:
 s=c.start(scenario_id='late_arrival',style='chat');t=c.respond(s['id'],reply)
 out.append({'guest':s['guest_message'],'reply':reply,'assessment':t['assessment'],'issue':t['assessment_issue'],'fact_check':t['fact_check']})
 print(json.dumps(out[-1],ensure_ascii=False),flush=True)
Path('evaluation/feedback-clarity-development.json').write_text(json.dumps(out,ensure_ascii=False,indent=2))
c.close()
