"""Delivery boundary tests. Never contacts SSH, Docker or application environments."""
import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("delivery", ROOT / "run-backend-deploy.py")
DELIVERY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DELIVERY)
IMAGE = "ghcr.io/bannergress/bannergress-backend@sha256:" + "a" * 64


class RunnerBoundaryTest(unittest.TestCase):
    def env(self):
        return {"DEPLOY_ENVIRONMENT": "test", "DEPLOY_IMAGE": IMAGE, "SSH_HOST": "example.org",
                "SSH_PORT": "22", "SSH_USER": "backend-test", "SSH_PRIVATE_KEY": "synthetic-key",
                "SSH_KNOWN_HOSTS": "synthetic-known-host"}

    def test_missing_config_unpinned_images_and_shell_input_rejected(self):
        for overrides in ({"SSH_PRIVATE_KEY": ""}, {"DEPLOY_ENVIRONMENT": "other"},
                          {"DEPLOY_IMAGE": "ghcr.io/bannergress/bannergress-backend:latest"},
                          {"SSH_HOST": "host;touch bad"}, {"SSH_PORT": "0"},
                          {"SSH_USER": "-oProxyCommand=bad"}):
            with self.subTest(overrides=overrides), patch.object(DELIVERY.subprocess, "run") as run:
                env = self.env() | overrides
                with self.assertRaises(ValueError):
                    DELIVERY.deploy(env)
                run.assert_not_called()

    def test_pinned_host_key_direct_argv_and_credentials_cleaned_up(self):
        paths = []

        def fake_run(args, **kwargs):
            self.assertIn("StrictHostKeyChecking=yes", args)
            self.assertIn("IdentitiesOnly=yes", args)
            self.assertNotIn("synthetic-key", " ".join(args))
            self.assertEqual(args[-1], f"deploy test {IMAGE}")
            key = Path(args[args.index("-i") + 1])
            paths.append(key)
            self.assertEqual(key.stat().st_mode & 0o777, 0o600)
            self.assertEqual(key.read_text().strip(), "synthetic-key")
            self.assertTrue(kwargs["check"])
            raise subprocess.CalledProcessError(1, ["ssh"])

        with patch.object(DELIVERY.subprocess, "run", fake_run):
            with self.assertRaises(subprocess.CalledProcessError):
                DELIVERY.deploy(self.env())
        self.assertFalse(paths[0].exists())


class ForcedCommandTest(unittest.TestCase):
    def test_rejects_shells_cross_environment_and_mutable_tags(self):
        commands = ["bash", f"deploy production {IMAGE}", f"deploy test {IMAGE}; id",
                    "deploy test ghcr.io/bannergress/bannergress-backend:latest"]
        for command in commands:
            with self.subTest(command=command):
                result = subprocess.run(["bash", str(ROOT / "backend-deploy-command.sh"), "test"],
                                        env=os.environ | {"SSH_ORIGINAL_COMMAND": command}, capture_output=True)
                self.assertNotEqual(result.returncode, 0)

    def test_allowed_command_forwards_only_validated_arguments(self):
        with tempfile.TemporaryDirectory() as directory:
            sudo = Path(directory, "sudo")
            sudo.write_text('#!/bin/sh\nprintf "%s\\n" "$@"\n')
            sudo.chmod(0o755)
            result = subprocess.run(["bash", str(ROOT / "backend-deploy-command.sh"), "test"],
                                    env=os.environ | {"SSH_ORIGINAL_COMMAND": f"deploy test {IMAGE}",
                                                      "PATH": directory + ":" + os.environ["PATH"]},
                                    check=True, text=True, capture_output=True)
            self.assertEqual(result.stdout.splitlines(),
                             ["-n", "/usr/local/sbin/bannergress-deploy-backend", "test", IMAGE])


class ServerRolloutTest(unittest.TestCase):
    def rollout(self, mode):
        with tempfile.TemporaryDirectory() as directory:
            work = Path(directory)
            deploy = work / "deployment"
            deploy.mkdir()
            old = "ghcr.io/bannergress/bannergress-backend:v2.8"
            compose = deploy / "compose.yaml"
            original = f"services:\n  application:\n    image: {old}\n    env_file: private.env\n    restart: unless-stopped\n"
            compose.write_text(original)
            state = work / "current-image"
            state.write_text(old)
            log = work / "docker-calls"
            binary = work / "bin"
            binary.mkdir()
            fake = binary / "docker"
            fake.write_text('''#!/usr/bin/env python3
import os
from pathlib import Path
import re
import sys
args=sys.argv[1:]
state=Path(os.environ['FAKE_STATE'])
with open(os.environ['FAKE_LOG'],'a') as f: f.write(' '.join(args)+'\\n')
if args[0]=='pull': sys.exit(1 if os.environ['FAKE_MODE']=='pull-failure' else 0)
if args[:2]==['image','inspect']: print(os.environ['FAKE_NEW_IMAGE'])
elif args[0]=='inspect':
    print('true' if 'Running' in args[-1] else state.read_text())
elif args[0]=='compose':
    if 'ps' in args: print('application-container')
    elif 'up' in args:
        assert '--no-deps' in args and args[-1]=='application'
        body=Path(args[args.index('-f')+1]).read_text()
        state.write_text(re.search(r'    image: (\\S+)',body)[1])
    else: sys.exit(2)
else: sys.exit(2)
''')
            fake.chmod(0o755)
            (binary / "id").write_text('#!/bin/sh\necho 0\n')
            (binary / "sleep").write_text('#!/bin/sh\nexit 0\n')
            (binary / "curl").write_text('''#!/usr/bin/env python3
import os
from pathlib import Path
import sys
new=Path(os.environ['FAKE_STATE']).read_text()==os.environ['FAKE_NEW_IMAGE']
sys.exit(1 if new and os.environ['FAKE_MODE']=='health-failure' else 0)
''')
            for name in ("id", "sleep", "curl"):
                (binary / name).chmod(0o755)
            script = work / "deploy.sh"
            script.write_text((ROOT / "deploy-backend.sh").read_text().replace("/opt/backendtest", str(deploy)))
            result = subprocess.run(["bash", str(script), "test", IMAGE], text=True, capture_output=True,
                                    timeout=20, env=os.environ | {"PATH": str(binary) + ":" + os.environ["PATH"],
                                        "FAKE_STATE": str(state), "FAKE_LOG": str(log),
                                        "FAKE_NEW_IMAGE": IMAGE, "FAKE_MODE": mode})
            return result, compose.read_text(), original, state.read_text(), log.read_text()

    def test_success_pins_only_application_image_and_preserves_configuration(self):
        result, compose, original, state, log = self.rollout("success")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(compose, original.replace("ghcr.io/bannergress/bannergress-backend:v2.8", IMAGE))
        self.assertEqual(state, IMAGE)
        self.assertNotIn(" down", log)
        self.assertNotIn("database", log)

    def test_readiness_failure_restores_previous_image_and_verifies_it(self):
        result, compose, original, state, _ = self.rollout("health-failure")
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(compose, original)
        self.assertEqual(state, "ghcr.io/bannergress/bannergress-backend:v2.8")
        self.assertIn("Previous application image is healthy", result.stderr)

    def test_pull_failure_does_not_touch_compose(self):
        result, compose, original, _, _ = self.rollout("pull-failure")
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(compose, original)


if __name__ == "__main__":
    unittest.main()
