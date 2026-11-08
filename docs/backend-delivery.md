# Backend CI and release delivery

This pipeline is independent of application features. It changes workflows and deployment tooling only: no comment API, bot, database migration or dependency on an unmerged feature branch.

## Trigger policy

| Event | Verification | Image publication | Deployment |
| --- | --- | --- | --- |
| PR targeting `main` | Java 21 tests, Checkstyle, executable JAR, delivery boundary tests | No | None; no deploy secrets available to PR jobs |
| Merge/push to `main` | Same gates | SHA-tagged image, resolved to immutable digest | **Test only** |
| Manual workflow dispatch on `main` | Same gates | SHA-tagged image, resolved to immutable digest | **Test only** |
| Manually publish a normal GitHub Release | Same gates on the release commit; require it to be reachable from `main` | SHA-tagged image, resolved to immutable digest | **Production only** |
| Draft release or prerelease | No production rollout | A published prerelease may run verification, but cannot publish/deploy through this pipeline | None |
| Plain tag push | The pre-existing tag image workflow is unchanged | Existing version-image workflow only | None |

Publish a stable Release **manually in GitHub** after choosing a reviewed tag from `main`; do not merely push a tag. A Release created with `GITHUB_TOKEN` by another workflow may not emit downstream workflow events, so this is not an automated release-bump chain. Production has no unrestricted workflow-dispatch path. Do not publish an unreviewed application revision just to test the pipeline.

The previous CI targeted `master` and Java 11 despite the actual `main` branch and Java 21 toolchain. `Backend CI` now uses the real branch/toolchain and runs complete tests/lint/build before a deployment can be considered. A disposable PostgreSQL 16/PostGIS service is available to integration tests; existing tests do not need a feature PR to exist. Test artifacts are uploaded even on failure.

## Image and environment isolation

The image job retains Spring Boot's existing buildpack tooling, building the exact verified source revision as `ghcr.io/bannergress/bannergress-backend:sha-FULL_SHA`, then publishes and resolves its registry digest. The reusable deploy workflow receives only `ghcr.io/bannergress/bannergress-backend@sha256:DIGEST`, never a mutable tag. Test rejects superseded `main` revisions at rollout time; production accepts only the manually released, main-reachable revision. An older reviewed release commit can be deliberately released even if `main` has advanced.

GitHub environments `test` and `production` must have separate deploy credentials. Delivery is serialized per target and does not cancel an in-progress rollout. PR verification has read-only repository permissions; only the image publication job has `packages: write`. An image-publish failure prevents deployment. GitHub environment approvals/branch protection can add stricter gates without changing the workflow.

The existing host uses separate Compose projects, a single `application` service each:

| Environment | Compose directory | Local readiness endpoint |
| --- | --- | --- |
| Test | `/opt/backendtest` | `http://127.0.0.1:8082/bnrs?limit=1` |
| Production | `/opt/backend` | `http://127.0.0.1:8081/bnrs?limit=1` |

Deploying one does not restart the other, Keycloak, a database, search bot or other services. Images must be publicly pullable (the current backend repository is public), or the operator must independently provision a narrowly scoped read-only registry credential on the host; never copy a write-capable CI token there.

## One-time setup (required before merging/enabling delivery)

**This PR does not provision accounts, install root helpers, create deploy keys or populate GitHub secrets.** The current inspected repository/test environment had no Actions deploy secrets. Until the following is done, a deployment job intentionally fails closed before SSH rather than silently skipping delivery.

1. Review and install the server-side scripts as root-owned, non-writable-by-deployer files:

   ```sh
   install -o root -g root -m 0755 scripts/deploy-backend.sh /usr/local/sbin/bannergress-deploy-backend
   install -o root -g root -m 0755 scripts/backend-deploy-command.sh /usr/local/sbin/bannergress-backend-deploy-command
   ```

2. Provision **two distinct** locked-password deploy accounts/keypairs, one per environment. Do not add them to the Docker group, general sudo or interactive SSH access. Keep their `authorized_keys` and parent directory root-owned so the deployer cannot remove its forced-command restriction. Use key restrictions with an explicit environment, for example:

   ```text
   restrict,command="/usr/local/sbin/bannergress-backend-deploy-command test" ssh-ed25519 PUBLIC_KEY_FOR_TEST
   restrict,command="/usr/local/sbin/bannergress-backend-deploy-command production" ssh-ed25519 PUBLIC_KEY_FOR_PRODUCTION
   ```

   Store private keys only in the matching GitHub environment secret. The examples are two different accounts/files, not two keys granting both environments to one account. An environment mismatch is rejected before sudo. The forced-command wrapper accepts only `deploy TARGET IMMUTABLE_IMAGE`, parses it without `eval`, and rejects arbitrary shell commands, mutable tags and other registries.

3. Install a narrowly scoped sudoers rule for each account, using `visudo` validation, for example:

   ```text
   backend-deploy-test ALL=(root) NOPASSWD: /usr/local/sbin/bannergress-deploy-backend test *
   backend-deploy-prod ALL=(root) NOPASSWD: /usr/local/sbin/bannergress-deploy-backend production *
   ```

   The root helper revalidates both arguments and the exact repository/digest format before doing anything. It uses fixed directories/ports, a per-target lock and only `application --no-deps`. Keep Compose/env files root-owned, restrict saved configuration permissions, and review image provenance before publishing a Release. Deploying application code is a privileged operation even with a restricted key; a compromise of the trusted main/release workflow remains significant.

4. Configure the following **environment secrets** separately for `test` and `production`:

   | Secret | Value |
   | --- | --- |
   | `SSH_HOST` | Verified server hostname |
   | `SSH_PORT` | Existing SSH port |
   | `SSH_USER` | The target-specific restricted account |
   | `SSH_PRIVATE_KEY` | That account's dedicated key |
   | `SSH_KNOWN_HOSTS` | Independently verified, port-qualified pinned host-key entry |

   Do not dynamically trust `ssh-keyscan` output during deployment or disable host verification. The runner writes key/known-host files in a private temporary directory and deletes them even after SSH failure; key content is not put in process arguments or logs. No ordinary SSH password is needed. Ensure the package is linked to this repository and the workflow token can write it.

5. Preserve/audit existing host PostgreSQL backups before enabling releases with migrations. New migrations must be backward compatible with the previous application if automatic image rollback is expected. Test the first automated main release in test, inspect its deployment receipt/public API and logs, then publish a reviewed production Release only after environment setup is complete. No live environment has been deployed to validate this PR.

## Readiness, rollback and recovery

The helper pulls first, without touching the running service. It atomically replaces only the expected application image line in the existing Compose file, preserving other bytes/permissions/ownership; it refuses unexpected Compose layouts. It keeps one root-only `.ci-previous-compose.yaml`, not an accumulating backup directory. It recreates only `application`, verifies the container's exact image ID and probes `/bnrs?limit=1` for up to five minutes. The receipt `.ci-deployment-receipt` records time, environment and digest after success.

On startup/readiness failure or a handled termination signal it restores the previous Compose image and verifies the old application. Failure to recover is an explicit critical error. Rollback restores **application image/config only**, never a database. A successful API readiness check is not a replacement for product-specific functional acceptance. Review migration reversibility/compatibility independently before making a Release; do not silently restore old SQL over live data.

If SSH disconnects, inspect the host receipt/Compose/container image before retrying. Per-target `flock` prevents simultaneous host rollouts; a retry uses a pinned digest and deterministic target. A process kill/host power loss cannot be made atomic with container recreation: inspect the receipt and restore `.ci-previous-compose.yaml` if needed. Do not run global Docker pruning or restart an entire Compose project as generic recovery.

## Local verification

```sh
bash -n scripts/deploy-backend.sh scripts/backend-deploy-command.sh
python3 -m unittest discover -s scripts -p 'test_*.py' -v
actionlint .github/workflows/check-pr.yaml .github/workflows/deploy-backend.yaml
./gradlew test checkstyleMain checkstyleTest bootJar --no-daemon
```

Delivery tests use fake SSH/Docker/curl commands and temporary Compose files. They verify rejection of cross-environment/shell/mutable-tag input, pinned host verification, credential cleanup, successful image-only rollout, failed-pull non-mutation and failed-readiness rollback. They never connect to a real environment.
