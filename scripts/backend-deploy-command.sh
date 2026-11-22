#!/usr/bin/env bash
# Forced-command entrypoint for the dedicated deploy account. No shell interpolation of SSH input.
set -euo pipefail
target=${1:-}
if [[ $target != test && $target != production ]]; then
  exit 64
fi
if [[ ${SSH_ORIGINAL_COMMAND:-} =~ ^deploy\ (test|production)\ (ghcr\.io/bannergress/bannergress-backend@sha256:[0-9a-f]{64})$ ]]; then
  if [[ ${BASH_REMATCH[1]} != "$target" ]]; then
    echo 'This key is not authorized for that environment.' >&2
    exit 77
  fi
  exec sudo -n /usr/local/sbin/bannergress-deploy-backend "${BASH_REMATCH[1]}" "${BASH_REMATCH[2]}"
fi
echo 'Only a digest-pinned Bannergress backend deployment is permitted.' >&2
exit 64
