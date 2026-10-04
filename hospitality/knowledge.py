"""Offline retrieval over reviewed, source-linked paraphrases; no external calls."""
import re

ILO = 'https://www.ilo.org/sites/default/files/wcmsp5/groups/public/@asia/@ro-bangkok/documents/publication/wcms_bk_pb_233_en.pdf'
CARDS = [
    {'id':'needs','skill':'duration','title':'Identify the guest’s needs','source':ILO+'#page=46','section':'ILO Tourism RMCS, C10, printed p.36',
     'text':'Listen and ask questions to establish the guest’s needs. Recommend a service using accurate product knowledge and the guest’s available time and preferences.'},
    {'id':'information','skill':'directions','title':'Give accurate local information','source':ILO+'#page=44','section':'ILO Tourism RMCS, C8, printed p.34',
     'text':'Keep destination information current and relevant to the customer. Tailor the information to the setting and check reliable sources when details are uncertain.'},
    {'id':'expectations','skill':'expectations','title':'Match the service to the request','source':ILO+'#page=46','section':'ILO Tourism RMCS, C10, printed p.36',
     'text':'Clarify customer needs through listening and questions, then suggest services supported by product knowledge. Follow up so the customer knows what happens next.'},
]

def retrieve(skill, query='', limit=2):
    """Skill filter prevents irrelevant fallback; lexical overlap ranks eligible cards."""
    tokens=set(re.findall(r'\w+',query.lower()))
    eligible=[c for c in CARDS if c['skill']==skill]
    ranked=sorted(eligible,key=lambda c:-len(tokens & set(re.findall(r'\w+',c['text'].lower()))))
    return [{**c,'kind':'reviewed paraphrase','version':'2026-10-03'} for c in ranked[:limit]]
