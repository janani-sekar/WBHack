"""Original synthetic practice content; reserved evaluation data is never loaded here."""
import json
from pathlib import Path

PATH = Path(__file__).resolve().parents[1] / 'data/curriculum/practice.json'
CASES = {c['id']: c for c in json.loads(PATH.read_text())['cases']}
SCORE_KEYS = ('answers_request','factual_accuracy','clarifies_unknowns','next_step','customer_tone')


def public_case(case):
    return {k:v for k,v in case.items() if k not in ('reference_response_es','weak_response','followup')}


def scenario(case_id):
    if case_id not in CASES:
        raise ValueError('Unknown practice scenario; evaluation cases are not available for teaching')
    return public_case(CASES[case_id])
