"""Loopback-only HTTP API for the desktop development harness."""
import argparse
import json
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse
from pathlib import Path
from .coach import Coach, SCENARIOS
from .model import OllamaModel, ModelError


def make_handler(coach):
    class Handler(BaseHTTPRequestHandler):
        def log_message(self, *args):
            pass  # Do not log learner/review content.

        def send(self, code, data):
            payload = json.dumps(data, ensure_ascii=False).encode()
            self.send_response(code)
            self.send_header('Content-Type', 'application/json; charset=utf-8')
            self.send_header('Cache-Control', 'no-store')
            self.send_header('X-Content-Type-Options', 'nosniff')
            self.send_header('Content-Length', str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)

        def do_GET(self):
            self.dispatch('GET')

        def do_POST(self):
            self.dispatch('POST')

        def do_DELETE(self):
            self.dispatch('DELETE')

        def dispatch(self, method):
            host = self.headers.get('Host', '')
            if urlparse('http://' + host).hostname not in ('127.0.0.1','localhost','::1'):
                return self.send(403, {'error': 'Loopback Host required'})
            origin = self.headers.get('Origin')
            if (origin and origin != 'http://' + host) or self.headers.get('Sec-Fetch-Site') == 'cross-site':
                return self.send(403, {'error': 'Same-origin local requests only'})
            if method == 'GET' and urlparse(self.path).path == '/':
                payload = (Path(__file__).parent / 'ui' / 'index.html').read_bytes()
                self.send_response(200)
                self.send_header('Content-Type', 'text/html; charset=utf-8')
                self.send_header('Cache-Control', 'no-store')
                self.send_header('X-Content-Type-Options', 'nosniff')
                self.send_header('Content-Security-Policy', "default-src 'none'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; connect-src 'self'; img-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'")
                self.send_header('Content-Length', str(len(payload)))
                self.end_headers()
                self.wfile.write(payload)
                return
            try:
                body = {}
                if method == 'POST':
                    if self.headers.get('Content-Type','').split(';')[0] != 'application/json':
                        raise ValueError('Content-Type must be application/json')
                    size = int(self.headers.get('Content-Length', '0'))
                    if not 0 < size <= 16384:
                        raise ValueError('Request must be 1–16384 bytes')
                    body = json.loads(self.rfile.read(size))
                    if not isinstance(body, dict):
                        raise ValueError('JSON object required')
                path = urlparse(self.path).path.strip('/').split('/')
                if method == 'GET' and path == ['health']:
                    result = coach.model.health()
                elif method == 'POST' and path == ['agent']:
                    try:
                        from .agent import HospitalityAgent
                    except ImportError:
                        raise ModelError('Agent dependencies missing. Start with .venv/bin/python after installing requirements-agent.txt')
                    result = HospitalityAgent(coach).run(**body)
                elif method == 'GET' and path == ['curriculum']:
                    from .curriculum import CASES, public_case
                    result = [public_case(c) for c in CASES.values()]
                elif method == 'GET' and path == ['learning-plan']:
                    result = coach.learning_plan()
                elif method == 'GET' and path == ['scenarios']:
                    result = SCENARIOS
                elif method == 'GET' and path == ['learning-settings']:
                    result = coach.learning_settings()
                elif method == 'GET' and path == ['learner-memory']:
                    result = coach.learner_memory()
                elif method == 'POST' and len(path) == 3 and path[0] == 'sessions' and path[2] == 'coach-language':
                    result = coach.coaching_language(path[1], **body)
                elif method == 'POST' and len(path) == 3 and path[0] == 'sessions' and path[2] == 'finish':
                    result = coach.session_summary(path[1])
                elif method == 'POST' and path == ['learning-settings']:
                    result = coach.save_learning_settings(**body)
                elif method == 'POST' and len(path) == 3 and path[0] == 'sessions' and path[2] == 'language':
                    result = coach.guest_language(path[1], **body)
                elif method == 'GET' and path == ['profile']:
                    result = coach.get('profile', 'default')
                elif method == 'POST' and path == ['profile']:
                    result = coach.save_profile(**body)
                elif method == 'GET' and path == ['progress']:
                    result = coach.progress()
                elif method == 'GET' and path == ['memory']:
                    result = coach.all('memory')
                elif method == 'POST' and path == ['memory']:
                    result = coach.remember(**body)
                elif method == 'DELETE' and len(path) == 2 and path[0] == 'memory':
                    result = coach.forget(path[1])
                elif method == 'POST' and path == ['sessions']:
                    result = coach.start(**body)
                elif method == 'GET' and len(path) == 2 and path[0] == 'sessions':
                    result = coach.get('session', path[1])
                elif method == 'POST' and len(path) == 3 and path[0] == 'sessions' and path[2] == 'responses':
                    result = coach.respond(path[1], **body)
                elif method == 'POST' and len(path) == 3 and path[0] == 'sessions' and path[2] == 'next':
                    result = coach.next_guest(path[1])
                elif method == 'POST' and len(path) == 3 and path[0] == 'sessions' and path[2] == 'assessment':
                    result = coach.approve_assessment(path[1], **body)
                elif method == 'POST' and path == ['review-batches']:
                    result = coach.review_batch(**body)
                elif method == 'GET' and path == ['review-batches']:
                    result = coach.all('review_batch')
                elif method == 'POST' and path == ['batch-lessons']:
                    result = coach.batch_lesson(**body)
                elif method == 'POST' and path == ['practice-drafts']:
                    result = coach.practice_draft(**body)
                elif method == 'POST' and len(path)==2 and path[0]=='drafts':
                    result = coach.approve_draft(path[1],**body)
                elif method == 'GET' and path == ['drafts']:
                    result = coach.all('draft')
                elif method == 'POST' and path == ['reviews']:
                    result = coach.understand_review(**body)
                elif method == 'GET' and path == ['reviews']:
                    result = coach.all('review')
                elif method == 'POST' and path == ['lessons']:
                    result = coach.create_lesson(**body)
                elif method == 'GET' and path == ['lessons']:
                    result = coach.all('lesson')
                elif method == 'POST' and path == ['reset']:
                    result = coach.reset(**body)
                else:
                    return self.send(404, {'error': 'Unknown route'})
                self.send(200, result)
            except ModelError as exc:
                self.send(503, {'error': str(exc), 'retryable': True, 'no_cloud_fallback': True})
            except KeyError:
                self.send(404, {'error': 'Record not found'})
            except (ValueError, TypeError) as exc:
                self.send(400, {'error': str(exc)})
    return Handler


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--model', choices=['qwen3:1.7b','qwen3:4b-instruct'], default='qwen3:1.7b')
    parser.add_argument('--port', type=int, default=8765)
    parser.add_argument('--db', default='data/private/coach.sqlite3')
    parser.add_argument('--constrained', action='store_true', help='CPU-only two-thread desktop surrogate, NOT an Android emulator')
    args = parser.parse_args()
    coach = Coach(OllamaModel(model=args.model, constrained=args.constrained), args.db)
    server = HTTPServer(('127.0.0.1', args.port), make_handler(coach))
    print(f'AI backend: http://127.0.0.1:{args.port} (desktop harness; Android unverified)', flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()
        coach.close()


if __name__ == '__main__':
    main()
