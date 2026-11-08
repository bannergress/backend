#!/usr/bin/env python3
"""Runner-side SSH delivery: no credential arguments, shell evaluation or unpinned host keys."""
import os
from pathlib import Path
import re
import subprocess
import tempfile


def deploy(env=None):
    env = os.environ if env is None else env
    required = ["DEPLOY_ENVIRONMENT", "DEPLOY_IMAGE", "SSH_HOST", "SSH_PORT", "SSH_USER",
                "SSH_PRIVATE_KEY", "SSH_KNOWN_HOSTS"]
    if any(not env.get(key, "").strip() for key in required):
        raise ValueError("Deployment configuration is incomplete; configure the target GitHub environment")
    target = env["DEPLOY_ENVIRONMENT"]
    image = env["DEPLOY_IMAGE"]
    if target not in ("test", "production"):
        raise ValueError("Unsupported target")
    if not re.fullmatch(r"ghcr\.io/bannergress/bannergress-backend@sha256:[0-9a-f]{64}", image):
        raise ValueError("A pinned backend image digest is required")
    if not re.fullmatch(r"[a-zA-Z0-9.-]+", env["SSH_HOST"]):
        raise ValueError("Invalid host")
    if not re.fullmatch(r"[a-z_][a-z0-9_-]*", env["SSH_USER"]):
        raise ValueError("Invalid SSH user")
    if not env["SSH_PORT"].isdigit() or not 1 <= int(env["SSH_PORT"]) <= 65535:
        raise ValueError("Invalid SSH port")
    with tempfile.TemporaryDirectory(prefix="backend-deploy-") as directory:
        key = Path(directory, "key")
        hosts = Path(directory, "known_hosts")
        key.write_text(env["SSH_PRIVATE_KEY"] + "\n")
        hosts.write_text(env["SSH_KNOWN_HOSTS"] + "\n")
        key.chmod(0o600)
        hosts.chmod(0o600)
        subprocess.run(["ssh", "-T", "-i", str(key), "-p", env["SSH_PORT"],
                        "-o", "BatchMode=yes", "-o", "IdentitiesOnly=yes",
                        "-o", "StrictHostKeyChecking=yes", "-o", f"UserKnownHostsFile={hosts}",
                        "-o", "ConnectTimeout=20", f'{env["SSH_USER"]}@{env["SSH_HOST"]}',
                        f"deploy {target} {image}"], check=True, timeout=840)


if __name__ == "__main__":
    deploy()
