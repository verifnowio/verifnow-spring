# CLAUDE.md — VerifNow Java / Spring SDK

Guidance for Claude Code (claude.ai/code) when working in this repository.

Published to **Maven Central** as `io.verifnow:verifnow-spring` (and siblings). Current version:
`2.1.1`. The repository lives under the `verifnowio` GitHub organisation — an older, unrelated
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
./mvnw clean deploy -P release   # signs and publishes to Central — release only
```

Publishing uses `central-publishing-maven-plugin` **0.11.0**. Version 0.6.0 breaks on a field the
Sonatype portal now returns; the upgrade also renamed a parameter, so bumping the version alone is
not enough.

## Conventions

- Java 17+, Spring Boot 3.x (parent `3.5.13`).
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
