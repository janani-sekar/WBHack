"""macOS process-isolated offline test. Leaves system network settings untouched.
Requires preinstalled dependencies and weights. Blocks internet for this worker,
its backend, Ollama server and model runner; permits localhost communication only.
"""
import json, os, socket, subprocess, sys, tempfile, time, urllib.request
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
POLICY = '(version 1)(allow default)(deny network*)(allow network-outbound (remote ip "localhost:*"))(allow network-inbound (local ip "localhost:*"))'


def port():
    with socket.socket() as s:
        s.bind(('127.0.0.1', 0))
        return s.getsockname()[1]


def worker():
    report = {'scope':'macOS process sandbox; internet denied, loopback permitted; not Android or whole-device airplane mode',
              'prerequisites':'Model weights, Python packages and Ollama already installed', 'network_policy':POLICY}
    with socket.socket() as s:
        s.settimeout(3)
        error = s.connect_ex(('1.1.1.1',443))
    report['external_connection_errno'] = error
    assert error == 1, f'Expected EPERM from sandbox, got {error}'
    model_port, app_port = port(), port()
    http = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    def req(base, path, body=None):
        r=urllib.request.Request(base+path, data=None if body is None else json.dumps(body).encode(),
            headers={'Content-Type':'application/json','Origin':base})
        with http.open(r,timeout=150) as response:
            data=response.read()
            return json.loads(data) if 'application/json' in response.headers.get('Content-Type','') else data.decode()
    model_base=f'http://127.0.0.1:{model_port}'
    app_base=f'http://127.0.0.1:{app_port}'
    processes=[]
    with tempfile.TemporaryDirectory() as d, open(Path(d)/'process.log','w') as log:
        try:
            env={**os.environ,'OLLAMA_HOST':model_base,'OLLAMA_NO_CLOUD':'true','OLLAMA_NOPRUNE':'true'}
            processes.append(subprocess.Popen(['/usr/local/bin/ollama','serve'],env=env,stdout=log,stderr=log))
            for _ in range(100):
                try: req(model_base,'/api/tags');break
                except Exception: time.sleep(.2)
            else: raise RuntimeError('Isolated model server did not start')
            code='from hospitality.server import make_handler; from hospitality.coach import Coach; from hospitality.model import OllamaModel; from http.server import HTTPServer; import sys; HTTPServer(("127.0.0.1",int(sys.argv[1])),make_handler(Coach(OllamaModel(base_url=sys.argv[2]),sys.argv[3]))).serve_forever()'
            processes.append(subprocess.Popen([sys.executable,'-c',code,str(app_port),model_base,str(Path(d)/'test.sqlite3')],cwd=ROOT,stdout=log,stderr=log))
            for _ in range(100):
                try: report['health']=req(app_base,'/health');break
                except Exception:time.sleep(.2)
            else:raise RuntimeError('Isolated app did not start')
            report['ui_served']='Hospitality Coach' in req(app_base,'/')
            report['agent']=req(app_base,'/agent',{'request':'Practice explaining the duration of the coffee tasting.'})
            session=req(app_base,'/sessions',{'skill':'duration','scenario_id':'short_visit'})
            report['practice']=req(app_base,'/sessions/'+session['id']+'/responses',{'response':'La degustación dura 20 minutos. ¿A qué hora desean comenzar?'})
            assert report['practice']['fact_check']['verdict'] in ('supported','unsupported','uncertain')
            report['review']=req(app_base,'/reviews',{'review':'The coffee was delicious, but finding the blue gate was difficult.'})
            report['memory']=req(app_base,'/memory',{'original':'coffee tasting','preferred':'degustación de café','context':'synthetic offline test','approved':True})
            report['memory_readback']=len(req(app_base,'/memory'))==1
            report['passed']=True
        except Exception as exc:
            report['passed']=False; report['error']=str(exc)
        finally:
            for p in reversed(processes):
                p.terminate()
                try:p.wait(timeout=10)
                except subprocess.TimeoutExpired:p.kill();p.wait()
    print(json.dumps(report,ensure_ascii=False,indent=2))
    return 0 if report.get('passed') else 1

if __name__=='__main__':
    if '--worker' in sys.argv:sys.exit(worker())
    if sys.platform!='darwin':sys.exit('This verifier requires macOS sandbox-exec; use an equivalent network namespace elsewhere.')
    sys.exit(subprocess.call(['/usr/bin/sandbox-exec','-p',POLICY,sys.executable,str(Path(__file__).resolve()),'--worker'],cwd=ROOT))
