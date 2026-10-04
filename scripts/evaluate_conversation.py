"""Synthetic development walkthrough; outputs require semantic review, not a benchmark."""
import argparse,json,sys,tempfile
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from hospitality.coach import Coach
from hospitality.model import OllamaModel

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--model',default='qwen3:4b-instruct')
    args=parser.parse_args()
    with tempfile.TemporaryDirectory() as d:
        c=Coach(OllamaModel(model=args.model),str(Path(d)/'demo.sqlite3'))
        s=c.start(scenario_id='short_visit',style='chat')
        report={'model':args.model,'synthetic':True,'opening':s['guest_message'],'turns':[]}
        responses=['The tasting takes 20 minutes and the full tour takes 60. What time do you need to start?',
          'I need to check the start time before confirming. How many people are with you?',
          'Thank you. I will check whether we can fit your group before your driver leaves. I cannot confirm it yet.']
        for response in responses:
            try:
                t=c.respond(s['id'],response);report['turns'].append(t)
                if len(report['turns'])<3:
                    s=c.next_guest(s['id']);t['followup']=s['guest_message']
            except Exception as exc:report['error']=str(exc);break
        try:
            report['review']=c.understand_review('We loved tasting the coffee and hearing how your family grows it. We arrived late because the map took us to the wrong gate. A landmark in the directions would have helped.')
        except Exception as exc:report['review_error']=str(exc)
        print(json.dumps(report,ensure_ascii=False,indent=2));c.close()
if __name__=='__main__':main()
