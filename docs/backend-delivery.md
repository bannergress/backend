# Backend CI and release delivery

Independent CI/CD infrastructure: no dependency on application features or unmerged feature branches.

## Trigger policy

| Event | Verification | Image/deployment |
| --- | --- | --- |
| PR targeting `main` | Java 25 tests, Checkstyle, JAR and Bash delivery tests | None; no deployment secrets |
| Merge/push to `main` | Same gates | Publish immutable image, **test only** |
| Manual workflow dispatch on `main` | Same gates | **Test only** |
| Manually published stable GitHub Release | Same gates; commit must be reachable from `main` | **Production only** |
| Draft or prerelease | Published prereleases may verify | No delivery |
| Plain tag push | No workflow triggered | No delivery |

Publish a stable Release manually in GitHub from a reviewed main-reachable tag. There is no production workflow-dispatch selector. A Release created by another workflow's `GITHUB_TOKEN` may not emit downstream events. Never publish an unreviewed revision to test delivery.

## Integration with the current main branch

Java 25, Gradle 9.9.0 and current action majors from main are retained. The standalone manual `deploy.yml` introduced by PR492 is superseded by this single gated pipeline: its buildpack image build and direct GitHub environment binding remain, but its environment selector, mutable version-tag deployment and unrestricted remote Compose command do not. The old `container.yaml` was already removed by PR492 and is not restored. No parallel manual production or tag-push publication path remains. The existing restricted host helpers and five environment secrets are unchanged; `SSH_TARGET` is not used.

## GitHub environments

Like website PR553, the deployment job directly binds its `environment` and concurrency group to its destination. The backend has a stricter production trigger: `release` chooses `production`; main push/manual dispatch chooses `test`. Prereleases/drafts cannot enter the image job. The deployment job rechecks stable publication and main ancestry; test rejects superseded main commits.

Secrets are read from the selected **GitHub environment**, not repository-level credentials:

| Environment secret | Purpose |
| --- | --- |
| `SSH_HOST` | Verified hostname |
| `SSH_PORT` | Existing SSH port |
| `SSH_USER` | Environment-specific restricted deploy account |
| `SSH_PRIVATE_KEY` | Dedicated account key |
| `SSH_KNOWN_HOSTS` | Independently pinned port-qualified host key |

Do not scan/trust a new host key during deployment. The Bash runner validates inputs, writes credentials with mode 600 inside a private temporary directory, calls SSH with strict host checking and removes files on exit/failure. No deployment helper, client or delivery test requires Python.

The image job uses the existing Spring Boot buildpacks, publishes a SHA-tagged image, and resolves `ghcr.io/bannergress/bannergress-backend@sha256:DIGEST`. The server accepts only this repository and immutable digest. PR permissions are read-only; only image publication has `packages: write`. Per-environment deployment concurrency does not cancel an active rollout. Existing GitHub environment reviewers/wait rules can impose stronger controls.

## Host setup and access

Separate locked-password `backend-deploy-test` and `backend-deploy-prod` accounts have no Docker group/general sudo/interactive SSH access. Their root-owned authorized keys use `restrict,command="/usr/local/sbin/bannergress-backend-deploy-command TARGET"`. The wrapper validates exact command/environment/digest without `eval` and rejects cross-environment access before sudo.

Install reviewed helpers as root-owned, non-writable-by-deployer files:

```sh
install -o root -g root -m 0755 scripts/deploy-backend.sh /usr/local/sbin/bannergress-deploy-backend
install -o root -g root -m 0755 scripts/backend-deploy-command.sh /usr/local/sbin/bannergress-backend-deploy-command
```

Validate narrow account-specific sudoers rules with `visudo -cf FILE` and `visudo -c`:

```text
backend-deploy-test ALL=(root) NOPASSWD: /usr/local/sbin/bannergress-deploy-backend test *
backend-deploy-prod ALL=(root) NOPASSWD: /usr/local/sbin/bannergress-deploy-backend production *
```

The root helper revalidates arguments before any operation. Keep Compose/env files root-owned. Deployment of trusted application images remains privileged; review source before a Release. Images must be publicly pullable or the host needs independently provisioned read-only registry access, never a write-capable CI token.

Provisioning status: both restricted accounts, pinned keys and all five secrets per environment are installed; test permits main refs and production permits tag refs. Recovery copies of dedicated keys are stored in the protected vault. This is access verification, **not** proof of a GitHub-runner application rollout. The first rollout remains unexercised.

## Rollout and failure handling

| Environment | Compose project | Readiness |
| --- | --- | --- |
| Test | `/opt/backendtest` | `http://127.0.0.1:8082/bnrs?limit=1` |
| Production | `/opt/backend` | `http://127.0.0.1:8081/bnrs?limit=1` |

The helper takes a per-target lock, pulls before mutation, validates exactly one expected application image entry, and atomically replaces it while preserving other lines, permissions and ownership. Unexpected layouts are rejected. It runs only `application` with `--no-deps --pull never`, checks the exact running image ID and probes readiness for up to five minutes. Other applications, databases, Keycloak and bots are not recreated. Temporary Compose files are removed on exit.

**There is no automated rollback, previous-image backup or database restore.** Application-only rollback cannot undo migrations. Failed startup/readiness reports the target, requested image and Compose application status, exits nonzero and requires operator recovery. The current application/configuration and database may already have changed. Do not assume the previous image is safe to run. Inspect application logs, Compose state and database/migration state before deciding recovery. Never restore SQL over live data automatically.

`.ci-deployment-receipt` records the last successful time/environment/digest; after a failure it may describe an older deployment and is not proof of current state. On SSH interruption or host failure inspect actual container/Compose/database state before retrying. A readiness check does not replace functional acceptance. Verify backup/restore coverage before releases with migrations; no global Docker pruning or whole-project restarts are part of recovery.

## Verification

```sh
bash -n scripts/*.sh
bash scripts/test-backend-delivery.sh
actionlint .github/workflows/check-pr.yaml
./gradlew test checkstyleMain checkstyleTest bootJar --no-daemon
```

Bash-only tests use fake SSH/Docker/curl and disposable Compose files. They cover runner input rejection, pinned host checking/credential cleanup, forced-command isolation, image-only success, failed pull without mutation, startup/readiness failure without rollback, malformed/duplicate application entries and temporary-file cleanup. They never contact a live environment.
