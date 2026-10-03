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
            assert request('/progress',headers={'Origin':'https://example.com'})[0] == 403
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
