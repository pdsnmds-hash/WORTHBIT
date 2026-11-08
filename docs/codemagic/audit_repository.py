"""Reject private files and authoring provenance in checkout and packaged resources."""
import os
from pathlib import Path
import re
import struct
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
MARKERS = re.compile('|'.join([r'generated\s+by\s+(?:' + 'chat' + r'gpt|open' + r'ai|cod' + r'ex)',
                             'c2' + 'pa', 'content' + 'credentials', 'chat' + 'gpt',
                             'open' + 'ai', 'anthro' + 'pic', 'claude' + r'\s+(?:code|ai)']), re.I)
PRIVATE = {'.jks', '.keystore', '.p12', '.pfx', '.key', '.pem', '.apk', '.aab', '.zip'}


def inspect(name, data):
    path = Path(name)
    if path.suffix.lower() in PRIVATE or (path.name in {'local.properties', 'upload-key.properties', 'signing.properties'} or path.name.startswith('.env')):
        raise SystemExit('Disallowed private/generated file: ' + name)
    if data.startswith(b'\x89PNG\r\n\x1a\n'):
        offset = 8
        while offset + 12 <= len(data):
            length = struct.unpack('>I', data[offset:offset + 4])[0]
            chunk = data[offset + 4:offset + 8]
            payload = data[offset + 8:offset + 8 + length]
            if chunk == b'caBX' or (chunk in {b'tEXt', b'iTXt', b'zTXt', b'eXIf'} and MARKERS.search(payload.decode('latin1'))):
                raise SystemExit('Authoring metadata in image: ' + name)
            offset += 12 + length
    if MARKERS.search(data.decode('utf-8', errors='replace')):
        raise SystemExit('Authoring marker in: ' + name)



def audit_archive(path):
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if name.endswith('/'):
                continue
            inspect(name, archive.read(name))


def checkout_files():
    probe = subprocess.run(['git', '-C', str(ROOT), 'rev-parse', '--show-toplevel'], capture_output=True, text=True)
    if probe.returncode == 0:
        if Path(probe.stdout.strip()) != ROOT:
            raise SystemExit('Run audit with the project as repository root')
        return subprocess.check_output(['git', '-C', str(ROOT), 'ls-files', '--cached', '--others', '--exclude-standard', '-z']).decode().split('\0')[:-1]
    # Same Git ignore/index semantics without initializing or changing the user's repository.
    with tempfile.TemporaryDirectory() as folder:
        gitdir = str(Path(folder) / 'audit.git')
        subprocess.run(['git', 'init', '--bare', '-q', gitdir], check=True)
        command = ['git', '--git-dir=' + gitdir, '--work-tree=' + str(ROOT)]
        subprocess.run(command + ['add', '--all'], cwd=ROOT, check=True)
        return subprocess.check_output(command + ['ls-files', '-z'], cwd=ROOT).decode().split('\0')[:-1]


def main():
    files = checkout_files()
    forbidden_dirs = {'build', '.gradle', '.kotlin', 'generated-signing', 'deliverables', '.codex', '.claude'}
    for name in files:
        if forbidden_dirs.intersection(Path(name).parts) or name.startswith(('docs/refactoring/', 'docs/compatibility/', 'screens/')):
            raise SystemExit('Generated/private directory tracked: ' + name)
        inspect(name, (ROOT / name).read_bytes())
    print('PASS checkout audit: ' + str(len(files)) + ' files; no private files or authoring markers')


if __name__ == '__main__':
    main()
