"""Local-only Ollama adapter, structured output validation, no cloud fallback."""
import json
import time
import urllib.error
import urllib.request
from urllib.parse import urlparse


class ModelError(RuntimeError):
    pass


def validate(value, schema, path='$'):
    kind = schema.get('type')
    valid = {'object': isinstance(value, dict), 'array': isinstance(value, list),
             'string': isinstance(value, str), 'integer': type(value) is int,
             'boolean': type(value) is bool}.get(kind, False)
    if not valid:
        raise ModelError(f'Invalid {kind} at {path}')
    if 'enum' in schema and value not in schema['enum']:
        raise ModelError(f'Invalid choice at {path}')
    if kind == 'object':
        props = schema['properties']
        if set(value) != set(props):
            raise ModelError(f'Unexpected or missing fields at {path}')
        for key, item in value.items():
            validate(item, props[key], f'{path}.{key}')
    if kind == 'array':
        if len(value) > schema.get('maxItems', 20):
            raise ModelError(f'Too many items at {path}')
        for item in value:
            validate(item, schema['items'], path+'[]')
    if kind == 'string' and not 0 < len(value) <= schema.get('maxLength', 2000):
        raise ModelError(f'Invalid text length at {path}')
    if kind == 'integer' and not schema.get('minimum', 0) <= value <= schema.get('maximum', 2):
        raise ModelError(f'Invalid score at {path}')


def obj(**fields):
    return {'type': 'object', 'properties': fields, 'required': list(fields), 'additionalProperties': False}


TEXT = {'type': 'string', 'maxLength': 600}
BOOL = {'type': 'boolean'}
FACT_CHECK = obj(verdict={'type':'string','enum':['unsupported','supported','uncertain']},reason=TEXT)
SCORE = {'type': 'integer', 'minimum': 0, 'maximum': 2}
SKILLS = ('duration', 'directions', 'expectations')
SKILL = {'type': 'string', 'enum': list(SKILLS)}
TURN = obj(guest_message=TEXT)
ASSESSMENT = obj(strength_local=TEXT, improvement_local=TEXT, evidence_quote=TEXT,
                 uncertain=BOOL, answers_request=SCORE, factual_accuracy=SCORE,
                 clarifies_unknowns=SCORE, next_step=SCORE, customer_tone=SCORE)
THEME = obj(kind={'type': 'string', 'enum': ['positive', 'concern', 'suggestion']},
            explanation_local=TEXT, evidence_quote=TEXT, skill=SKILL,
            training_relevant=BOOL)
REVIEW = obj(translation_local=TEXT, explanation_local=TEXT, uncertain=BOOL,
             themes={'type': 'array', 'items': THEME, 'maxItems': 2})


class OllamaModel:
    def __init__(self, base_url='http://127.0.0.1:11434', model='qwen3:1.7b', timeout=120, constrained=False):
        u = urlparse(base_url)
        if (u.scheme != 'http' or u.hostname not in ('127.0.0.1', '::1', 'localhost')
                or u.username or u.password or u.path not in ('', '/') or u.query or u.fragment):
            raise ValueError('Inference endpoint must be loopback HTTP')
        if model not in ('qwen3:1.7b', 'qwen3:4b-instruct'):
            raise ValueError('This build only permits approved local Qwen models')
        self.base_url = base_url.rstrip('/')
        self.model = model
        self.timeout = timeout
        self.constrained = constrained
        self.http = urllib.request.build_opener(urllib.request.ProxyHandler({}), NoRedirect())

    def _request(self, path, data=None):
        req = urllib.request.Request(self.base_url + path,
            data=None if data is None else json.dumps(data).encode(),
            headers={'Content-Type': 'application/json'})
        try:
            with self.http.open(req, timeout=self.timeout) as response:
                raw = response.read(1_000_001)
            if len(raw) > 1_000_000:
                raise ModelError('Model response too large')
            return json.loads(raw)
        except (OSError, ValueError, urllib.error.URLError) as exc:
            raise ModelError('Local model unavailable or returned invalid data. Start Ollama and install the selected local model.') from exc

    def health(self):
        data = self._request('/api/tags')
        return {'runtime': 'ollama', 'model': self.model,
                'model_installed': any(x.get('name') == self.model for x in data.get('models', [])),
                'deployment': 'desktop-development-harness', 'android_verified': False,
                'constrained_profile': self.constrained}

    def generate(self, instruction, context, schema):
        start = time.monotonic()
        response = self._request('/api/chat', {
            'model': self.model, 'stream': False, 'think': False,
            'format': schema,
            'messages': [
                {'role': 'system', 'content': instruction + '\nReturn only the JSON object matching the supplied schema. /no_think'},
                {'role': 'user', 'content': json.dumps(context, ensure_ascii=False)}],
            'options': {'temperature': 0.15, 'num_ctx': 2048 if self.constrained else 4096,
                        'num_predict': 1200, **({'num_thread': 2, 'num_gpu': 0} if self.constrained else {})},
            'keep_alive': '5m'})
        try:
            if response.get('done_reason') == 'length':
                raise ModelError('Model response was truncated; try a shorter input')
            result = json.loads(response['message']['content'])
            validate(result, schema)
        except (KeyError, TypeError, ValueError) as exc:
            raise ModelError('Model output failed validation; no assessment or review was saved') from exc
        return result, {'model': self.model, 'elapsed_seconds': round(time.monotonic()-start, 3),
                        'output_tokens': response.get('eval_count'),
                        'prompt_tokens': response.get('prompt_eval_count'),
                        'constrained_profile': self.constrained,
                        'provenance': 'real-local-model', 'android_verified': False}


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        raise ModelError('Redirects are disabled for local-only inference')
