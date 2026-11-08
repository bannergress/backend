#!/usr/bin/env bash
# Install root-owned and invoke through a dedicated restricted SSH key; see docs/backend-delivery.md.
set -euo pipefail
umask 077

if [[ $# != 2 || ! $1 =~ ^(test|production)$ || ! $2 =~ ^ghcr\.io/bannergress/bannergress-backend@sha256:[0-9a-f]{64}$ ]]; then
  echo 'Usage: deploy-backend.sh test|production ghcr.io/bannergress/bannergress-backend@sha256:DIGEST' >&2
  exit 64
fi
if [[ $(id -u) != 0 ]]; then
  echo 'This server-side deployment helper must run as root.' >&2
  exit 77
fi
case "$1" in
  test) directory=/opt/backendtest; port=8082 ;;
  production) directory=/opt/backend; port=8081 ;;
esac
image=$2
exec 9>"$directory/.ci-deploy.lock"
flock -w 300 9
compose="$directory/compose.yaml"
previous="$directory/.ci-previous-compose.yaml"

# Pull before touching the live service. Never recreate DB/auth/other Compose services.
docker pull "$image"
expected=$(docker image inspect "$image" --format '{{.Id}}')
previous_container=$(docker compose --project-directory "$directory" -f "$compose" ps --quiet application)
[[ -n $previous_container ]]
previous_expected=$(docker inspect "$previous_container" --format '{{.Image}}')
cp --preserve=mode,ownership "$compose" "$previous"
chmod 600 "$previous"

set_image() {
  python3 - "$compose" "$image" <<'PY'
import os
from pathlib import Path
import re
import sys
import tempfile

path = Path(sys.argv[1])
body = path.read_text()
pattern = r'(?m)^(    image: )ghcr\.io/bannergress/bannergress-backend[^\s#]*(\s*(?:#[^\n]*)?)$'
updated, count = re.subn(pattern, lambda match: match[1] + sys.argv[2] + match[2], body)
if count != 1:
    raise SystemExit('Refusing deployment: expected exactly one application image entry')
metadata = path.stat()
fd, temp = tempfile.mkstemp(prefix='.ci-compose-', dir=path.parent)
try:
    with os.fdopen(fd, 'w') as stream:
        stream.write(updated)
        stream.flush()
        os.fsync(stream.fileno())
    os.chmod(temp, metadata.st_mode & 0o777)
    os.chown(temp, metadata.st_uid, metadata.st_gid)
    os.replace(temp, path)
finally:
    if os.path.exists(temp):
        os.unlink(temp)
PY
}

compose_up() {
  docker compose --project-directory "$directory" -f "$compose" up --detach --no-deps --pull never application
}

healthy() {
  local container actual
  container=$(docker compose --project-directory "$directory" -f "$compose" ps --quiet application)
  [[ -n $container ]] || return 1
  actual=$(docker inspect "$container" --format '{{.Image}}')
  [[ $actual == "$expected" ]] || return 1
  for attempt in {1..60}; do
    if [[ $(docker inspect "$container" --format '{{.State.Running}}') != true ]]; then
      return 1
    fi
    if curl --fail --silent --show-error --max-time 2 "http://127.0.0.1:$port/bnrs?limit=1" > /dev/null; then
      return 0
    fi
    sleep 3
  done
  return 1
}

rollback() {
  echo 'Deployment failed; restoring previous application image/configuration (not the database).' >&2
  cp --preserve=mode,ownership "$previous" "$compose"
  compose_up
  expected=$previous_expected
  if ! healthy; then
    echo 'CRITICAL: the previous application did not recover; operator intervention required.' >&2
    return 1
  fi
  echo 'Previous application image is healthy; database was not restored.' >&2
}

# An interrupted rollout must restore the old application; migrations must remain backward compatible.
trap 'rollback; exit 1' HUP INT TERM
if set_image && compose_up && healthy; then
  trap - HUP INT TERM
  printf '%s %s %s\n' "$(date -u +%FT%TZ)" "$1" "$image" > "$directory/.ci-deployment-receipt"
  echo "Verified $1 deployment of $image"
else
  rollback
  exit 1
fi
