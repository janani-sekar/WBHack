"""Exercise real HTTP routes without requiring inference or changing user data."""
import json
import socket
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
from pathlib import Path


def main():
    root = Path(__file__).resolve().parents[1]
    with socket.socket() as sock:
        sock.bind(('127.0.0.1',0))
        port = sock.getsockname()[1]
    http = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    def request(path, body=None, headers=None, method=None):
        payload = None if body is None else json.dumps(body).encode()
        req = urllib.request.Request(f'http://127.0.0.1:{port}'+path, data=payload,
                headers={'Content-Type':'application/json', **(headers or {})},method=method)
        try:
            with http.open(req,timeout=5) as r:return r.status,json.load(r)
        except urllib.error.HTTPError as e:
            return e.code,json.load(e)
    with tempfile.TemporaryDirectory() as d:
        process = subprocess.Popen([sys.executable,'-m','hospitality.server','--port',str(port),'--db',str(Path(d)/'test.sqlite3')],cwd=root,stdout=subprocess.DEVNULL)
        try:
            for attempt in range(50):
                try:
                    status,_=request('/progress')
                    break
                except urllib.error.URLError:time.sleep(.1)
            else:raise RuntimeError('Server did not start')
            assert status == 200
            assert request('/missing')[0] == 404
            status, cases = request('/curriculum')
            assert status == 200 and len(cases) == 18
            assert all('reference_response_es' not in c and 'weak_response' not in c for c in cases)
            assert request('/learning-plan')[0] == 200
            assert request('/learning-settings')[0] == 200
            assert request('/learner-memory')[0] == 200
            assert request('/review-batches')[1] == []
            assert request('/review-batches', {'reviews': []})[0] == 400
            assert request('/batch-lessons', {'batch_id': 'missing', 'group_id': 'missing'})[0] == 400
            assert request('/drafts')[1] == []
            assert request('/learning-settings',{'guest_language':'es','style':'chat'})[0] == 400
            assert request('/learning-settings',{'guest_language':'es','style':'chat','approved':True})[0] == 200
            status, session = request('/sessions',{'scenario_id':'booking_ambiguous'})
            assert status == 200 and session['guest_language'] == 'es'
            assert request('/practice-drafts', {'session_id': session['id']})[0] == 400
            status, switched = request('/sessions/'+session['id']+'/language',{'language':'en'})
            assert status == 200 and switched['id'] == session['id']
            assert switched['question_index'] == 0 and switched['turns'] == []
            assert request('/sessions/'+session['id']+'/coach-language',{'language':'es'})[1]['coach_language'] == 'es'
            assert request('/sessions/'+session['id']+'/finish',{})[0] == 400
            assert request('/progress',headers={'Origin':'https://example.com'})[0] == 403
            assert request('/progress',headers={'Origin':f'http://127.0.0.1:{port}'})[0] == 200
            assert request('/progress',headers={'Origin':'null'})[0] == 403
            assert request('/progress',headers={'Sec-Fetch-Site':'cross-site'})[0] == 403
            with http.open(f'http://127.0.0.1:{port}/') as page:
                assert page.status == 200
                assert 'frame-ancestors' in page.headers['Content-Security-Policy']
                assert b'Hospitality Coach' in page.read()
            assert request('/profile',{'name':'demo','facts':{'duration':20}})[0] == 400
            assert request('/profile',{'name':'demo','facts':{'duration':20},'approved':True})[0] == 200
            assert request('/memory',{'original':'coffee','preferred':'காபி','context':'menu'})[0] == 400
            status,entry=request('/memory',{'original':'coffee','preferred':'காபி','context':'menu','approved':True})
            assert status == 200
            assert request('/memory')[1][0]['id'] == entry['id']
            assert request('/memory/'+entry['id'],method='DELETE')[0] == 200
            assert request('/memory')[1] == []
            assert request('/sessions',{'skill':'invalid'})[0] == 400
            assert request('/reset',{'approved':True})[0] == 200
            print('HTTP checks passed: routes, input validation, consent, deletion, reset, cross-origin rejection.')
        finally:
            process.terminate()
            process.wait(timeout=5)


if __name__ == '__main__':main()
