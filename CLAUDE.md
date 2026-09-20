# CLAUDE.md — VerifNow Java / Spring SDK

Guidance for Claude Code (claude.ai/code) when working in this repository.

Published to **Maven Central** as `io.verifnow:verifnow-spring` (and siblings). Current version:
`2.7.0`. The repository lives under the `verifnowio` GitHub organisation — an older, unrelated
`validation-spring` repo under the personal account is dead and must not be used.

## Modules

| module | contains |
|---|---|
| `verifnow-core` | transport-free client, DTOs, `VerifNowClient` |
| `verifnow-spring` | Spring integration: auto-configuration, `@VerifNowVat`/`@VerifNowIban`/… constraint annotations |
| `verifnow-spring-boot-starter` | the dependency users actually declare |

## Two bugs that must never come back

Both shipped to Maven Central in 2.1.0 and were fixed in 2.1.1. They are the reason this repo has
tests at all.

- **The default `baseUrl` pointed at `api.verifnow.com`** — a host with no DNS record. The real host
  is `api.verifnow.io`. Combined with fail-open behaviour on transport errors, every call failed and
  then returned `valid=true`: an application following the README validated nothing while appearing
  to work. `ValidationProperties.baseUrl` carries a comment saying exactly this. Leave it there.
- **`validateAsync()` targeted the wrong path**, so it 404'd every time.

The lesson is not "be careful with strings". It is that **a default that is wrong fails silently
when the failure mode is permissive**. Prefer failing loudly over letting a caller believe a value
was validated.

## Commands

```bash
./mvnw clean test        # unit tests, no network
./mvnw clean install     # local install for testing against a sample app
./mvnw clean deploy -P release   # signs and publishes to Central — prefer the workflow below
```

Publishing uses `central-publishing-maven-plugin` **0.11.0**. Version 0.6.0 breaks on a field the
Sonatype portal now returns; the upgrade also renamed a parameter, so bumping the version alone is
not enough.

## CI and release workflows

Three workflows, recovered from the repository this SDK was extracted from (`validation-spring`,
whose remote no longer exists) and adapted:

- **`ci.yml`** — build and tests on `main` / `develop` and their PRs.
- **`deploy.yml`** — *Deploy to Maven Central*. Runs on a `v*.*.*` tag, or on demand: it strips
  `-SNAPSHOT`, deploys, tags, publishes a GitHub release with the three jars, then bumps to the next
  patch snapshot. This is the release path — prefer it over deploying from a laptop, which is how
  an unsigned or half-configured artifact reaches Central.
- **`security.yml`** — OWASP dependency-check and Snyk, **weekly and on demand only**. The
  dependency-check plugin is configured in the parent POM but deliberately **left unbound to any
  phase**: bound to `verify` it ran inside `mvn clean verify` (every CI build) and inside
  `mvn clean deploy` (every release), where a CVSS >= 7 finding in a transitive dependency fails
  the release itself. The workflow calls the goal directly. It is
  deliberately off the push and PR paths: dependency-check downloads the NVD database, which takes
  hours without an API key and stalls every review behind a scan whose result rarely changes
  between two commits. That mistake cost days of CI time in `validAPI` before the scan was moved to
  a schedule there.

Two files from the old repository were **not** brought over, on purpose: a `settings.xml` and a
`DEPLOYMENT_GUIDE.md` that both describe the OSSRH portal. This project publishes through the
Central Portal via `central-publishing-maven-plugin`, no workflow referenced that settings file, and
following the old guide would send you to configure credentials the release path does not read.

**Required secrets** (on the `verifnowio/verifnow-spring` repository or the organisation):
`CENTRAL_USERNAME`, `CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE` for publishing;
`NVD_API_KEY` to keep the security scan from crawling; `SNYK_TOKEN` (its step tolerates failure).
**Neither of the last two is set today**, so `security.yml` skips both scans and says so in a
warning annotation rather than crawling the NVD feed for 45 minutes. A free key is issued in
minutes at <https://nvd.nist.gov/developers/request-an-api-key>; adding it as a repository secret
is all it takes to turn the weekly scan back on.
A release fails late and confusingly when one of the first four is missing — check them before
tagging.

## Conventions

- Java 17+, Spring Boot 3.x (parent `3.5.16`).
- Every source file carries the license header (`LICENSE_HEADER`, enforced by `license-maven-plugin`).
- Public API is a published contract: additive changes only, semver strictly. Someone else's build
  depends on this.
- Constructor injection, records for DTOs, no field injection — same rules as `validAPI`.
- Tests are unit tests, offline. A test that needs the live API is a test that fails at random.
- The SDK version is sent as a request header so API-side usage can be attributed per version. Keep
  it aligned with the Maven version.

## Related

- API: `validAPI` — endpoints and response shapes
- Node equivalent: `verifnow-node` (`@verifnow/sdk` on npm)
- Docs site: `verifnow-doc` — its Java snippets must match this SDK's real API
