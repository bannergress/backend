#!/usr/bin/env bash
# Root-owned helper for a target-specific forced-command SSH account.
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
temporary=''
cleanup() { if [[ -n $temporary ]]; then rm -f -- "$temporary"; fi; }
trap cleanup EXIT

# Pull and validate before touching the live service. No database/auth service recreation.
docker pull "$image"
expected=$(docker image inspect "$image" --format '{{.Id}}')

set_image() {
  temporary=$(mktemp "$directory/.ci-compose-XXXXXX") || return 1
  # Only the application's expected unquoted image entry is accepted.
  if ! awk -v image="$image" '
    /^  [^ ]/ { application = ($0 == "  application:") }
    application && /^    image:/ {
      if ($0 !~ /^    image: ghcr\.io\/bannergress\/bannergress-backend[^[:space:]#]*([[:blank:]]*(#.*)?)?$/) exit 1
      sub(/ghcr\.io\/bannergress\/bannergress-backend[^[:space:]#]*/, image)
      count++
    }
    { print }
    END { if (count != 1) exit 1 }
  ' "$compose" > "$temporary"; then
    echo 'Refusing deployment: expected exactly one application image entry.' >&2
    return 1
  fi
  chmod --reference="$compose" "$temporary" || return 1
  chown --reference="$compose" "$temporary" || return 1
  mv -f -- "$temporary" "$compose" || return 1
  temporary=''
}

healthy() {
  local container actual
  container=$(docker compose --project-directory "$directory" -f "$compose" ps --quiet application)
  [[ -n $container ]] || return 1
  actual=$(docker inspect "$container" --format '{{.Image}}')
  [[ $actual == "$expected" ]] || return 1
  for attempt in {1..60}; do
    if [[ $(docker inspect "$container" --format '{{.State.Running}}') != true ]]; then return 1; fi
    if curl --fail --silent --show-error --max-time 2 "http://127.0.0.1:$port/bnrs?limit=1" > /dev/null; then return 0; fi
    sleep 3
  done
  return 1
}

failed() {
  echo "Deployment failed for $1; requested image: $image. No rollback attempted. Operator recovery required; inspect Compose, application logs and database migrations." >&2
  docker compose --project-directory "$directory" -f "$compose" ps application >&2 || true
  # Do not print Compose/env files: they may contain secrets.
}
trap 'failed "$1"; exit 1' HUP INT TERM
if set_image && docker compose --project-directory "$directory" -f "$compose" up --detach --no-deps --pull never application && healthy; then
  trap - HUP INT TERM
  printf '%s %s %s\n' "$(date -u +%FT%TZ)" "$1" "$image" > "$directory/.ci-deployment-receipt"
  echo "Verified $1 deployment of $image"
else
  failed "$1"
  exit 1
fi
