"""Synthetic regression examples; inspect outputs, not a validated quality benchmark."""
import json
from pathlib import Path
import sys
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from hospitality.coach import Coach
from hospitality.model import OllamaModel

def main():
    c=Coach(OllamaModel(model='qwen3:4b-instruct'),':memory:')
    results=[]
    for replies in [('lunch is included! congrats',),('lunch is included! congrats','no lunch is included')]:
        s=c.start(scenario_id='tour_contents')
        for reply in replies:
            s['turns'].append({'guest_message':s['guest_message'],'response':reply,'question_index':s['question_index'],'status':'pending','assessment':{'uncertain':True}})
            c.put('session',s['id'],s)
            s=c.next_guest(s['id'])
        results.append({'operator_replies':replies,'guest':s['guest_message']})
    for reply in ['Lunch is not included, obviously. Read the description.',
                  'The visit includes a farm walk, coffee preparation demonstration and tasting. Lunch is not included. Would you like to hear more about the tasting?']:
        s=c.start(scenario_id='tour_contents');t=c.respond(s['id'],reply)
        results.append({'operator_reply':reply,'assessment':t['assessment'],'fact_check':t['fact_check']})
    Path('evaluation/guest-tone-development.json').write_text(json.dumps(results,indent=2,ensure_ascii=False))
    print(json.dumps(results,indent=2,ensure_ascii=False))
    c.close()
if __name__=='__main__':main()
