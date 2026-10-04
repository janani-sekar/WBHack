"""Create a source-only judge package; never include private data or model weights."""
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

ROOT = Path(__file__).resolve().parents[1]
ALLOWED_DIRS = ('hospitality', 'docs', 'data/curriculum', 'data/sample', 'tests', 'scripts', 'evaluation')
EXCLUDED_PARTS = {'private', '__pycache__', '.venv', 'node_modules', 'build', '.git'}
ALLOWED_SUFFIXES = {'.py', '.md', '.json', '.html', '.txt'}

def build(destination=None):
    target = Path(destination) if destination else ROOT / 'dist/guest-i-mate-demo.zip'
    target.parent.mkdir(parents=True, exist_ok=True)
    paths = set()
    for directory in ALLOWED_DIRS:
        for p in (ROOT / directory).rglob('*'):
            if p.is_file() and p.suffix in ALLOWED_SUFFIXES and not EXCLUDED_PARTS.intersection(p.relative_to(ROOT).parts):
                paths.add(p)
    for name in ('README.md', 'requirements-agent.txt', 'LICENSE'):
        p = ROOT / name
        if p.is_file(): paths.add(p)
    manifest = {}
    with ZipFile(target, 'w', ZIP_DEFLATED) as archive:
        for p in sorted(paths):
            name = p.relative_to(ROOT).as_posix()
            data = p.read_bytes()
            manifest[name] = hashlib.sha256(data).hexdigest()
            archive.writestr('guest-i-mate-demo/' + name, data)
        archive.writestr('guest-i-mate-demo/PACKAGE-MANIFEST.json', json.dumps(manifest, indent=2))
        archive.writestr('guest-i-mate-demo/START-HERE.txt',
            'guest-i-mate - AI Hospitality Coach source demo\n\n'
            'Requires Python 3.11+ and Ollama. In this directory:\n'
            'python3 -m venv .venv\n'
            '.venv/bin/pip install -r requirements-agent.txt\n'
            'ollama pull qwen3:4b-instruct\n'
            '.venv/bin/python -m hospitality.server --model qwen3:4b-instruct\n'
            'Open http://127.0.0.1:8765 in a browser.\n\n'
            'Keep Ollama running. Internet is needed for initial setup; inference is local.\n'
            'This is source, not a standalone executable. Android sources are in the repository.\n'
            'Read docs/SHAREABLE_DEMO.md and docs/OFFLINE.md for scope and evidence.\n')
    return target, len(paths)

if __name__ == '__main__':
    target, count = build()
    print(f'{target}: {count} source files')
