# GitHub Actions CI/CD Notes

## Goal

Build Phase 8 of the retail order service by adding GitHub Actions CI so the project automatically builds, tests, and packages the Spring Boot application on push and pull request.

## What We Built

- `.github/workflows/ci.yml`.
- Push and pull request workflow triggers.
- Java 21 setup in CI.
- Maven dependency caching.
- Automated `mvn test` execution.
- Automated `mvn package` execution.
- Packaged jar verification.
- README CI badge.

## Why It Matters

CI/CD shows source control discipline and protects the project from broken changes. Every push or pull request gets the same automated checks, which helps prove that tests pass and the application can still be packaged before changes are merged.

## Code Concepts

- GitHub Actions workflows live in `.github/workflows`.
- `actions/checkout` downloads the repository into the runner.
- `actions/setup-java` installs Java 21 and enables Maven caching.
- `mvn test` proves the automated test suite.
- `mvn package` proves the Spring Boot application can be built into a jar.
- The workflow stops at CI verification and does not deploy the application.

## Files Changed

- `.github/workflows/ci.yml`
- `README.md`
- `docs/testing-guide.md`
- `docs/architecture.md`
- `docs/troubleshooting.md`
- `docs/youtube/08-github-actions-ci.md`
- `CHANGELOG.md`

## Workflow Summary

On each push or pull request, GitHub Actions runs:

```text
Checkout repository
Set up Java 21
Restore/cache Maven dependencies
Run mvn test
Run mvn package
Verify the packaged jar exists
```

## How to Test Locally

Run the automated test suite:

```bash
mvn test
```

Package the application:

```bash
mvn package
```

## Manual Demo Flow

These steps are for the video/demo walkthrough. The GitHub Actions workflow is the automated verification method for this phase.

Show the workflow file:

```bash
cat .github/workflows/ci.yml
```

Show local tests passing:

```bash
mvn test
```

Show local packaging:

```bash
mvn package
```

If the local `.m2` cache is not writable, use a project-local Maven repository:

```bash
mvn -B -ntp "-Dmaven.repo.local=target/maven-repository" package
```

Show the expected commit:

```bash
git status
git add .
git commit -m "Phase 8: add GitHub Actions CI workflow"
```

After pushing to GitHub, open the repository Actions tab and show the workflow running or passing.

## Common Errors

- Workflow does not run: confirm the file is under `.github/workflows/ci.yml`.
- Java version mismatch: confirm the workflow sets `java-version: "21"`.
- Maven dependency download fails: rerun after network or Maven repository availability returns.
- Tests fail in CI: run `mvn test` locally and fix the failing behavior.
- Package fails in CI: run `mvn package` locally and confirm the jar is generated.
- Local package fails with a Maven plugin cache error: allow Maven to refresh the local `.m2` cache, then rerun `mvn package`.
- Badge does not update immediately: wait for GitHub to finish the workflow run.

## Interview Talking Points

- Explain why automated tests should run before code is merged.
- Explain how CI catches regressions earlier than manual testing.
- Explain why employers care about repeatable build verification.
- Explain how Maven caching speeds up workflow runs.
- Explain why this phase verifies build readiness without deploying anything yet.

## Definition of Done

- `mvn test` passes locally.
- `mvn package` passes locally.
- GitHub Actions workflow is configured for push and pull request.
- CI uses Java 21 and Maven caching.
- CI runs tests and package verification.
- README and project documentation are updated.

## Commit Message

```bash
git commit -m "Phase 8: add GitHub Actions CI workflow"
```
