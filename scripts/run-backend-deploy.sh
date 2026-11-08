#!/usr/bin/env bash
# Runner-side delivery. Credentials stay in private files, never SSH arguments.
set -euo pipefail
umask 077
for name in DEPLOY_ENVIRONMENT DEPLOY_IMAGE SSH_HOST SSH_PORT SSH_USER SSH_PRIVATE_KEY SSH_KNOWN_HOSTS; do
  if [[ -z ${!name:-} ]]; then
    echo "Missing deployment configuration: $name" >&2
    exit 64
  fi
done
[[ $DEPLOY_ENVIRONMENT =~ ^(test|production)$ ]]
[[ $DEPLOY_IMAGE =~ ^ghcr\.io/bannergress/bannergress-backend@sha256:[0-9a-f]{64}$ ]]
[[ $SSH_HOST =~ ^[a-zA-Z0-9][a-zA-Z0-9.-]*$ ]]
[[ $SSH_USER =~ ^[a-z_][a-z0-9_-]*$ ]]
[[ $SSH_PORT =~ ^[0-9]{1,5}$ ]] && (( 10#$SSH_PORT >= 1 && 10#$SSH_PORT <= 65535 ))
directory=$(mktemp -d)
trap 'rm -rf -- "$directory"' EXIT
trap 'exit 1' HUP INT TERM
printf '%s\n' "$SSH_PRIVATE_KEY" > "$directory/key"
printf '%s\n' "$SSH_KNOWN_HOSTS" > "$directory/known_hosts"
ssh -T -i "$directory/key" -p "$SSH_PORT" \
  -o BatchMode=yes -o IdentitiesOnly=yes -o StrictHostKeyChecking=yes \
  -o "UserKnownHostsFile=$directory/known_hosts" -o ConnectTimeout=20 \
  "$SSH_USER@$SSH_HOST" "deploy $DEPLOY_ENVIRONMENT $DEPLOY_IMAGE"
