#!/usr/bin/env bash
# Isolated boundary/rollout tests; never contacts a live host or Docker daemon.
set -euo pipefail
root=$(cd -- "$(dirname -- "$0")" && pwd)
work=$(mktemp -d)
trap 'rm -rf -- "$work"' EXIT
mkdir "$work/bin" "$work/deployment"
export DEPLOY_ENVIRONMENT=test DEPLOY_IMAGE="ghcr.io/bannergress/bannergress-backend@sha256:$(printf 'a%.0s' {1..64})"
export SSH_HOST=example.org SSH_PORT=22 SSH_USER=backend-test SSH_PRIVATE_KEY=synthetic-key SSH_KNOWN_HOSTS=synthetic-host
export FAKE_LOG="$work/log" FAKE_STATE="$work/state" FAKE_MODE=success FAKE_NEW_IMAGE="$DEPLOY_IMAGE"
export PATH="$work/bin:$PATH"
reject() { if "$@" > "$work/output" 2>&1; then echo "Unexpected success: $*" >&2; exit 1; fi; }
cat > "$work/bin/ssh" <<'SH'
#!/usr/bin/env bash
set -eu
printf '%s\n' "$@" > "$FAKE_LOG"
while (( $# )); do
  if [[ $1 == -i ]]; then
    key=$2
    [[ $(stat -c %a "$key") == 600 ]]
    [[ $(cat "$key") == synthetic-key ]]
    printf '%s\n' "$key" > "$FAKE_STATE"
  fi
  shift
done
exit 7
SH
chmod +x "$work/bin/ssh"
reject env SSH_PRIVATE_KEY= bash "$root/run-backend-deploy.sh"
reject env DEPLOY_ENVIRONMENT=other bash "$root/run-backend-deploy.sh"
reject env DEPLOY_IMAGE=ghcr.io/bannergress/bannergress-backend:latest bash "$root/run-backend-deploy.sh"
reject env SSH_HOST=-option bash "$root/run-backend-deploy.sh"
reject env SSH_HOST='host;id' bash "$root/run-backend-deploy.sh"
reject env SSH_USER=-option bash "$root/run-backend-deploy.sh"
reject env SSH_PORT=0 bash "$root/run-backend-deploy.sh"
reject env SSH_PORT=65536 bash "$root/run-backend-deploy.sh"
[[ ! -f $FAKE_LOG ]]
reject bash "$root/run-backend-deploy.sh"
[[ ! -e $(cat "$FAKE_STATE") ]]
grep -qx StrictHostKeyChecking=yes "$FAKE_LOG"
grep -qx IdentitiesOnly=yes "$FAKE_LOG"
grep -qx "deploy test $DEPLOY_IMAGE" "$FAKE_LOG"
! grep -q synthetic-key "$FAKE_LOG"
for command in bash "deploy production $DEPLOY_IMAGE" "deploy test $DEPLOY_IMAGE; id" 'deploy test ghcr.io/bannergress/bannergress-backend:latest'; do
  reject env SSH_ORIGINAL_COMMAND="$command" bash "$root/backend-deploy-command.sh" test
done
cat > "$work/bin/sudo" <<'SH'
#!/bin/sh
printf '%s\n' "$@"
SH
chmod +x "$work/bin/sudo"
SSH_ORIGINAL_COMMAND="deploy test $DEPLOY_IMAGE" bash "$root/backend-deploy-command.sh" test > "$work/forwarded"
printf '%s\n' -n /usr/local/sbin/bannergress-deploy-backend test "$DEPLOY_IMAGE" > "$work/expected"
cmp "$work/expected" "$work/forwarded"
echo 'PASS: runner validation, pinned SSH, credential cleanup and forced-command isolation'

cat > "$work/bin/docker" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "$*" >> "$FAKE_LOG"
case "$1" in
  pull) [[ $FAKE_MODE != pull-failure ]] ;;
  image) printf '%s\n' "$FAKE_NEW_IMAGE" ;;
  inspect)
    if [[ ${*: -1} == *Running* ]]; then echo true; else cat "$FAKE_STATE"; fi ;;
  compose)
    while [[ $1 != -f ]]; do shift; done
    compose=$2
    shift 2
    case "$1" in
      ps) echo application-container ;;
      up)
        [[ $* == 'up --detach --no-deps --pull never application' ]]
        awk '/^    image:/ {print $2}' "$compose" > "$FAKE_STATE"
        [[ $FAKE_MODE != up-failure ]] ;;
      *) exit 2 ;;
    esac ;;
  *) exit 2 ;;
esac
SH
cat > "$work/bin/curl" <<'SH'
#!/bin/sh
[ "$FAKE_MODE" != health-failure ]
SH
printf '#!/bin/sh\necho 0\n' > "$work/bin/id"
printf '#!/bin/sh\nexit 0\n' > "$work/bin/sleep"
chmod +x "$work/bin/"*
sed "s|/opt/backendtest|$work/deployment|g" "$root/deploy-backend.sh" > "$work/deploy.sh"
old=ghcr.io/bannergress/bannergress-backend:v2.8
for mode in success pull-failure health-failure up-failure invalid-layout duplicate-image; do
  export FAKE_MODE=$mode
  printf 'services:\n  application:\n    image: %s # keep comment\n    env_file: private.env\n    restart: unless-stopped\n' "$old" > "$work/original"
  if [[ $mode == invalid-layout ]]; then sed -i s/application:/other:/ "$work/original"; fi
  if [[ $mode == duplicate-image ]]; then printf '    image: %s\n' "$old" >> "$work/original"; fi
  cp "$work/original" "$work/deployment/compose.yaml"
  chmod 640 "$work/deployment/compose.yaml"
  printf '%s\n' "$old" > "$FAKE_STATE"
  : > "$FAKE_LOG"
  rm -f "$work/deployment/.ci-deployment-receipt"
  if [[ $mode == success ]]; then
    bash "$work/deploy.sh" test "$DEPLOY_IMAGE" > "$work/output" 2>&1
    sed "s|$old|$DEPLOY_IMAGE|" "$work/original" > "$work/expected"
    cmp "$work/expected" "$work/deployment/compose.yaml"
    [[ -f $work/deployment/.ci-deployment-receipt ]]
    [[ $(cat "$FAKE_STATE") == "$DEPLOY_IMAGE" ]]
  else
    reject bash "$work/deploy.sh" test "$DEPLOY_IMAGE"
    [[ ! -f $work/deployment/.ci-deployment-receipt ]]
    if [[ $mode == health-failure || $mode == up-failure ]]; then
      [[ $(cat "$FAKE_STATE") == "$DEPLOY_IMAGE" ]]
      grep -q 'No rollback attempted. Operator recovery required' "$work/output"
      [[ $(grep -c ' up ' "$FAKE_LOG") == 1 ]]
    else
      cmp "$work/original" "$work/deployment/compose.yaml"
      ! grep -q ' up ' "$FAKE_LOG"
    fi
  fi
  [[ $(stat -c %a "$work/deployment/compose.yaml") == 640 ]]
  [[ -z $(find "$work/deployment" -name '.ci-compose-*' -print) ]]
  ! grep -Eq ' down|database|keycloak' "$FAKE_LOG"
  echo "PASS: server $mode"
done
